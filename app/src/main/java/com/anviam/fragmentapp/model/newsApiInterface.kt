package com.anviam.fragmentapp.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiInterface {

    @GET("top-headlines")
    fun getTopNews(
        @Query("apiKey") apiKey: String
    ): Call<NewsApiResponse>

    companion object {
        const val API_KEY = "e992f617-418c-45cb-8719-9e12ac20c556"
    }
}
