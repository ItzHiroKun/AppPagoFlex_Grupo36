package com.example.apppagoflex_grupo36.data.remote

import com.example.apppagoflex_grupo36.data.remote.dto.CompromisoDto
import retrofit2.http.*

interface PagoFlexApiService {
    @GET("api/compromisos")
    suspend fun obtenerTodos(): List<CompromisoDto>

    @GET("api/compromisos/usuario/{rut}")
    suspend fun obtenerPorRut(@Path("rut") rut: String): List<CompromisoDto>

    @POST("api/compromisos")
    suspend fun crearCompromiso(@Body compromiso: CompromisoDto): CompromisoDto

    @PUT("api/compromisos/{id}/pagar")
    suspend fun pagarCompromiso(@Path("id") id: Long): CompromisoDto

    @DELETE("api/compromisos/{id}")
    suspend fun eliminarCompromiso(@Path("id") id: Long)
}