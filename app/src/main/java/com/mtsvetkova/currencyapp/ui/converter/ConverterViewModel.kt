package com.mtsvetkova.currencyapp.ui.converter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtsvetkova.currencyapp.data.Repository
import com.mtsvetkova.currencyapp.ui.BigDecimalFormat
import com.mtsvetkova.currencyapp.ui.extractCurrency1Code
import com.mtsvetkova.currencyapp.ui.extractCurrency2Code
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.ParseException
import kotlin.Exception

class ConverterViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _state = MutableStateFlow(
        State(
            currency1Code = savedStateHandle.extractCurrency1Code(),
            currency1Value = null,
            currency2Code = null,
            currency2Value = null,
            exchangeAvailable = false,
            exchangeResult = null,
        )
    )
    val state = _state.asStateFlow()
    private var job: Job? = null

    init {
        job = viewModelScope.launch {
            _state.value = state.value.copy(
                currency2Code = getCurrency2Code(savedStateHandle, state.value.currency1Code)
            )
            setCurrency1("1")
        }
    }

    fun setCurrency1(value: String) = convertUpdateCurrencies(
        currencyFromValue = value,
        direct = true,
        onCurrencyFromValueChange = { updateStateWithExchangeAvailable(currency1Value = it) },
        onCurrencyToValueChange = { updateStateWithExchangeAvailable(currency2Value = it) }
    )

    fun setCurrency2(value: String) = convertUpdateCurrencies(
        currencyFromValue = value,
        direct = false,
        onCurrencyFromValueChange = { updateStateWithExchangeAvailable(currency2Value = it) },
        onCurrencyToValueChange = { updateStateWithExchangeAvailable(currency1Value = it) },
    )

    fun exchange() {
        viewModelScope.launch {
            with(state.value) {
                if (Repository.isStorageRateOutdated(currency1Code, currency2Code!!)) {
                    updateStateWithExchangeAvailable(
                        currency2Value = convert(
                            amount = BigDecimalFormat.parse(currency1Value!!.getOrThrow()),
                            direct = true
                        ),
                        exchangeResult = Result.failure(Exception())
                    )
                } else {
                    Repository.exchange(
                        currency1Code = currency1Code,
                        currency1Amount = BigDecimalFormat.parse(currency1Value!!.getOrThrow()),
                        currency2Code = currency2Code,
                        currency2Amount = BigDecimalFormat.parse(currency2Value!!.getOrThrow())
                    )
                    _state.value = state.value.copy(exchangeResult = Result.success(Unit))
                }
            }
        }
    }

    private fun convertUpdateCurrencies(
        currencyFromValue: String,
        direct: Boolean,
        onCurrencyFromValueChange: (Result<String>) -> Unit,
        onCurrencyToValueChange: (Result<String>?) -> Unit,
    ) {
        job?.cancel()
        onCurrencyFromValueChange(Result.success(currencyFromValue))
        if (currencyFromValue.isEmpty()) onCurrencyToValueChange(Result.success(""))
        else {
            val amount = parseToBigDecimalOrNull(currencyFromValue)
            if (amount != null) {
                onCurrencyToValueChange(null)
                job = viewModelScope.launch {
                    onCurrencyToValueChange(convert(amount, direct))
                }
            } else {
                onCurrencyToValueChange(Result.failure(NumberFormatException()))
            }
        }
    }

    private suspend fun convert(amount: BigDecimal, direct: Boolean) = try {
        val rate = getRate()
        Result.success(BigDecimalFormat.format(if (direct) (amount * rate) else (amount / rate)))
    } catch (e: Exception) {
        Result.failure(e)
    }


    private suspend fun getRate() = BigDecimal.valueOf(
        Repository.getRate(state.value.currency1Code, state.value.currency2Code!!).rate
    )

    private suspend fun getCurrency2Code(savedStateHandle: SavedStateHandle, currency1: String) =
        savedStateHandle.extractCurrency2Code()
            ?: Repository.getFavoriteCurrencies().firstOrNull { it.code != currency1 }?.code
            ?: if (currency1 != CURRENCY_RUB) CURRENCY_RUB else CURRENCY_USD

    private fun parseToBigDecimalOrNull(string: String) = try {
        BigDecimalFormat.parse(string)
    } catch (e: ParseException) {
        null
    }

    private fun updateStateWithExchangeAvailable(
        currency1Value: Result<String>? = state.value.currency1Value,
        currency2Value: Result<String>? = state.value.currency2Value,
        exchangeResult: Result<Unit>? = state.value.exchangeResult
    ) {
        _state.value = state.value.copy(
            currency1Value = currency1Value,
            currency2Value = currency2Value,
            exchangeAvailable = isValidBigDecimal(currency1Value) &&
                    isValidBigDecimal(currency2Value),
            exchangeResult = exchangeResult,
        )
    }

    private fun isValidBigDecimal(value: Result<String>?) =
        value?.getOrNull()?.let { parseToBigDecimalOrNull(it) } != null

    private companion object {
        private const val CURRENCY_RUB = "RUB"
        private const val CURRENCY_USD = "USD"
    }

    data class State(
        val currency1Code: String,
        val currency1Value: Result<String>?,
        val currency2Code: String?,
        val currency2Value: Result<String>?,
        val exchangeAvailable: Boolean,
        val exchangeResult: Result<Unit>?,
    )
}