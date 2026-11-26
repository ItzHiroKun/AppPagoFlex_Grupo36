package com.example.apppagoflex_grupo36.viewmodel

import android.app.Application
import com.example.apppagoflex_grupo36.data.local.EstadoDataStore
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class EstadoViewModelTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
        mockkConstructor(EstadoDataStore::class)
    }

    afterSpec {
        Dispatchers.resetMain()
        unmockkAll()
    }

    test("cargarEstado inicializa activo tras delay y alternarEstado guarda y muestra confirmación") {
        runTest(testDispatcher) {
            every { anyConstructed<EstadoDataStore>().obtenerEstado() } returns flowOf(false)
            coEvery { anyConstructed<EstadoDataStore>().guardarEstado(any()) } returns Unit

            val app = mockk<Application>(relaxed = true)
            val vm = EstadoViewModel(app)

            // Antes de los 1500ms sigue cargando (null)
            vm.activo.value shouldBe null

            advanceTimeBy(1501)
            runCurrent()
            vm.activo.value shouldBe false
            vm.modoOscuro.value shouldBe false

            // Alternar estado activa modo oscuro y muestra confirmación temporal
            vm.alternarEstado()
            runCurrent()
            vm.activo.value shouldBe true
            vm.mostrarMensaje.value shouldBe true
            coVerify { anyConstructed<EstadoDataStore>().guardarEstado(true) }

            // Tras 2000ms desaparece el mensaje de confirmación
            advanceTimeBy(2001)
            runCurrent()
            vm.mostrarMensaje.value shouldBe false
        }
    }
})