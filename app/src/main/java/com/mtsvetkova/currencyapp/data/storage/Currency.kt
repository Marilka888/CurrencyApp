package com.mtsvetkova.currencyapp.data.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity
data class Currency(
    @PrimaryKey val code: String,
    val favorite: Boolean,
    val lastUsedInstant: Instant?
)