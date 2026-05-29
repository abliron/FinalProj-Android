package com.example.finalproj

data class Driver(
    val id: Int,
    val name: String,
    val licenseNumber: String? = null
)

data class Vehicle(
    val id: Int,
    val licensePlate: String,
    val model: String? = null
)

data class Company(
    val id: Int,
    val name: String
)
