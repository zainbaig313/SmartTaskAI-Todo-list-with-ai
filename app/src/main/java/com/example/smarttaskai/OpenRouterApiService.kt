package com.example.smarttaskai

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Header

interface OpenRouterApiService {

    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun analyzeEmotion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>
}