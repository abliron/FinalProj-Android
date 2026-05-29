package com.example.finalproj

import com.google.gson.annotations.SerializedName

data class Vehicle(
    @SerializedName("VehicleID", alternate = ["id", "_id", "vehicleId", "vehicleid"])
    val vehicleId: Int?,
    
    @SerializedName("LicensePlate", alternate = ["licensePlate", "licenseplate"])
    val licensePlate: String?,

    @SerializedName("VIN", alternate = ["vin"])
    val vin: String?,
    
    @SerializedName("Make", alternate = ["make", "manufacturer"])
    val make: String?,
    
    @SerializedName("Model", alternate = ["model"])
    val model: String?,

    @SerializedName("Year", alternate = ["year"])
    val year: Int?,

    @SerializedName("Status", alternate = ["status"])
    val status: String?,

    @SerializedName("VehicleType", alternate = ["vehicleType"])
    val vehicleType: String?
)
