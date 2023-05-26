package com.mtsvetkova.currencyapp.data.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {

    @GET("symbols")
    suspend fun getCurrencies(): CurrencyResponse

    @GET("convert")
    suspend fun convert(
        @Query("from") fromCurrency: String,
        @Query("to") toCurrency: String,
        @Query("amount") amount: Double = 1.0
    ): ConvertResponse

    @GET("timeseries")
    suspend fun timeSeries(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("symbols") currencies: String
    ): TimeSeriesResponse
}