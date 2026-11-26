package com.example.apppagoflex_grupo36.viewmodel

import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import com.example.apppagoflex_grupo36.data.repository.PagoFlexRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AgenteViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec { Dispatchers.setMain(testDispatcher) }
    afterSpec { Dispatchers.resetMain() }

    fun crearViewModel(): Triple<PagoFlexDao, PagoFlexRepository, AgenteViewModel> {
        val dao = mockk<PagoFlexDao>(relaxed = true)
        val repo = mockk<PagoFlexRepository>(relaxed = true)

        val u1 = UsuarioEntity(10L, "12.345.678-5", "Juan Pérez", 1, null, "juan@test.cl")
        val e1 = mockk<EmpresaClienteEntity>(relaxed = true) {
            every { idEmpresa } returns 1L
            every { razonSocial } returns "Servicios Eléctricos S.A."
        }
        val c1 = CompromisoEntity(1L, 10L, 1L, 50000.0, "2026-12-01", CompromisoEntity.ESTADO_PENDIENTE)
        val c2 = CompromisoEntity(2L, 10L, 1L, 80000.0, "2026-10-01", CompromisoEntity.ESTADO_VENCIDO)

        every { dao.getAllCompromisos() } returns MutableStateFlow(listOf(c1, c2))
        every { dao.getAllUsuarios() } returns MutableStateFlow(listOf(u1))
        every { dao.getAllEmpresas() } returns MutableStateFlow(listOf(e1))
        every { dao.getAllPagos() } returns MutableStateFlow(emptyList())

        return Triple(dao, repo, AgenteViewModel(dao, repo))
    }

    test("filtra compromisos por estado y por búsqueda de RUT o nombre") {
        val (_, _, vm) = crearViewModel()

        vm.setStatusFilter("VENCIDO")
        vm.uiState.first { it.selectedStatusFilter == "VENCIDO" }.compromisosFiltrados shouldHaveSize 1

        vm.setStatusFilter("Todos")
        vm.setSearchQuery("12345678")
        vm.uiState.first { it.searchQuery == "12345678" }.compromisosFiltrados shouldHaveSize 2

        vm.setSearchQuery("Inexistente")
        vm.uiState.first { it.searchQuery == "Inexistente" }.compromisosFiltrados shouldHaveSize 0
    }

    test("gestiona notas de soporte y cambio de estado de tickets") {
        val (_, _, vm) = crearViewModel()

        vm.guardarNotaSoporte(1L, "Cliente solicita prórroga")
        vm.toggleEstadoTicket(1L)
        vm.toggleEstadoTicket(2L)

        val state = vm.uiState.first { it.ticketsSoporteMap.size == 2 }
        state.ticketsSoporteMap[1L]?.nota shouldBe "Cliente solicita prórroga"
        state.ticketsSoporteMap[1L]?.resuelto shouldBe true
        state.ticketsSoporteMap[2L]?.resuelto shouldBe true
    }

    test("anularCompromiso actualiza estado en DAO y llama al repositorio") {
        val (dao, repo, vm) = crearViewModel()
        val c1 = CompromisoEntity(1L, 10L, 1L, 50000.0, "2026-12-01", CompromisoEntity.ESTADO_PENDIENTE)
        coEvery { dao.getCompromisoById(1L) } returns c1

        vm.anularCompromiso(1L)

        coVerify { dao.updateCompromiso(c1.copy(estado = CompromisoEntity.ESTADO_ANULADO)) }
        coVerify { repo.eliminarCompromiso(1L) }
    }

    test("AgenteViewModelFactory crea instancia válida y valida tipo") {
        val (dao, repo, _) = crearViewModel()
        val factory = AgenteViewModelFactory(dao, repo)
        factory.create(AgenteViewModel::class.java)
        shouldThrow<IllegalArgumentException> {
            factory.create(EjecutivoViewModel::class.java)
        }
    }
})