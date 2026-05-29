package com.example.finalproj

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Company(
    @SerializedName("CompanyID", alternate = ["id", "_id", "companyid"])
    val id: String?,
    @SerializedName("CompanyName", alternate = ["name", "companyName", "companyname"])
    val name: String?
) : Serializable