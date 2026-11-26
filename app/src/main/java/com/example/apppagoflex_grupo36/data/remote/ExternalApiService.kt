package com.example.apppagoflex_grupo36.data.remote

import com.example.apppagoflex_grupo36.data.remote.dto.MindicadorResponse
import retrofit2.http.GET

interface ExternalApiService {
    @GET("api")
    suspend fun obtenerIndicadores(): MindicadorResponse
}