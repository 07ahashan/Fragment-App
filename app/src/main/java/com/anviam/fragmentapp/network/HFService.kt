package com.anviam.fragmentapp.network

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// Request models
data class HFParams(
    val prompt: String
)

data class HFRequest(
    val inputs: String,
    val parameters: HFParams
)

interface HFApi {
    @POST("fal-ai/fal-ai/flux-kontext/dev?_subdomain=queue")
    suspend fun transform(
        @Header("Authorization") auth: String,
        @Body body: HFRequest
    ): Response<ResponseBody>
}

object HFService {
    private const val BASE_URL = "https://router.huggingface.co/"

    private val logging: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    val api: HFApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HFApi::class.java)
    }
}
