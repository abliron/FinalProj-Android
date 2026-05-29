package com.example.finalproj

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token", alternate = ["accessToken", "jwt"])
    val token: String?,
    val requiresCompanySelection: Boolean?,
    val companies: List<Company>?,
    val selectionToken: String?
)
