package com.mtsvetkova.currencyapp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyResponse(val success: Boolean, val symbols: Map<String, String>)