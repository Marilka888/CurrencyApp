package com.mtsvetkova.currencyapp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtsvetkova.currencyapp.data.Repository
import com.mtsvetkova.currencyapp.data.storage.Exchange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class ExchangesViewModel : ViewModel() {
    var period: Pair<LocalDate, LocalDate>? = null
        private set

    private var _currencies: Map<String, Boolean>? = null
    val currencies get() = _currencies!!
    private val selectedCurrencies get() = currencies.filterValues { it }.map { it.key }

    private var exchanges: List<Exchange>? = null

    private val _state = MutableStateFlow<State?>(null)
    val state = _state.asStateFlow()


    init {
        viewModelScope.launch {
            Repository.getExchanges().collect { exchanges ->
                _currencies = buildMap {
                    exchanges.forEach {
                        put(it.currency1Code, false)
                        put(it.currency2Code, false)
                    }
                }.toSortedMap()
                period = null
                this@ExchangesViewModel.exchanges = exchanges
                setState()
            }
        }
    }

    fun setFilters(period: Pair<LocalDate, LocalDate>?, currencies: Map<String, Boolean>) {
        this.period = period
        _currencies = currencies
        setState()
    }

    private fun setState() {
        _state.value = State(
            period,
            selectedCurrencies,
            exchanges!!.filter { it.matches(period) && it.matches(selectedCurrencies) })
    }

    private fun Exchange.matches(currencies: List<String>) = if (currencies.isNotEmpty()) {
        (currencies.contains(currency1Code) || currencies.contains(currency2Code))
    } else true

    private fun Exchange.matches(period: Pair<LocalDate, LocalDate>?) = if (period != null) {
        period.first.toInstant() <= instant && instant <= period.second.plusDays(1).toInstant()
    } else true

    private fun LocalDate.toInstant() = atStartOfDay(ZoneId.systemDefault()).toInstant()

    data class State(
        val period: Pair<LocalDate, LocalDate>?,
        val selectedCurrencies: List<String>,
        val exchanges: List<Exchange>,
    )
}