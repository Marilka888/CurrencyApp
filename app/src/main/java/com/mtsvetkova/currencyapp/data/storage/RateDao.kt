package com.mtsvetkova.currencyapp.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RateDao {
    @Query("SELECT * FROM rate WHERE currency1 = :currency1 AND currency2 = :currency2 LIMIT 1")
    suspend fun get(currency1: String, currency2: String): Rate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rate: Rate)
}