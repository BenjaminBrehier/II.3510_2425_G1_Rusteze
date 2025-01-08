package com.bl.rustyze.data.models

import org.json.JSONObject

data class Vehicle(
    val make: String,
    val model: String
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
        vehicles.add(Vehicle(make, model))
    }
    return vehicles
}