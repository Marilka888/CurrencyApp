package com.mtsvetkova.currencyapp.data.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database([Currency::class, Rate::class, Exchange::class, TimeSeriesEntry::class], version = 1)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    abstract fun rateDao(): RateDao
    abstract fun exchangeDao(): ExchangeDao
    abstract fun timeSeriesDao(): TimeSeriesDao
}