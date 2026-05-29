package com.example.finalproj

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token", alternate = ["accessToken", "jwt"])
    val token: String?,
    val requiresCompanySelection: Boolean?,
    
    @SerializedName("companies", alternate = ["userCompanies"])
    val companies: List<Company>?,
    
    val selectionToken: String?,
    
    // הוספת אובייקט משתמש כי ה-API מחזיר נתונים בתוכו לעיתים
    val user: UserData?
)

data class UserData(
    val userId: Int?,
    val firstName: String?,
    val lastName: String?,
    val role: String?,
    
    @SerializedName("userCompanies", alternate = ["companies"])
    val userCompanies: List<Company>?
)
