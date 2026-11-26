package com.example.apppagoflex_grupo36.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.data.local.entity.PagoEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import com.example.apppagoflex_grupo36.data.repository.PagoFlexRepository
import com.example.apppagoflex_grupo36.utils.RUTUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SupportTicketData(
    val idCompromiso: Long,
    val nota: String,
    val resuelto: Boolean = false
)

data class AgenteUiState(
    val compromisos: List<CompromisoEntity> = emptyList(),
    val compromisosFiltrados: List<CompromisoEntity> = emptyList(),
    val usuariosMap: Map<Long, UsuarioEntity> = emptyMap(),
    val empresasMap: Map<Long, EmpresaClienteEntity> = emptyMap(),
    val pagosMap: Map<Long, PagoEntity> = emptyMap(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "Todos",
    val ticketsSoporteMap: Map<Long, SupportTicketData> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AgenteViewModel(
    private val dao: PagoFlexDao,
    private val repository: PagoFlexRepository = PagoFlexRepository()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatusFilter = MutableStateFlow("Todos")
    private val _ticketsSoporte = MutableStateFlow(mapOf<Long, SupportTicketData>())
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AgenteUiState> = combine(
        dao.getAllCompromisos(),
        dao.getAllUsuarios(),
        dao.getAllEmpresas(),
        dao.getAllPagos(),
        combine(_searchQuery, _selectedStatusFilter, _ticketsSoporte, _errorMessage) { query, statusFilter, tickets, error ->
            Pair(Pair(query, statusFilter), Pair(tickets, error))
        }
    ) { compromisos, usuarios, empresas, pagos, (filters, ticketAndError) ->
        val (query, statusFilter) = filters
        val (tickets, error) = ticketAndError

        val usuariosMap = usuarios.associateBy { it.idUsuario }
        val empresasMap = empresas.associateBy { it.idEmpresa }
        val pagosMap = pagos.associateBy { it.idCompromiso }

        val cleanQuery = query.trim()

        val filtrados = compromisos.filter { compromiso ->
            val statusMatches = when (statusFilter) {
                "Todos" -> true
                else -> compromiso.estado.equals(statusFilter, ignoreCase = true)
            }

            val searchMatches = if (cleanQuery.isEmpty()) {
                true
            } else {
                val user = usuariosMap[compromiso.idUsuario]
                val empresa = empresasMap[compromiso.idEmpresa]
                val userRutClean = user?.rutUsuario?.let { RUTUtils.cleanRut(it) } ?: ""
                val queryClean = RUTUtils.cleanRut(cleanQuery)

                compromiso.idCompromiso.toString().contains(cleanQuery) ||
                        (user != null && user.nombreUsuario.contains(cleanQuery, ignoreCase = true)) ||
                        (user != null && user.rutUsuario.contains(cleanQuery, ignoreCase = true)) ||
                        (queryClean.isNotEmpty() && userRutClean.contains(queryClean)) ||
                        (empresa != null && empresa.razonSocial.contains(cleanQuery, ignoreCase = true))
            }

            statusMatches && searchMatches
        }

        AgenteUiState(
            compromisos = compromisos,
            compromisosFiltrados = filtrados,
            usuariosMap = usuariosMap,
            empresasMap = empresasMap,
            pagosMap = pagosMap,
            searchQuery = query,
            selectedStatusFilter = statusFilter,
            ticketsSoporteMap = tickets,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AgenteUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _selectedStatusFilter.value = filter
    }

    fun guardarNotaSoporte(idCompromiso: Long, nota: String) {
        val currentTickets = _ticketsSoporte.value.toMutableMap()
        val existing = currentTickets[idCompromiso]
        currentTickets[idCompromiso] = SupportTicketData(
            idCompromiso = idCompromiso,
            nota = nota,
            resuelto = existing?.resuelto ?: false
        )
        _ticketsSoporte.value = currentTickets
    }

    fun toggleEstadoTicket(idCompromiso: Long) {
        val currentTickets = _ticketsSoporte.value.toMutableMap()
        val existing = currentTickets[idCompromiso]
        if (existing != null) {
            currentTickets[idCompromiso] = existing.copy(resuelto = !existing.resuelto)
        } else {
            currentTickets[idCompromiso] = SupportTicketData(
                idCompromiso = idCompromiso,
                nota = "Consulta o reporte de soporte general.",
                resuelto = true
            )
        }
        _ticketsSoporte.value = currentTickets
    }

    fun anularCompromiso(idCompromiso: Long) {
        viewModelScope.launch {
            try {
                val compromiso = dao.getCompromisoById(idCompromiso)
                if (compromiso != null) {
                    dao.updateCompromiso(compromiso.copy(estado = CompromisoEntity.ESTADO_ANULADO))
                    try {
                        repository.eliminarCompromiso(idCompromiso)
                    } catch (_: Exception) { }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al anular compromiso: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

class AgenteViewModelFactory(
    private val dao: PagoFlexDao,
    private val repository: PagoFlexRepository = PagoFlexRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgenteViewModel::class.java)) {
            return AgenteViewModel(dao, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}