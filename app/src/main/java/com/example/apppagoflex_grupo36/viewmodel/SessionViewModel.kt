package com.example.apppagoflex_grupo36.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.apppagoflex_grupo36.data.local.DatabaseSeeder
import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.data.local.entity.RolEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import com.example.apppagoflex_grupo36.utils.RUTUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionState(
    val usuarioActual: UsuarioEntity? = null,
    val rolActual: RolEntity? = null,
    val empresaActual: EmpresaClienteEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isLoggedIn: Boolean
        get() = usuarioActual != null && rolActual != null
}

class SessionViewModel(
    private val dao: PagoFlexDao
) : ViewModel() {

    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    val usuarios: StateFlow<List<UsuarioEntity>> = dao.getAllUsuarios()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatabaseSeeder.usuariosInitial
        )

    val roles: StateFlow<List<RolEntity>> = dao.getAllRoles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatabaseSeeder.rolesInitial
        )

    val empresas: StateFlow<List<EmpresaClienteEntity>> = dao.getAllEmpresas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DatabaseSeeder.empresasInitial
        )

    fun selectUser(usuario: UsuarioEntity) {
        viewModelScope.launch {
            _sessionState.value = _sessionState.value.copy(isLoading = true, error = null)

            val rol = dao.getRolById(usuario.idRol)
                ?: DatabaseSeeder.rolesInitial.find { it.idRol == usuario.idRol }

            val empresa = usuario.idEmpresa?.let { id ->
                dao.getEmpresaById(id)
                    ?: DatabaseSeeder.empresasInitial.find { it.idEmpresa == id }
            }

            _sessionState.value = SessionState(
                usuarioActual = usuario,
                rolActual = rol,
                empresaActual = empresa,
                isLoading = false,
                error = null
            )
        }
    }

    fun selectRole(idRol: Int) {
        viewModelScope.launch {
            _sessionState.value = _sessionState.value.copy(isLoading = true, error = null)

            val availableUsers = dao.getAllUsuariosList().ifEmpty { DatabaseSeeder.usuariosInitial }
            val matchingUser = availableUsers.firstOrNull { it.idRol == idRol }

            if (matchingUser != null) {
                selectUser(matchingUser)
            } else {
                _sessionState.value = _sessionState.value.copy(
                    isLoading = false,
                    error = "No se encontró ningún usuario con el rol seleccionado."
                )
            }
        }
    }

    suspend fun loginByRut(rutInput: String): Boolean {
        _sessionState.value = _sessionState.value.copy(isLoading = true, error = null)

        if (!RUTUtils.validateRut(rutInput)) {
            _sessionState.value = _sessionState.value.copy(
                isLoading = false,
                error = "RUT no válido. Verifique el dígito verificador (Módulo 11)."
            )
            return false
        }

        val formattedRut = RUTUtils.formatRut(rutInput)
        val cleanedRut = RUTUtils.cleanRut(rutInput)

        var usuario = dao.getUsuarioByRut(formattedRut)
        if (usuario == null) {
            val allUsers = dao.getAllUsuariosList().ifEmpty { DatabaseSeeder.usuariosInitial }
            usuario = allUsers.firstOrNull { RUTUtils.cleanRut(it.rutUsuario) == cleanedRut }
        }

        return if (usuario != null) {
            selectUser(usuario)
            true
        } else {
            _sessionState.value = _sessionState.value.copy(
                isLoading = false,
                error = "El RUT $formattedRut no está registrado en la base de datos."
            )
            false
        }
    }

    fun logout() {
        _sessionState.value = SessionState()
    }

    fun clearError() {
        _sessionState.value = _sessionState.value.copy(error = null)
    }
}

class SessionViewModelFactory(private val dao: PagoFlexDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            return SessionViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
