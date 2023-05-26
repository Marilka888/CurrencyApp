package com.mtsvetkova.currencyapp.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currency ORDER BY favorite DESC, lastUsedInstant DESC")
    fun getAll(): Flow<List<Currency>>

    @Query("SELECT * FROM currency WHERE favorite = 1 ORDER BY lastUsedInstant DESC")
    suspend fun getFavorite(): List<Currency>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCurrencies(currencies: List<Currency>)

    @Query("UPDATE currency SET favorite = :favorite WHERE code = :code")
    suspend fun setFavorite(code: String, favorite: Boolean)

    @Query("UPDATE currency SET lastUsedInstant = :instant WHERE code = :code")
    suspend fun setLastUsedInstant(code: String, instant: Instant)
}