package com.mtsvetkova.currencyapp.data.storage

import androidx.room.Entity
import java.math.BigDecimal
import java.time.LocalDate

@Entity(primaryKeys = ["currency", "date"])
data class TimeSeriesEntry(
    val currency: String,
    val date: LocalDate,
    val rateToEuro: BigDecimal,
)
