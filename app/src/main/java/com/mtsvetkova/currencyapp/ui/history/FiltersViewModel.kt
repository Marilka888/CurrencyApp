package com.mtsvetkova.currencyapp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtsvetkova.currencyapp.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

private val nowLocalDate = LocalDate.now()
private val weekPeriod = nowLocalDate.minusDays(7) to nowLocalDate
private val monthPeriod = nowLocalDate.minusMonths(1) to nowLocalDate

class FiltersViewModel : ViewModel() {
    private val _state = MutableStateFlow<State?>(null)
    val state = _state.asStateFlow()
    private var _currencies: MutableMap<String, Boolean>? = null
    val currencies get() = _currencies!!.toMap()
    val period
        get() = when (state.value!!.periodType) {
            State.PeriodType.ALL -> null
            State.PeriodType.WEEK -> weekPeriod
            State.PeriodType.MONTH -> monthPeriod
            State.PeriodType.CUSTOM -> state.value!!.beginDate to state.value!!.endDate
        }

    fun setInitialFilters(
        period: Pair<LocalDate, LocalDate>?,
        currencies: Map<String, Boolean>,
    ) {
        _currencies = currencies.toMutableMap()
        viewModelScope.launch {
            _state.value = State(
                periodType = when (period) {
                    null -> State.PeriodType.ALL
                    weekPeriod -> State.PeriodType.WEEK
                    monthPeriod -> State.PeriodType.MONTH
                    else -> State.PeriodType.CUSTOM
                },
                beginDate = Repository.getOrSetFilterBeginDate(),
                endDate = Repository.getOrSetFilterEndDate(),
                currencies = currencies.toList(),
            )
        }
    }

    fun setPeriodType(periodType: State.PeriodType) {
        _state.value = state.value!!.copy(periodType = periodType)
    }

    fun setBeginDate(beginDate: LocalDate) {
        state.value!!.let { state ->
            val endDate = if (state.endDate.isBefore(beginDate)) beginDate else state.endDate
            _state.value = state.copy(beginDate = beginDate, endDate = endDate)
            updatePeriodInStorage(beginDate, endDate)
        }

    }

    fun setEndDate(endDate: LocalDate) {
        state.value!!.let { state ->
            val beginDate = if (endDate.isBefore(state.beginDate)) endDate else state.beginDate
            _state.value = state.copy(beginDate = beginDate, endDate = endDate)
            updatePeriodInStorage(beginDate, endDate)
        }
    }

    fun toggleCurrencyFilter(currency: String, selected: Boolean) {
        _currencies!![currency] = selected
        _state.value = state.value!!.copy(currencies = currencies.toList())
    }

    private fun updatePeriodInStorage(beginDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch { Repository.setFilterPeriodBegin(beginDate) }
        viewModelScope.launch { Repository.setFilterPeriodEnd(endDate) }

    }

    data class State(
        val periodType: PeriodType,
        val beginDate: LocalDate,
        val endDate: LocalDate,
        val currencies: List<Pair<String, Boolean>>,
    ) {

        enum class PeriodType {
            ALL, WEEK, MONTH, CUSTOM
        }
    }
}