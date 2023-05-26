package com.mtsvetkova.currencyapp.ui

import java.math.BigDecimal
import java.text.DecimalFormat

object BigDecimalFormat {
    private val format = (DecimalFormat.getInstance() as DecimalFormat).apply {
        isParseBigDecimal = true
        isGroupingUsed = false
    }

    fun format(value: BigDecimal) = format.format(value)
    fun parse(value: String) = format.parse(value) as BigDecimal
}