package com.example.apppagoflex_grupo36.viewmodel

import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import com.example.apppagoflex_grupo36.data.remote.dto.MindicadorResponse
import com.example.apppagoflex_grupo36.data.repository.PagoFlexRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
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
class UsuarioHomeViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec { Dispatchers.setMain(testDispatcher) }
    afterSpec { Dispatchers.resetMain() }

    fun crearViewModel(): Triple<PagoFlexDao, PagoFlexRepository, UsuarioHomeViewModel> {
        val dao = mockk<PagoFlexDao>(relaxed = true)
        val repo = mockk<PagoFlexRepository>(relaxed = true)

        val usuario = UsuarioEntity(10L, "12.345.678-5", "Juan Pérez", 1, null, "juan@test.cl")
        val empresa = mockk<EmpresaClienteEntity>(relaxed = true) {
            every { idEmpresa } returns 1L
            every { razonSocial } returns "Agua Potable S.A."
        }
        val cPendiente = CompromisoEntity(1L, 10L, 1L, 40000.0, "2026-12-20", CompromisoEntity.ESTADO_PENDIENTE)
        val cVencido = CompromisoEntity(2L, 10L, 1L, 50000.0, "2026-11-01", CompromisoEntity.ESTADO_VENCIDO)

        val mindicador = mockk<MindicadorResponse>(relaxed = true) {
            every { uf.valor } returns 38500.0
        }

        coEvery { repo.obtenerIndicadoresEconomicos() } returns mindicador
        coEvery { repo.obtenerCompromisos() } returns emptyList()
        coEvery { dao.getUsuarioById(10L) } returns usuario
        coEvery { dao.getEmpresaById(1L) } returns empresa
        coEvery { dao.getCompromisosListByUsuario(10L) } returns listOf(cPendiente, cVencido)

        every { dao.getCompromisosByUsuario(10L) } returns MutableStateFlow(listOf(cPendiente, cVencido))
        every { dao.getAllPagos() } returns MutableStateFlow(emptyList())
        every { dao.getAllEmpresas() } returns MutableStateFlow(listOf(empresa))
        every { dao.getAllUsuarios() } returns MutableStateFlow(listOf(usuario))

        return Triple(dao, repo, UsuarioHomeViewModel(dao, 10L, repo))
    }

    test("carga UF externa y calcula totalAPagar incluyendo recargo por vencido") {
        val (_, _, vm) = crearViewModel()
        val state = vm.uiState.first { it.compromisos.isNotEmpty() }

        state.valorUf shouldBe 38500.0
        // 40.000 (pendiente) + 55.000 (vencido con 10% recargo) = 95.000
        state.totalAPagar shouldBe 95000.0
    }

    test("pagarProximoCompromiso prioriza deuda vencida, registra en Room y sincroniza PUT con backend") {
        val (dao, repo, vm) = crearViewModel()

        vm.pagarProximoCompromiso()

        val state = vm.uiState.first { it.comprobanteReciente != null }
        state.comprobanteReciente?.idCompromiso shouldBe 2L
        state.comprobanteReciente?.montoPagado shouldBe 55000.0

        coVerify { dao.registrarPagoTransaction(any()) }
        coVerify { repo.pagarCompromiso(2L) }

        vm.dismissComprobanteDialog()
        vm.uiState.first { it.comprobanteReciente == null }.comprobanteReciente shouldBe null
    }

    test("pagarProximoCompromiso muestra error cuando no existen compromisos pendientes o vencidos") {
        val (dao, _, vm) = crearViewModel()
        coEvery { dao.getCompromisosListByUsuario(10L) } returns emptyList()

        vm.pagarProximoCompromiso()

        val state = vm.uiState.first { it.errorMessage != null }
        state.errorMessage shouldContain "No hay compromisos pendientes"
        vm.clearError()
        vm.uiState.first { it.errorMessage == null }.errorMessage shouldBe null
    }

    test("UsuarioHomeViewModelFactory crea instancia válida y rechaza clases incorrectas") {
        val (dao, repo, _) = crearViewModel()
        val factory = UsuarioHomeViewModelFactory(dao, 10L, repo)
        factory.create(UsuarioHomeViewModel::class.java) shouldNotBe null
        shouldThrow<IllegalArgumentException> {
            factory.create(AgenteViewModel::class.java)
        }
    }
})