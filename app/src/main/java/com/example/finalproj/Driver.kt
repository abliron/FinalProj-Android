package com.example.finalproj

import com.google.gson.annotations.SerializedName

data class Driver(
    @SerializedName("DriverID", alternate = ["id", "_id", "driverId", "driverid"])
    val driverId: Int?,
    
    @SerializedName("FirstName", alternate = ["firstname", "firstName"])
    val firstname: String?,
    
    @SerializedName("LastName", alternate = ["lastname", "lastName"])
    val lastname: String?,

    @SerializedName("IDNumber", alternate = ["idNumber", "identityCard"])
    val idNumber: String?,
    
    @SerializedName("BirthDate", alternate = ["birthDate", "birthday"])
    val birthDate: String?,
    
    @SerializedName("DrivingLicenseNumber", alternate = ["drivinglicensenumber", "drivingLicenseNumber"])
    val drivinglicensenumber: String?,

    @SerializedName("LicenseTypeCode", alternate = ["licenseTypeCode"])
    val licenseTypeCode: String?,

    @SerializedName("LicenseExpiryDate", alternate = ["licenseExpiryDate"])
    val licenseExpiryDate: String?
)