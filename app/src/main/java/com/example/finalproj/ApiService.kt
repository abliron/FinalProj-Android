package com.example.finalproj

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/users/login")
    fun signIn(@Body user: User): Call<LoginResponse>
}
