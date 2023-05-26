package com.mtsvetkova.currencyapp.data.storage

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.mtsvetkova.currencyapp.data.generateLocalDatesList
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

@SuppressLint("StaticFieldLeak")
object StorageManager {
    private val periodBeginKey = stringPreferencesKey("begin")
    private val periodEndKey = stringPreferencesKey("end")
    private val Context.dataStore by preferencesDataStore("preferences")
    private lateinit var database: Database
    private lateinit var dataStore: DataStore<Preferences>
    val currencyDao get() = database.currencyDao()
    val rateDao get() = database.rateDao()
    val exchangeDao get() = database.exchangeDao()
    val timeSeriesDao get() = database.timeSeriesDao()

    fun init(context: Context) {
        database = Room.databaseBuilder(context, Database::class.java, "db")
            .fallbackToDestructiveMigration()
            .build()
        dataStore = context.dataStore
    }

    suspend fun clearOutdatedTimeSeries() = LocalDate.now().let { now ->
        timeSeriesDao.clearEntries(generateLocalDatesList(now.minusMonths(1), now))
    }

    suspend fun getBeginDate() = getLocalDate(periodBeginKey)
    suspend fun getEndDate() = getLocalDate(periodEndKey)

    suspend fun setBeginDate(date: LocalDate) = dataStore.edit {
        it[periodBeginKey] = date.toString()
    }

    suspend fun setEndDate(date: LocalDate) = dataStore.edit { it[periodEndKey] = date.toString() }

    private suspend fun getLocalDate(key: Preferences.Key<String>) =
        dataStore.data.firstOrNull()?.get(key)?.let { LocalDate.parse(it) }
}