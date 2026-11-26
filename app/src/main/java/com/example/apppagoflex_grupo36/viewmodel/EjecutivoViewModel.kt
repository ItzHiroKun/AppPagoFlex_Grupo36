package com.example.apppagoflex_grupo36.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import com.example.apppagoflex_grupo36.data.remote.dto.CompromisoDto
import com.example.apppagoflex_grupo36.data.repository.PagoFlexRepository
import com.example.apppagoflex_grupo36.utils.CalculoUtils
import com.example.apppagoflex_grupo36.utils.RUTUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EjecutivoUiState(
    val empresa: EmpresaClienteEntity? = null,
    val compromisos: List<CompromisoEntity> = emptyList(),
    val usuariosMap: Map<Long, UsuarioEntity> = emptyMap(),
    val tasaCobranza: Double = 0.0,
    val countPendiente: Int = 0,
    val countVencido: Int = 0,
    val countPagado: Int = 0,
    val countAnulado: Int = 0,
    val totalMontoComprometido: Double = 0.0,
    val totalMontoRecaudado: Double = 0.0,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class EjecutivoViewModel(
    private val dao: PagoFlexDao,
    private val idEmpresa: Long,
    private val repository: PagoFlexRepository = PagoFlexRepository()
) : ViewModel() {

    private val _successMessage = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<EjecutivoUiState> = combine(
        dao.getCompromisosByEmpresa(idEmpresa),
        dao.getAllUsuarios(),
        dao.getAllEmpresas(),
        combine(_successMessage, _errorMessage) { succ, err -> Pair(succ, err) }
    ) { compromisos, usuarios, empresas, (successMsg, errorMsg) ->
        val empresa = empresas.find { it.idEmpresa == idEmpresa }
        val usuariosMap = usuarios.associateBy { it.idUsuario }

        val tasaCobranza = CalculoUtils.calcularTasaCobranza(compromisos)

        val countPendiente = compromisos.count { it.estado.equals(CompromisoEntity.ESTADO_PENDIENTE, ignoreCase = true) }
        val countVencido = compromisos.count { it.estado.equals(CompromisoEntity.ESTADO_VENCIDO, ignoreCase = true) }
        val countPagado = compromisos.count { it.estado.equals(CompromisoEntity.ESTADO_PAGADO, ignoreCase = true) }
        val countAnulado = compromisos.count { it.estado.equals(CompromisoEntity.ESTADO_ANULADO, ignoreCase = true) }

        val totalComprometido = compromisos
            .filter { !it.estado.equals(CompromisoEntity.ESTADO_ANULADO, ignoreCase = true) }
            .sumOf { it.monto }

        val totalRecaudado = compromisos
            .filter { it.estado.equals(CompromisoEntity.ESTADO_PAGADO, ignoreCase = true) }
            .sumOf { it.monto }

        EjecutivoUiState(
            empresa = empresa,
            compromisos = compromisos,
            usuariosMap = usuariosMap,
            tasaCobranza = tasaCobranza,
            countPendiente = countPendiente,
            countVencido = countVencido,
            countPagado = countPagado,
            countAnulado = countAnulado,
            totalMontoComprometido = totalComprometido,
            totalMontoRecaudado = totalRecaudado,
            successMessage = successMsg,
            errorMessage = errorMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EjecutivoUiState()
    )

    fun registrarCompromiso(rutInput: String, monto: Double, fechaVencimiento: String) {
        viewModelScope.launch {
            _successMessage.value = null
            _errorMessage.value = null

            if (monto <= 0) {
                _errorMessage.value = "El monto debe ser un valor mayor a cero."
                return@launch
            }

            if (!RUTUtils.validateRut(rutInput)) {
                _errorMessage.value = "RUT no válido. Verifique el formato y dígito verificador."
                return@launch
            }

            if (fechaVencimiento.isBlank()) {
                _errorMessage.value = "Debe ingresar una fecha de vencimiento válida (YYYY-MM-DD)."
                return@launch
            }

            try {
                val formattedRut = RUTUtils.formatRut(rutInput)
                var usuario = dao.getUsuarioByRut(formattedRut)

                if (usuario == null) {
                    val cleanInput = RUTUtils.cleanRut(rutInput)
                    val allUsers = dao.getAllUsuariosList()
                    usuario = allUsers.firstOrNull { RUTUtils.cleanRut(it.rutUsuario) == cleanInput }
                }

                if (usuario == null) {
                    val newUserId = dao.insertUsuario(
                        UsuarioEntity(
                            rutUsuario = formattedRut,
                            nombreUsuario = "Cliente $formattedRut",
                            idRol = 1,
                            idEmpresa = null,
                            correo = "cliente.${RUTUtils.cleanRut(formattedRut)}@pagoflex.cl"
                        )
                    )
                    usuario = dao.getUsuarioById(newUserId)
                }

                if (usuario != null) {
                    val nuevoCompromiso = CompromisoEntity(
                        idUsuario = usuario.idUsuario,
                        idEmpresa = idEmpresa,
                        monto = monto,
                        fechaVencimiento = fechaVencimiento.trim(),
                        estado = CompromisoEntity.ESTADO_PENDIENTE
                    )
                    dao.insertCompromiso(nuevoCompromiso)
                    try {
                        repository.crearCompromiso(
                            CompromisoDto(
                                usuarioRut = formattedRut,
                                usuarioNombre = usuario.nombreUsuario,
                                empresaNombre = "Empresa #$idEmpresa",
                                descripcion = "Compromiso registrado desde App",
                                monto = monto.toInt(),
                                fechaVencimiento = fechaVencimiento.trim(),
                                estado = "PENDIENTE"
                            )
                        )
                        _successMessage.value = "Compromiso registrado en App y Backend para RUT $formattedRut."
                    } catch (e: Exception) {
                        Log.e("POST_BACKEND", "Fallo al enviar POST a Spring Boot", e)
                        _errorMessage.value = "Guardado local, pero falló red: ${e.message}"
                    }
                } else {
                    _errorMessage.value = "No se pudo vincular el usuario al compromiso."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al registrar el compromiso: ${e.localizedMessage}"
            }
        }
    }

    fun anularCompromiso(idCompromiso: Long) {
        viewModelScope.launch {
            _successMessage.value = null
            _errorMessage.value = null

            try {
                val compromiso = dao.getCompromisoById(idCompromiso)
                if (compromiso != null) {
                    if (compromiso.estado.equals(CompromisoEntity.ESTADO_PAGADO, ignoreCase = true)) {
                        _errorMessage.value = "No se puede anular un compromiso que ya fue pagado."
                        return@launch
                    }
                    dao.updateCompromiso(compromiso.copy(estado = CompromisoEntity.ESTADO_ANULADO))
                    try {
                        repository.eliminarCompromiso(idCompromiso)
                    } catch (e: Exception) {
                        Log.e("DELETE_BACKEND", "Fallo al eliminar en Spring Boot", e)
                    }

                    _successMessage.value = "Compromiso #${idCompromiso} anulado correctamente."
                } else {
                    _errorMessage.value = "No se encontró el compromiso a anular."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al anular compromiso: ${e.localizedMessage}"
            }
        }
    }

    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
    }
}

class EjecutivoViewModelFactory(
    private val dao: PagoFlexDao,
    private val idEmpresa: Long,
    private val repository: PagoFlexRepository = PagoFlexRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EjecutivoViewModel::class.java)) {
            return EjecutivoViewModel(dao, idEmpresa, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}