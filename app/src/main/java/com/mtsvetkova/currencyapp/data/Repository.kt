package com.mtsvetkova.currencyapp.data

import com.mtsvetkova.currencyapp.data.storage.Currency
import com.mtsvetkova.currencyapp.data.storage.StorageManager
import com.mtsvetkova.currencyapp.data.storage.Exchange
import com.mtsvetkova.currencyapp.data.storage.Rate
import com.mtsvetkova.currencyapp.data.network.NetworkManager
import com.mtsvetkova.currencyapp.data.storage.TimeSeriesEntry
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object Repository {

    fun getStorageCurrencies() = StorageManager.currencyDao.getAll()

    suspend fun getFavoriteCurrencies() = StorageManager.currencyDao.getFavorite()

    suspend fun refreshCurrencies() = StorageManager.currencyDao.insertCurrencies(
        NetworkManager.getCurrencies().symbols.keys.map { currency ->
            Currency(
                code = currency,
                favorite = false,
                lastUsedInstant = null,
            )
        }
    )

    suspend fun setFavorite(code: String, favorite: Boolean) =
        StorageManager.currencyDao.setFavorite(code, favorite)

    suspend fun getRate(fromCurrency: String, toCurrency: String) =
        StorageManager.rateDao.get(fromCurrency, toCurrency).let { dbRate ->
            if (dbRate != null && isRecent(dbRate)) dbRate
            else {
                Rate(
                    currency1 = fromCurrency,
                    currency2 = toCurrency,
                    rate = NetworkManager.convert(fromCurrency, toCurrency).result,
                    instant = Instant.now()
                ).also { StorageManager.rateDao.insert(it) }
            }
        }

    suspend fun exchange(
        currency1Code: String,
        currency1Amount: BigDecimal,
        currency2Code: String,
        currency2Amount: BigDecimal
    ) {
        val now = Instant.now()
        StorageManager.exchangeDao.insert(
            Exchange(currency1Code, currency1Amount, currency2Code, currency2Amount, now)
        )
        StorageManager.currencyDao.setLastUsedInstant(currency1Code, now)
        StorageManager.currencyDao.setLastUsedInstant(currency2Code, now)
    }

    suspend fun isStorageRateOutdated(currency1: String, currency2: String) =
        !isRecent(getRate(currency1, currency2))

    fun getExchanges() = StorageManager.exchangeDao.get()

    suspend fun getOrSetFilterBeginDate(): LocalDate =
        StorageManager.getBeginDate() ?: LocalDate.now().also { setFilterPeriodBegin(it) }

    suspend fun getOrSetFilterEndDate(): LocalDate =
        StorageManager.getEndDate() ?: LocalDate.now().also { setFilterPeriodEnd(it) }

    suspend fun getTimeSeries(
        beginDate: LocalDate,
        endDate: LocalDate,
        currency1: String,
        currency2: String
    ): List<Pair<LocalDate, BigDecimal>> {
        val dates = generateLocalDatesList(beginDate, endDate)
        val storageTimeSeries = getStorageTimeSeries(currency1, currency2, dates)
        return if (dates.size == storageTimeSeries.size) {
            storageTimeSeries
        } else {
            getAndPutNetworkTimeSeries(currency1, currency2, dates)
        }
    }

    private suspend fun getAndPutNetworkTimeSeries(
        currency1: String,
        currency2: String,
        dates: List<LocalDate>
    ): List<Pair<LocalDate, BigDecimal>> {
        val timeSeries = getNetworkTimeSeries(dates.first(), dates.last(), currency1, currency2)
        dates.forEach { date ->
            val (euroRate1, euroRate2) = timeSeries[date]!!
            with(StorageManager.timeSeriesDao) {
                insertEntry(TimeSeriesEntry(currency1, date, euroRate1))
                insertEntry(TimeSeriesEntry(currency2, date, euroRate2))
            }
        }
        return timeSeries.map { (date, rates) -> date to rates.second / rates.first } // (euro/c2) / (euro/c1) = c1/c2
    }

    private suspend fun getNetworkTimeSeries(
        beginDate: LocalDate,
        endDate: LocalDate,
        currency1: String,
        currency2: String
    ): Map<LocalDate, Pair<BigDecimal, BigDecimal>> {
        val timeSeries = NetworkManager.timeSeries(
            startDate = beginDate.toString(),
            endDate = endDate.toString(),
            currencies = "$currency1,$currency2",
        ).rates
            .mapKeys { LocalDate.parse(it.key) }
            .mapValues { (_, rates) ->
                rates[currency1]!!.toBigDecimal() to rates[currency2]!!.toBigDecimal()
            }
        return timeSeries
    }

    private suspend fun getStorageTimeSeries(
        currency1: String,
        currency2: String,
        dates: List<LocalDate>
    ) = buildMap<LocalDate, BigDecimal> {
        dates.forEach { date ->
            val rate1 = StorageManager.timeSeriesDao.getEntry(currency1, date)?.rateToEuro
            val rate2 = StorageManager.timeSeriesDao.getEntry(currency2, date)?.rateToEuro
            // (euro/c2) / (euro/c1) = c1/c2
            if (rate1 != null && rate2 != null) put(date, rate2 / rate1) else return@forEach
        }
    }.toList()

    suspend fun setFilterPeriodBegin(date: LocalDate) = StorageManager.setBeginDate(date)

    suspend fun setFilterPeriodEnd(date: LocalDate) = StorageManager.setEndDate(date)

    private fun isRecent(rate: Rate) = Duration.between(rate.instant, Instant.now()).toMinutes() < 5
}