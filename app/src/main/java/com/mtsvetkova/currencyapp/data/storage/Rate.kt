package com.mtsvetkova.currencyapp.data.storage

import androidx.room.Entity
import java.time.Instant

@Entity(primaryKeys = ["currency1", "currency2"])
data class Rate(
    val currency1: String,
    val currency2: String,
    val rate: Double,
    val instant: Instant,
)
