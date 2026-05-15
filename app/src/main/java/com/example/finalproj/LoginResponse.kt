package com.example.finalproj

data class LoginResponse(
    val token: String?,
    val requiresCompanySelection: Boolean?
)
