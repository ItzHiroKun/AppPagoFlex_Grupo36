package com.example.apppagoflex_grupo36.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apppagoflex_grupo36.data.local.EstadoDataStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EstadoViewModel(application: Application) : AndroidViewModel(application) {
    private val estadoDataStore = EstadoDataStore(application)

    /** Estado para la pantalla de Perfil (con loader simulado de 1,5 s, Guía 12). */
    private val _activo = MutableStateFlow<Boolean?>(null)
    val activo: StateFlow<Boolean?> = _activo

    /**
     * Modo oscuro REAL de la app. Se lee directo del DataStore, sin el delay del loader,
     * para que MainActivity pueda aplicar el tema desde el primer momento.
     * null = todavía cargando.
     */
    val modoOscuro: StateFlow<Boolean?> = estadoDataStore.obtenerEstado()
        .map { it ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _mostrarMensaje = MutableStateFlow(false)
    val mostrarMensaje: StateFlow<Boolean> = _mostrarMensaje

    private var mensajeJob: Job? = null

    init {
        cargarEstado()
    }

    private fun cargarEstado() {
        viewModelScope.launch {
            delay(1500)
            _activo.value = estadoDataStore.obtenerEstado().first() ?: false
        }
    }

    fun alternarEstado() {
        viewModelScope.launch {
            val nuevoValor = !(_activo.value ?: false)
            estadoDataStore.guardarEstado(nuevoValor)
            _activo.value = nuevoValor
            mostrarConfirmacion()
        }
    }

    private fun mostrarConfirmacion() {
        mensajeJob?.cancel()
        mensajeJob = viewModelScope.launch {
            _mostrarMensaje.value = true
            delay(2000)
            _mostrarMensaje.value = false
        }
    }
}
