package com.bl.rustyze.data.models

import org.json.JSONObject

data class Vehicle(
    val make: String,
    val model: String,
    val year: Int,
    val fuelType: String,
    val cylinders: Int,
    val trany: String,
    val comb08: Int
)

data class ApiResponse(
    val totalCount: Int,
    val results: List<Vehicle>
)

// Fonction pour convertir le JSON en liste d'objets Vehicle
fun parseVehicles(jsonResponse: String): List<Vehicle> {
    val jsonObject = JSONObject(jsonResponse)
    val resultsArray = jsonObject.getJSONArray("results")
    val vehicles = mutableListOf<Vehicle>()

    for (i in 0 until resultsArray.length()) {
        val result = resultsArray.getJSONObject(i)
        val make = result.optString("make", "Unknown")
        val model = result.optString("model", "Unknown")
        val year = result.optInt("year", 0)
        val fuelType = result.optString("fueltype", "Unknown")
        val cylinders = result.optInt("cylinders", 0)
        val trany = result.optString("trany", "Unknown")
        val comb08 = result.optInt("comb08", 0)
        vehicles.add(Vehicle(make, model, year, fuelType, cylinders, trany, comb08))
    }
    return vehicles
}