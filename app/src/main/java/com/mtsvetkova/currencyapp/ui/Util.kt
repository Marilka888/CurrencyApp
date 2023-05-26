package com.mtsvetkova.currencyapp.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun LocalDate.format(): String = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))