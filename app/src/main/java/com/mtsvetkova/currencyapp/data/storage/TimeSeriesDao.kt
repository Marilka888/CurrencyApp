package com.mtsvetkova.currencyapp.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDate

@Dao
interface TimeSeriesDao {

    @Query("SELECT * FROM timeseriesentry WHERE currency = :currency AND date = :date")
    suspend fun getEntry(currency: String, date: LocalDate): TimeSeriesEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TimeSeriesEntry)

    @Query("DELETE FROM timeseriesentry WHERE date NOT IN (:dates)")
    suspend fun clearEntries(dates: List<LocalDate>)
}