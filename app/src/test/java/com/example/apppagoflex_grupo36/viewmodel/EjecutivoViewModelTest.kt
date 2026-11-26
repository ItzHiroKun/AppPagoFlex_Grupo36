package com.example.apppagoflex_grupo36.viewmodel

import android.util.Log
import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import com.example.apppagoflex_grupo36.data.repository.PagoFlexRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class EjecutivoViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    afterSpec {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    fun crearMocks(): Triple<PagoFlexDao, PagoFlexRepository, EjecutivoViewModel> {
        val dao = mockk<PagoFlexDao>(relaxed = true)
        val repo = mockk<PagoFlexRepository>(relaxed = true)
        val empresaMock = mockk<EmpresaClienteEntity>(relaxed = true) {
            every { idEmpresa } returns 1L
        }
        val compromisosFlow = MutableStateFlow(
            listOf(
                CompromisoEntity(1L, 10L, 1L, 100000.0, "2026-10-01", CompromisoEntity.ESTADO_PAGADO),
                CompromisoEntity(2L, 10L, 1L, 50000.0, "2026-12-01", CompromisoEntity.ESTADO_PENDIENTE)
            )
        )
        every { dao.getCompromisosByEmpresa(1L) } returns compromisosFlow
        every { dao.getAllUsuarios() } returns MutableStateFlow(emptyList())
        every { dao.getAllEmpresas() } returns MutableStateFlow(listOf(empresaMock))

        val vm = EjecutivoViewModel(dao, 1L, repo)
        return Triple(dao, repo, vm)
    }

    test("uiState calcula métricas y totales de empresa correctamente") {
        val (_, _, vm) = crearMocks()
        val state = vm.uiState.first { it.compromisos.isNotEmpty() }

        state.countPagado shouldBe 1
        state.countPendiente shouldBe 1
        state.totalMontoComprometido shouldBe 150000.0
        state.totalMontoRecaudado shouldBe 100000.0
        state.tasaCobranza shouldBe 50.0
    }

    test("registrarCompromiso valida monto, RUT y fecha antes de guardar") {
        val (_, _, vm) = crearMocks()

        vm.registrarCompromiso("12.345.678-5", 0.0, "2026-12-01")
        vm.uiState.first { it.errorMessage != null }.errorMessage shouldContain "mayor a cero"

        vm.registrarCompromiso("12.345.678-9", 50000.0, "2026-12-01")
        vm.uiState.first { it.errorMessage != null }.errorMessage shouldContain "RUT no válido"

        vm.registrarCompromiso("12.345.678-5", 50000.0, "")
        vm.uiState.first { it.errorMessage != null }.errorMessage shouldContain "fecha de vencimiento"
    }

    test("registrarCompromiso crea usuario si no existe y sincroniza con Room y Backend") {
        val (dao, repo, vm) = crearMocks()
        val nuevoUsuario = UsuarioEntity(99L, "12.345.678-5", "Cliente 12.345.678-5", 1, null, "c@pagoflex.cl")

        coEvery { dao.getUsuarioByRut("12.345.678-5") } returns null
        coEvery { dao.getAllUsuariosList() } returns emptyList()
        coEvery { dao.insertUsuario(any()) } returns 99L
        coEvery { dao.getUsuarioById(99L) } returns nuevoUsuario

        vm.registrarCompromiso("12345678-5", 75000.0, "2026-12-15")

        val state = vm.uiState.first { it.successMessage != null }
        state.successMessage shouldContain "App y Backend"
        coVerify { dao.insertCompromiso(any()) }
        coVerify { repo.crearCompromiso(any()) }
    }

    test("registrarCompromiso mantiene guardado local si falla la red del backend") {
        val (dao, repo, vm) = crearMocks()
        val usuario = UsuarioEntity(10L, "12.345.678-5", "Juan Pérez", 1, null, "juan@pagoflex.cl")

        coEvery { dao.getUsuarioByRut("12.345.678-5") } returns usuario
        coEvery { repo.crearCompromiso(any()) } throws RuntimeException("Timeout")

        vm.registrarCompromiso("12.345.678-5", 40000.0, "2026-12-20")

        val state = vm.uiState.first { it.errorMessage != null }
        state.errorMessage shouldContain "falló red"
        vm.clearMessages()
    }

    test("anularCompromiso impide anular pagados y anula pendientes correctamente") {
        val (dao, repo, vm) = crearMocks()
        val pagado = CompromisoEntity(1L, 10L, 1L, 100000.0, "2026-10-01", CompromisoEntity.ESTADO_PAGADO)
        val pendiente = CompromisoEntity(2L, 10L, 1L, 50000.0, "2026-12-01", CompromisoEntity.ESTADO_PENDIENTE)

        coEvery { dao.getCompromisoById(1L) } returns pagado
        vm.anularCompromiso(1L)
        vm.uiState.first { it.errorMessage != null }.errorMessage shouldContain "ya fue pagado"

        coEvery { dao.getCompromisoById(2L) } returns pendiente
        vm.anularCompromiso(2L)
        vm.uiState.first { it.successMessage != null }.successMessage shouldContain "anulado correctamente"
        coVerify { repo.eliminarCompromiso(2L) }
    }

    test("EjecutivoViewModelFactory instancia el ViewModel y lanza excepción con clase desconocida") {
        val (dao, repo, _) = crearMocks()
        val factory = EjecutivoViewModelFactory(dao, 1L, repo)
        factory.create(EjecutivoViewModel::class.java)
        shouldThrow<IllegalArgumentException> {
            factory.create(AgenteViewModel::class.java)
        }
    }
})