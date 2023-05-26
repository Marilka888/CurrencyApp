package com.mtsvetkova.currencyapp.ui.analytics

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mtsvetkova.currencyapp.R
import com.mtsvetkova.currencyapp.data.storage.Currency
import com.mtsvetkova.currencyapp.ui.ClickableTextField
import com.mtsvetkova.currencyapp.ui.TopAppBar
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.roundToInt
import com.mtsvetkova.currencyapp.ui.analytics.AnalyticsViewModel.State as State

@Composable
fun AnalyticsRoute(viewModel: AnalyticsViewModel = viewModel()) {
    val state = viewModel.state.collectAsState().value
    if (state != null) {
        var dialog by remember { mutableStateOf<Dialog?>(null) }
        Screen(
            state = state,
            onCurrency1Click = { dialog = Dialog.CURRENCY_1 },
            onCurrency2Click = { dialog = Dialog.CURRENCY_2 },
            onPeriodClick = viewModel::setPeriod
        )
        dialog?.let { type ->
            Dialog(
                currencies = state.currencies,
                onCurrencyClick = {
                    if (type == Dialog.CURRENCY_1) viewModel.setCurrency1(it)
                    else viewModel.setCurrency2(it)
                    dialog = null
                },
                onDismiss = { dialog = null }
            )
        }
    }
}

@Composable
private fun Dialog(
    currencies: List<Currency>,
    onCurrencyClick: (Currency) -> Unit,
    onDismiss: () -> Unit,
) = Dialog(onDismiss) {
    Surface {
        LazyColumn {
            items(currencies) {
                Text(
                    text = it.code,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCurrencyClick(it) }
                        .padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Screen(
    state: State,
    onCurrency1Click: () -> Unit,
    onCurrency2Click: () -> Unit,
    onPeriodClick: (State.Period) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(R.string.top_destination_analytics) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Row(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(16.dp)) {
                ClickableTextField(
                    value = state.currency1, onCurrency1Click,
                    Modifier.weight(1f)
                )
                ClickableTextField(
                    value = state.currency2, onCurrency2Click,
                    Modifier.weight(1f)
                )
            }
            if (state.timeSeries == null) {
                ProgressIndicator()
            } else {
                val data = state.timeSeries.getOrNull()
                if (data != null) Chart(data) else ErrorPlaceholder()
            }
            Row(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(16.dp)) {
                PeriodChip(
                    textId = R.string.period_week,
                    selected = state.period == State.Period.WEEK,
                    onClick = { onPeriodClick(State.Period.WEEK) }
                )
                PeriodChip(
                    textId = R.string.period_two_weeks,
                    selected = state.period == State.Period.TWO_WEEKS,
                    onClick = { onPeriodClick(State.Period.TWO_WEEKS) }
                )
                PeriodChip(
                    textId = R.string.period_month,
                    selected = state.period == State.Period.MONTH,
                    onClick = { onPeriodClick(State.Period.MONTH) }
                )
            }
        }
    }
}

@Composable
private fun ErrorPlaceholder() = Box(Modifier.fillMaxSize(), Alignment.Center) {
    Text(stringResource(R.string.error), style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ColumnScope.Chart(data: List<Pair<LocalDate, BigDecimal>>) = Chart(
    chart = lineChart(),
    model = entryModelOf(*data.map { it.second }.toTypedArray()),
    modifier = Modifier
        .weight(1f)
        .padding(16.dp),
    startAxis = startAxis(),
    bottomAxis = bottomAxis(
        labelRotationDegrees = 270f,
        valueFormatter = { value, _ -> data[value.roundToInt()].first.toString() }
    )
)

@Composable
private fun ProgressIndicator() =
    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodChip(@StringRes textId: Int, selected: Boolean, onClick: () -> Unit) =
    FilterChip(selected = selected, onClick = onClick, label = { Text(stringResource(textId)) })

private enum class Dialog { CURRENCY_1, CURRENCY_2 }