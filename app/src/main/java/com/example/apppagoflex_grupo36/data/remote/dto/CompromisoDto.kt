package com.example.apppagoflex_grupo36.data.remote.dto

data class CompromisoDto(
    val id: Long? = null,
    val usuarioRut: String,
    val usuarioNombre: String,
    val empresaNombre: String,
    val descripcion: String,
    val monto: Int,
    val fechaVencimiento: String,
    val estado: String = "PENDIENTE"
)