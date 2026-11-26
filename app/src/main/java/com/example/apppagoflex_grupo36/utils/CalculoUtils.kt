package com.example.apppagoflex_grupo36.utils

import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.PagoEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CalculoUtils {

    private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun calcularRecargoPorAtraso(
        compromiso: CompromisoEntity,
        porcentajeRecargo: Double = 0.10,
        recargoDiarioRate: Double = 0.0,
        diasAtraso: Int = 0
    ): Double {
        if (!compromiso.estado.equals(CompromisoEntity.ESTADO_VENCIDO, ignoreCase = true)) {
            return 0.0
        }
        val recargoBase = compromiso.monto * porcentajeRecargo
        val recargoDiario = compromiso.monto * recargoDiarioRate * diasAtraso
        return recargoBase + recargoDiario
    }

    fun calcularTotalAPagar(
        compromiso: CompromisoEntity,
        porcentajeRecargo: Double = 0.10,
        recargoDiarioRate: Double = 0.0,
        diasAtraso: Int = 0
    ): Double {
        return compromiso.monto + calcularRecargoPorAtraso(
            compromiso,
            porcentajeRecargo,
            recargoDiarioRate,
            diasAtraso
        )
    }

    fun calcularPorcentajeCumplimiento(
        compromisos: List<CompromisoEntity>,
        pagos: List<PagoEntity> = emptyList(),
        fechaReferencia: Date = Date()
    ): Double {
        val calendar = Calendar.getInstance()
        calendar.time = fechaReferencia
        calendar.add(Calendar.MONTH, -12)
        val fechaLimiteInferior = calendar.time

        val compromisosUltimos12Meses = compromisos.filter { c ->
            val fechaVenc = parseDate(c.fechaVencimiento)
            if (fechaVenc == null) {
                true
            } else {
                !fechaVenc.before(fechaLimiteInferior) && !fechaVenc.after(fechaReferencia)
            }
        }

        if (compromisosUltimos12Meses.isEmpty()) return 0.0

        val pagosMap = pagos.associateBy { it.idCompromiso }

        val pagadosATiempoCount = compromisosUltimos12Meses.count { c ->
            if (!c.estado.equals(CompromisoEntity.ESTADO_PAGADO, ignoreCase = true)) {
                false
            } else {
                val pago = pagosMap[c.idCompromiso]
                if (pago != null) {
                    val fechaPagoDate = parseDate(pago.fechaPago)
                    val fechaVencDate = parseDate(c.fechaVencimiento)
                    if (fechaPagoDate != null && fechaVencDate != null) {
                        !fechaPagoDate.after(fechaVencDate)
                    } else {
                        true
                    }
                } else {
                    true
                }
            }
        }

        return (pagadosATiempoCount.toDouble() / compromisosUltimos12Meses.size) * 100.0
    }

    fun calcularTasaCobranza(compromisos: List<CompromisoEntity>): Double {
        val validos = compromisos.filter { !it.estado.equals(CompromisoEntity.ESTADO_ANULADO, ignoreCase = true) }
        if (validos.isEmpty()) return 0.0

        val pagadosCount = validos.count { it.estado.equals(CompromisoEntity.ESTADO_PAGADO, ignoreCase = true) }
        return (pagadosCount.toDouble() / validos.size) * 100.0
    }

    fun calcularTasaCobranzaPorEmpresa(
        idEmpresa: Long,
        compromisos: List<CompromisoEntity>
    ): Double {
        val compromisosEmpresa = compromisos.filter { it.idEmpresa == idEmpresa }
        return calcularTasaCobranza(compromisosEmpresa)
    }

    private fun parseDate(dateStr: String): Date? {
        return try {
            DATE_FORMATTER.parse(dateStr)
        } catch (_: Exception) {
            null
        }
    }
}

fun CompromisoEntity.calcularTotalAPagar(porcentajeRecargo: Double = 0.10): Double {
    return CalculoUtils.calcularTotalAPagar(this, porcentajeRecargo)
}

fun List<CompromisoEntity>.calcularTasaCobranza(): Double {
    return CalculoUtils.calcularTasaCobranza(this)
}
