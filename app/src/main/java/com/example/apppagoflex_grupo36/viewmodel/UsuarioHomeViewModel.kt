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
import com.example.apppagoflex_grupo36.ui.components.ComprobanteData
import com.example.apppagoflex_grupo36.utils.CalculoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UsuarioHomeUiState(
    val usuario: UsuarioEntity? = null,
    val compromisos: List<CompromisoEntity> = emptyList(),
    val pagos: List<PagoEntity> = emptyList(),
    val empresasMap: Map<Long, EmpresaClienteEntity> = emptyMap(),
    val totalAPagar: Double = 0.0,
    val porcentajeCumplimiento: Double = 0.0,
    val valorUf: Double? = null,
    val comprobanteReciente: ComprobanteData? = null,
    val isProcessingPayment: Boolean = false,
    val errorMessage: String? = null
)

class UsuarioHomeViewModel(
    private val dao: PagoFlexDao,
    private val idUsuario: Long,
    private val repository: PagoFlexRepository = PagoFlexRepository()
) : ViewModel() {

    private val _comprobanteReciente = MutableStateFlow<ComprobanteData?>(null)
    private val _isProcessing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _valorUf = MutableStateFlow<Double?>(null)

    init {
        cargarDatosExternosYBackend()
    }

    private fun cargarDatosExternosYBackend() {
        viewModelScope.launch {
            // 1. Consumo API Externa (mindicador.cl - UF del día)
            try {
                val indicadores = repository.obtenerIndicadoresEconomicos()
                _valorUf.value = indicadores.uf.valor
            } catch (_: Exception) {
                // Si no hay internet, la UI sigue operando sin bloquearse
            }

            // 2. Sincronización GET con Microservicio Spring Boot
            try {
                repository.obtenerCompromisos()
            } catch (_: Exception) {
                // Respaldo transparente con Room local
            }
        }
    }

    val uiState: StateFlow<UsuarioHomeUiState> = combine(
        dao.getCompromisosByUsuario(idUsuario),
        dao.getAllPagos(),
        dao.getAllEmpresas(),
        dao.getAllUsuarios(),
        combine(_comprobanteReciente, _isProcessing, _errorMessage, _valorUf) { comp, proc, err, uf ->
            Quadruple(comp, proc, err, uf)
        }
    ) { compromisos, todosPagos, empresas, usuarios, (comprobante, isProcessing, error, valorUf) ->
        val usuario = usuarios.find { it.idUsuario == idUsuario }
        val empresasMap = empresas.associateBy { it.idEmpresa }

        val compromisosIds = compromisos.map { it.idCompromiso }.toSet()
        val pagosUsuario = todosPagos.filter { it.idCompromiso in compromisosIds }

        val pendientesOVencidos = compromisos.filter {
            it.estado.equals(CompromisoEntity.ESTADO_PENDIENTE, ignoreCase = true) ||
                    it.estado.equals(CompromisoEntity.ESTADO_VENCIDO, ignoreCase = true)
        }
        val totalAPagar = pendientesOVencidos.sumOf { CalculoUtils.calcularTotalAPagar(it) }
        val cumplimiento = CalculoUtils.calcularPorcentajeCumplimiento(compromisos, pagosUsuario)

        UsuarioHomeUiState(
            usuario = usuario,
            compromisos = compromisos,
            pagos = pagosUsuario,
            empresasMap = empresasMap,
            totalAPagar = totalAPagar,
            porcentajeCumplimiento = cumplimiento,
            valorUf = valorUf,
            comprobanteReciente = comprobante,
            isProcessingPayment = isProcessing,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UsuarioHomeUiState()
    )

    fun pagarProximoCompromiso() {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null

            try {
                val usuario = dao.getUsuarioById(idUsuario)
                if (usuario == null) {
                    _errorMessage.value = "Usuario no encontrado."
                    _isProcessing.value = false
                    return@launch
                }

                val compromisos = dao.getCompromisosListByUsuario(idUsuario)

                val overdue = compromisos.filter {
                    it.estado.equals(CompromisoEntity.ESTADO_VENCIDO, ignoreCase = true)
                }.sortedBy { it.fechaVencimiento }

                val targetCompromiso = if (overdue.isNotEmpty()) {
                    overdue.first()
                } else {
                    compromisos.filter {
                        it.estado.equals(CompromisoEntity.ESTADO_PENDIENTE, ignoreCase = true)
                    }.sortedBy { it.fechaVencimiento }.firstOrNull()
                }

                if (targetCompromiso == null) {
                    _errorMessage.value = "No hay compromisos pendientes ni vencidos para pagar."
                    _isProcessing.value = false
                    return@launch
                }

                val montoTotal = CalculoUtils.calcularTotalAPagar(targetCompromiso)
                val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val receiptCode = "COMPROBANTE-${(10000..99999).random()}"

                val nuevoPago = PagoEntity(
                    idCompromiso = targetCompromiso.idCompromiso,
                    fechaPago = fechaHoy,
                    montoPagado = montoTotal,
                    comprobante = receiptCode
                )

                // Persistencia local Room
                dao.registrarPagoTransaction(nuevoPago)

                // Sincronización PUT con Microservicio Spring Boot
                try {
                    repository.pagarCompromiso(targetCompromiso.idCompromiso)
                } catch (_: Exception) {
                    // Mantiene consistencia local si el backend no responde
                }

                val empresa = dao.getEmpresaById(targetCompromiso.idEmpresa)
                val nombreEmpresa = empresa?.razonSocial ?: "Empresa #${targetCompromiso.idEmpresa}"

                _comprobanteReciente.value = ComprobanteData(
                    comprobanteCode = receiptCode,
                    idCompromiso = targetCompromiso.idCompromiso,
                    rutUsuario = usuario.rutUsuario,
                    nombreEmpresa = nombreEmpresa,
                    fechaPago = fechaHoy,
                    montoPagado = montoTotal
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error al procesar el pago: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun dismissComprobanteDialog() {
        _comprobanteReciente.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

class UsuarioHomeViewModelFactory(
    private val dao: PagoFlexDao,
    private val idUsuario: Long,
    private val repository: PagoFlexRepository = PagoFlexRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsuarioHomeViewModel::class.java)) {
            return UsuarioHomeViewModel(dao, idUsuario, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}