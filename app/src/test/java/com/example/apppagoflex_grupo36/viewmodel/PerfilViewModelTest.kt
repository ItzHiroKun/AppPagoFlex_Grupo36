package com.example.apppagoflex_grupo36.viewmodel

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.apppagoflex_grupo36.data.local.FotoPerfilStorage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
        mockkObject(FotoPerfilStorage)
        mockkStatic(FileProvider::class)
        mockkStatic(Uri::class)
    }

    afterSpec {
        Dispatchers.resetMain()
        unmockkAll()
    }

    fun crearViewModel(): Pair<PerfilViewModel, Uri> {
        val tempCache = Files.createTempDirectory("pagoflex_cache").toFile()
        val app = mockk<Application>(relaxed = true) {
            every { cacheDir } returns tempCache
            every { packageName } returns "com.example.apppagoflex_grupo36"
        }
        val mockUri = mockk<Uri>(relaxed = true)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns mockUri
        every { Uri.fromFile(any()) } returns mockUri
        return Pair(PerfilViewModel(app), mockUri)
    }

    test("cargarFoto actualiza imagenUri desde FotoPerfilStorage") {
        val (vm, mockUri) = crearViewModel()
        every { FotoPerfilStorage.obtenerUri(any(), 10L) } returns mockUri

        vm.cargarFoto(10L)

        withTimeout(2000) {
            vm.imagenUri.first { it != null } shouldBe mockUri
        }
    }

    test("guardarDesdeGaleria actualiza imagenUri y muestra mensaje de éxito o error") {
        val (vm, mockUri) = crearViewModel()
        every { FotoPerfilStorage.guardar(any(), 10L, mockUri) } returns mockUri

        vm.guardarDesdeGaleria(10L, mockUri)
        withTimeout(2000) {
            vm.imagenUri.first { it != null } shouldBe mockUri
            vm.mensaje.first { it != null } shouldBe "Foto de perfil actualizada"
        }

        vm.limpiarMensaje()
        every { FotoPerfilStorage.guardar(any(), 10L, mockUri) } returns null
        vm.guardarDesdeGaleria(10L, mockUri)
        withTimeout(2000) {
            vm.mensaje.first { it != null } shouldBe "No se pudo guardar la foto. Intenta nuevamente."
        }
    }

    test("flujo de cámara crea URI temporal, confirma y cancela foto correctamente") {
        val (vm, mockUri) = crearViewModel()
        every { FotoPerfilStorage.guardar(any(), 10L, any()) } returns mockUri

        // Sin foto pendiente no hace nada
        vm.confirmarFotoCamara(10L)
        vm.imagenUri.value shouldBe null

        // Crea temporal y cancela
        vm.crearUriTemporalCamara() shouldBe mockUri
        vm.cancelarFotoCamara()

        // Crea temporal y confirma
        vm.crearUriTemporalCamara()
        vm.confirmarFotoCamara(10L)
        withTimeout(2000) {
            vm.imagenUri.first { it != null } shouldBe mockUri
            vm.mensaje.first { it != null } shouldBe "Foto de perfil actualizada"
        }
    }

    test("mostrarMensaje y limpiarMensaje gestionan el estado del mensaje") {
        val (vm, _) = crearViewModel()
        vm.mostrarMensaje("Aviso de prueba")
        vm.mensaje.value shouldBe "Aviso de prueba"

        vm.limpiarMensaje()
        vm.mensaje.value shouldBe null
    }
})