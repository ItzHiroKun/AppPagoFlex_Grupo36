package com.example.apppagoflex_grupo36.data.remote.dto

data class MindicadorResponse(
    val uf: SerieIndicador,
    val dolar: SerieIndicador
)

data class SerieIndicador(
    val codigo: String,
    val nombre: String,
    val valor: Double
)