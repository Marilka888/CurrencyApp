package com.mtsvetkova.currencyapp.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeDao {
    @Insert
    suspend fun insert(exchange: Exchange)

    @Query("SELECT * FROM exchange ORDER BY instant DESC")
    fun get(): Flow<List<Exchange>>
}