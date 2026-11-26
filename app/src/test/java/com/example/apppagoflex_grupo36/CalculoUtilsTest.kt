package com.example.apppagoflex_grupo36.utils

import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.PagoEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import java.text.SimpleDateFormat
import java.util.Locale

class CalculoUtilsTest : FunSpec({

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    test("calcularRecargoPorAtraso y calcularTotalAPagar aplican recargo solo cuando está VENCIDO") {
        val vencido = CompromisoEntity(
            idCompromiso = 1L, idUsuario = 1L, idEmpresa = 1L,
            monto = 100000.0, fechaVencimiento = "2026-01-10",
            estado = CompromisoEntity.ESTADO_VENCIDO
        )
        val pendiente = vencido.copy(estado = CompromisoEntity.ESTADO_PENDIENTE)

        CalculoUtils.calcularRecargoPorAtraso(pendiente) shouldBe 0.0
        pendiente.calcularTotalAPagar() shouldBe 100000.0

        CalculoUtils.calcularRecargoPorAtraso(vencido) shouldBe (10000.0 plusOrMinus 0.01)
        vencido.calcularTotalAPagar() shouldBe (110000.0 plusOrMinus 0.01)

        CalculoUtils.calcularTotalAPagar(vencido, porcentajeRecargo = 0.10, recargoDiarioRate = 0.01, diasAtraso = 5) shouldBe (115000.0 plusOrMinus 0.01)
    }

    test("calcularTasaCobranza y calcularTasaCobranzaPorEmpresa excluyen anulados") {
        val lista = listOf(
            CompromisoEntity(1L, 1L, 1L, 50000.0, "2026-05-01", CompromisoEntity.ESTADO_PAGADO),
            CompromisoEntity(2L, 1L, 1L, 50000.0, "2026-06-01", CompromisoEntity.ESTADO_PENDIENTE),
            CompromisoEntity(3L, 1L, 1L, 50000.0, "2026-04-01", CompromisoEntity.ESTADO_ANULADO),
            CompromisoEntity(4L, 1L, 2L, 80000.0, "2026-05-01", CompromisoEntity.ESTADO_PAGADO)
        )

        lista.calcularTasaCobranza() shouldBe (66.66 plusOrMinus 0.01)
        CalculoUtils.calcularTasaCobranzaPorEmpresa(1L, lista) shouldBe (50.0 plusOrMinus 0.01)
        CalculoUtils.calcularTasaCobranza(emptyList()) shouldBe 0.0
    }

    test("calcularPorcentajeCumplimiento evalúa pagos a tiempo dentro de los últimos 12 meses") {
        val fechaRef = sdf.parse("2026-11-25")!!
        val c1 = CompromisoEntity(1L, 1L, 1L, 50000.0, "2026-10-15", CompromisoEntity.ESTADO_PAGADO)
        val c2 = CompromisoEntity(2L, 1L, 1L, 50000.0, "2026-09-10", CompromisoEntity.ESTADO_PAGADO)
        val c3 = CompromisoEntity(3L, 1L, 1L, 50000.0, "2026-08-01", CompromisoEntity.ESTADO_VENCIDO)
        val cAntiguo = CompromisoEntity(4L, 1L, 1L, 50000.0, "2024-01-01", CompromisoEntity.ESTADO_PAGADO)

        val pagos = listOf(
            PagoEntity(1L, 1L, "2026-10-10", 50000.0, "COMP-1"), // A tiempo
            PagoEntity(2L, 2L, "2026-09-20", 55000.0, "COMP-2")  // Atrasado
        )

        val cumplimiento = CalculoUtils.calcularPorcentajeCumplimiento(
            compromisos = listOf(c1, c2, c3, cAntiguo),
            pagos = pagos,
            fechaReferencia = fechaRef
        )
        cumplimiento shouldBe (33.33 plusOrMinus 0.01)
        CalculoUtils.calcularPorcentajeCumplimiento(emptyList(), emptyList(), fechaRef) shouldBe 0.0
    }
})