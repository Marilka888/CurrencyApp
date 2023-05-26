package com.mtsvetkova.currencyapp.data

import java.time.LocalDate

fun generateLocalDatesList(beginDate: LocalDate, endDate: LocalDate) =
    generateSequence(beginDate) { if (it == endDate) null else it.plusDays(1) }.toList()