package com.mtsvetkova.currencyapp.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtsvetkova.currencyapp.data.Repository
import com.mtsvetkova.currencyapp.data.storage.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

class AnalyticsViewModel : ViewModel() {
    private val _state = MutableStateFlow<State?>(null)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            Repository.getStorageCurrencies().collect {
                try {
                    _state.value = State(
                        currency1 = it[0].code,
                        currency2 = it[1].code,
                        period = State.Period.WEEK,
                        timeSeries = null,
                        allCurrencies = it
                    )
                    loadTimeSeries()
                } catch (e: Exception) {
                    _state.value = null
                }

            }
        }
    }

    fun setCurrency1(currency: Currency) {
        _state.value = state.value!!.copy(currency1 = currency.code)
        loadTimeSeries()

    }

    private fun loadTimeSeries() {
        _state.value = state.value!!.copy(timeSeries = null)
        viewModelScope.launch {
            _state.value = state.value!!.copy(
                timeSeries = try {
                    val now = LocalDate.now()
                    Result.success(
                        state.value!!.run {
                            Repository.getTimeSeries(
                                beginDate = when (period) {
                                    State.Period.WEEK -> now.minusDays(7)
                                    State.Period.TWO_WEEKS -> now.minusDays(14)
                                    State.Period.MONTH -> now.minusMonths(1)
                                },
                                endDate = now,
                                currency1 = currency1,
                                currency2 = currency2,
                            )
                        }
                    )
                } catch (e: Exception) {
                    Result.failure(e)
                }
            )
        }
    }

    fun setCurrency2(currency: Currency) {
        _state.value = state.value!!.copy(currency2 = currency.code)
        loadTimeSeries()
    }

    fun setPeriod(period: State.Period) {
        _state.value = state.value!!.copy(period = period)
        loadTimeSeries()
    }


    data class State(
        val currency1: String,
        val currency2: String,
        val period: Period,
        val timeSeries: Result<List<Pair<LocalDate, BigDecimal>>>?,
        private val allCurrencies: List<Currency>
    ) {
        val currencies = allCurrencies.filterNot { it.code == currency1 || it.code == currency2 }

        enum class Period { WEEK, TWO_WEEKS, MONTH }
    }
}