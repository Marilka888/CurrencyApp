package com.mtsvetkova.currencyapp.data.network

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class TimeSeriesResponse(val rates: Map<String, Map<String, Double>>)