package com.example.equipoOcho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Inventory(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val name: String = "",
    val price: Int = 0,
    val quantity: Int = 0
) : Serializable {

    // ---- Firestore helpers ----

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "price" to price,
        "quantity" to quantity
    )

    companion object {

        fun fromMap(map: Map<String, Any?>): Inventory {
            val id = when (val raw = map["id"]) {
                is Long -> raw.toInt()
                is Int -> raw
                is String -> raw.toIntOrNull() ?: 0
                else -> 0
            }

            val price = when (val raw = map["price"]) {
                is Long -> raw.toInt()
                is Int -> raw
                is Double -> raw.toInt()
                is String -> raw.toIntOrNull() ?: 0
                else -> 0
            }

            val quantity = when (val raw = map["quantity"]) {
                is Long -> raw.toInt()
                is Int -> raw
                is Double -> raw.toInt()
                is String -> raw.toIntOrNull() ?: 0
                else -> 0
            }

            val name = map["name"] as? String ?: ""

            return Inventory(
                id = id,
                name = name,
                price = price,
                quantity = quantity
            )
        }
    }
}
