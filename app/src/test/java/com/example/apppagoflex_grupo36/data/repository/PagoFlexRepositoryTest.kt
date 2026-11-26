package com.example.apppagoflex_grupo36.data.repository

import com.example.apppagoflex_grupo36.data.remote.ExternalApiService
import com.example.apppagoflex_grupo36.data.remote.PagoFlexApiService
import com.example.apppagoflex_grupo36.data.remote.dto.CompromisoDto
import com.example.apppagoflex_grupo36.data.remote.dto.MindicadorResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class PagoFlexRepositoryTest : FunSpec({

    val backendApi = mockk<PagoFlexApiService>()
    val externalApi = mockk<ExternalApiService>()
    val repository = PagoFlexRepository(backendApi, externalApi)

    val dto = CompromisoDto(
        id = 1L,
        usuarioRut = "12.345.678-5",
        usuarioNombre = "Juan Pérez",
        empresaNombre = "Empresa #1",
        descripcion = "Prueba",
        monto = 50000,
        fechaVencimiento = "2026-12-01",
        estado = "PENDIENTE"
    )

    test("delega correctamente todas las operaciones CRUD al backendApi y externalApi") {
        val mindicadorMock = mockk<MindicadorResponse>()

        coEvery { backendApi.obtenerTodos() } returns listOf(dto)
        coEvery { backendApi.obtenerPorRut("12.345.678-5") } returns listOf(dto)
        coEvery { backendApi.crearCompromiso(dto) } returns dto
        coEvery { backendApi.pagarCompromiso(1L) } returns dto.copy(estado = "PAGADO")
        coEvery { backendApi.eliminarCompromiso(1L) } returns Unit
        coEvery { externalApi.obtenerIndicadores() } returns mindicadorMock

        repository.obtenerCompromisos() shouldBe listOf(dto)
        repository.obtenerCompromisosPorRut("12.345.678-5") shouldBe listOf(dto)
        repository.crearCompromiso(dto) shouldBe dto
        repository.pagarCompromiso(1L).estado shouldBe "PAGADO"
        repository.eliminarCompromiso(1L)
        repository.obtenerIndicadoresEconomicos() shouldBe mindicadorMock

        coVerify(exactly = 1) { backendApi.eliminarCompromiso(1L) }
    }
})