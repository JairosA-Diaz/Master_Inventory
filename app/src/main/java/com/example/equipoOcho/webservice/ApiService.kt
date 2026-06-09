package com.example.equipoOcho.webservice

import com.example.equipoOcho.model.ProductModelResponse
import com.example.equipoOcho.utils.Constants.END_POINT
import retrofit2.http.GET

interface ApiService {
    @GET(END_POINT)
    suspend fun getProducts(): MutableList<ProductModelResponse>
}