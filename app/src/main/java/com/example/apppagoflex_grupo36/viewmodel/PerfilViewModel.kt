package com.example.apppagoflex_grupo36.viewmodel

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apppagoflex_grupo36.data.local.FotoPerfilStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val _imagenUri = MutableStateFlow<Uri?>(null)
    val imagenUri: StateFlow<Uri?> = _imagenUri.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private var archivoCamaraPendiente: File? = null

    fun cargarFoto(idUsuario: Long) {
        viewModelScope.launch {
            _imagenUri.value = withContext(Dispatchers.IO) {
                FotoPerfilStorage.obtenerUri(getApplication<Application>(), idUsuario)
            }
        }
    }

    fun guardarDesdeGaleria(idUsuario: Long, uri: Uri) {
        viewModelScope.launch {
            val guardada = withContext(Dispatchers.IO) {
                FotoPerfilStorage.guardar(getApplication<Application>(), idUsuario, uri)
            }
            publicarResultado(guardada)
        }
    }

    fun crearUriTemporalCamara(): Uri {
        val context = getApplication<Application>()
        val directorio = File(context.cacheDir, "camera").apply { if (!exists()) mkdirs() }
        val archivo = File.createTempFile("perfil_", ".jpg", directorio)
        archivoCamaraPendiente = archivo
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    fun confirmarFotoCamara(idUsuario: Long) {
        val temporal = archivoCamaraPendiente ?: return
        archivoCamaraPendiente = null
        viewModelScope.launch {
            val guardada = withContext(Dispatchers.IO) {
                val uri = FotoPerfilStorage.guardar(getApplication<Application>(), idUsuario, Uri.fromFile(temporal))
                temporal.delete()
                uri
            }
            publicarResultado(guardada)
        }
    }

    fun cancelarFotoCamara() {
        archivoCamaraPendiente?.delete()
        archivoCamaraPendiente = null
    }

    fun mostrarMensaje(texto: String) {
        _mensaje.value = texto
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    private fun publicarResultado(uri: Uri?) {
        if (uri != null) {
            _imagenUri.value = uri
            _mensaje.value = "Foto de perfil actualizada"
        } else {
            _mensaje.value = "No se pudo guardar la foto. Intenta nuevamente."
        }
    }
}
