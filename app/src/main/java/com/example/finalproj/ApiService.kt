package com.example.finalproj

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    
    // התחברות ראשונית
    @POST("api/users/login")
    fun signIn(@Body user: User): Call<LoginResponse>

    // קבלת רשימת נהגים
    @GET("api/drivers")
    fun getDrivers(@Header("Authorization") token: String): Call<List<Driver>>

    // קבלת רשימת רכבים
    @GET("api/vehicles")
    fun getVehicles(@Header("Authorization") token: String): Call<List<Vehicle>>

    // קבלת רשימת חברות (למקרים של בחירת חברה לאחר התחברות)
    @GET("api/companies")
    fun getCompanies(@Header("Authorization") selectionToken: String): Call<okhttp3.ResponseBody>

    // שליחת טופס מסירת רכב עם תמונות
    @Multipart
    @POST("api/vehicle-handover/drivers/{driverId}")
    fun submitHandoverForm(
        @Header("Authorization") token: String,
        @Path("driverId") driverId: Int,
        @Part("vehicleId") vehicleId: RequestBody,
        @Part("odometerAtHandover") odometer: RequestBody,
        @Part("fuelLevel") fuel: RequestBody,
        @Part("notes") notes: RequestBody,
        @Part("handoverDate") handoverDate: RequestBody,
        @Part frontPhoto: MultipartBody.Part?,
        @Part rearPhoto: MultipartBody.Part?,
        @Part leftPhoto: MultipartBody.Part?,
        @Part rightPhoto: MultipartBody.Part?
    ): Call<Unit>

    // בחירת חברה לאחר התחברות
    @POST("api/users/select-company")
    fun selectCompany(
        @Header("Authorization") selectionToken: String,
        @Body request: Map<String, String>
    ): Call<LoginResponse>
}
