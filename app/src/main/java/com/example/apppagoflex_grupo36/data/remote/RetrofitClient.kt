package com.example.apppagoflex_grupo36.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Apunta a localhost:8080 mediante el túnel 'adb reverse tcp:8080 tcp:8080'
    private const val BASE_URL_BACKEND = "http://localhost:8080/"
    private const val BASE_URL_EXTERNA = "https://mindicador.cl/"

    val backendApi: PagoFlexApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_BACKEND)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PagoFlexApiService::class.java)
    }

    val externalApi: ExternalApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_EXTERNA)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExternalApiService::class.java)
    }
}