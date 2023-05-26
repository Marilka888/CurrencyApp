package com.mtsvetkova.currencyapp.data.storage

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

object Converters {
    @TypeConverter
    fun fromInstant(value: Instant?) = value?.epochSecond

    @TypeConverter
    fun toInstant(value: Long?) = value?.let { Instant.ofEpochSecond(it) }

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal) = value.toString()

    @TypeConverter
    fun toBigDecimal(value: String) = BigDecimal(value)

    @TypeConverter
    fun fromLocalDate(value: LocalDate) = value.toString()

    @TypeConverter
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)
}