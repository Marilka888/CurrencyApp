package com.mtsvetkova.currencyapp.ui.currencies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtsvetkova.currencyapp.data.Repository
import com.mtsvetkova.currencyapp.data.storage.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CurrenciesViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        State(
            selectedCurrencyCode = null,
            allCurrencies = emptyList(),
            loading = false,
            error = false
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (Repository.getStorageCurrencies().first().isEmpty()) refresh()
            Repository.getStorageCurrencies().collect { currencies ->
                _state.value = state.value.copy(allCurrencies = currencies)
            }
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            _state.value = state.value.copy(loading = true)
            Repository.refreshCurrencies()
        } catch (e: Exception) {
            _state.value = state.value.copy(error = true)
        } finally {
            _state.value = state.value.copy(loading = false)
        }
    }

    fun toggleFavorite(currency: Currency) = viewModelScope.launch {
        Repository.setFavorite(currency.code, !currency.favorite)
    }

    fun selectCurrency(currency: Currency) {
        _state.value = state.value.copy(selectedCurrencyCode = currency.code)
    }

    fun deselectCurrency() {
        _state.value = state.value.copy(selectedCurrencyCode = null)
    }

    fun clearError() {
        _state.value = state.value.copy(error = false)
    }

    data class State(
        private val selectedCurrencyCode: String?,
        private val allCurrencies: List<Currency>,
        val loading: Boolean,
        val error: Boolean,
    ) {
        val currencies = allCurrencies.filterNot { it.code == selectedCurrencyCode }
        val selectedCurrency = allCurrencies.firstOrNull { it.code == selectedCurrencyCode }
    }
}