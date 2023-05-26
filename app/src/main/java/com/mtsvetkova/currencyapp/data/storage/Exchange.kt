package com.mtsvetkova.currencyapp.data.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.Instant

@Entity
data class Exchange(
    val currency1Code: String,
    val currency1Amount: BigDecimal,
    val currency2Code: String,
    val currency2Amount: BigDecimal,
    @PrimaryKey val instant: Instant
)