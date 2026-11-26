package com.example.apppagoflex_grupo36.data.repository

import com.example.apppagoflex_grupo36.data.remote.ExternalApiService
import com.example.apppagoflex_grupo36.data.remote.PagoFlexApiService
import com.example.apppagoflex_grupo36.data.remote.RetrofitClient
import com.example.apppagoflex_grupo36.data.remote.dto.CompromisoDto
import com.example.apppagoflex_grupo36.data.remote.dto.MindicadorResponse

class PagoFlexRepository(
    private val backendApi: PagoFlexApiService = RetrofitClient.backendApi,
    private val externalApi: ExternalApiService = RetrofitClient.externalApi
) {
    suspend fun obtenerCompromisos(): List<CompromisoDto> {
        return backendApi.obtenerTodos()
    }

    suspend fun obtenerCompromisosPorRut(rut: String): List<CompromisoDto> {
        return backendApi.obtenerPorRut(rut)
    }

    suspend fun crearCompromiso(compromiso: CompromisoDto): CompromisoDto {
        return backendApi.crearCompromiso(compromiso)
    }

    suspend fun pagarCompromiso(id: Long): CompromisoDto {
        return backendApi.pagarCompromiso(id)
    }

    suspend fun eliminarCompromiso(id: Long) {
        backendApi.eliminarCompromiso(id)
    }

    suspend fun obtenerIndicadoresEconomicos(): MindicadorResponse {
        return externalApi.obtenerIndicadores()
    }
}