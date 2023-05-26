package com.mtsvetkova.currencyapp.ui.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mtsvetkova.currencyapp.R
import com.mtsvetkova.currencyapp.data.storage.Exchange
import com.mtsvetkova.currencyapp.ui.BigDecimalFormat
import com.mtsvetkova.currencyapp.ui.TopAppBar
import com.mtsvetkova.currencyapp.ui.format
import java.time.LocalDate

@Composable
fun ExchangesRoute(
    onNavigateToFilters: () -> Unit,
    viewModel: ExchangesViewModel = viewModel()
) {
    viewModel.state.collectAsState().value?.let { Screen(it, onFiltersClick = onNavigateToFilters) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Screen(state: ExchangesViewModel.State, onFiltersClick: () -> Unit) = Scaffold(
    topBar = { TopAppBar(R.string.top_destination_history) { FiltersIconButton(onFiltersClick) } }
) { padding ->
    Column(Modifier.padding(padding)) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PeriodFilterChip(state.period, onFiltersClick)
            CurrenciesFilterChip(state.selectedCurrencies, onFiltersClick)
        }
        LazyColumn { items(state.exchanges) { Exchange(it) } }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CurrenciesFilterChip(selectedCurrencies: List<String>, onFiltersClick: () -> Unit) =
    FilterChip(
        selected = true,
        onClick = onFiltersClick,
        label = {
            Text(
                text = if (selectedCurrencies.isNotEmpty()) selectedCurrencies.joinToString(", ")
                else stringResource(R.string.all_currencies)
            )
        }
    )

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PeriodFilterChip(period: Pair<LocalDate, LocalDate>?, onFiltersClick: () -> Unit) =
    FilterChip(
        selected = true,
        onClick = onFiltersClick,
        label = {
            Text(
                text = if (period != null) "${period.first.format()} — ${period.second.format()}"
                else stringResource(R.string.no_period)
            )
        }
    )

@Composable
private fun FiltersIconButton(onFiltersClick: () -> Unit) = IconButton(onFiltersClick) {
    Icon(painterResource(R.drawable.baseline_filter_alt_24), contentDescription = null)
}

@Composable
private fun Exchange(it: Exchange) = Column {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${BigDecimalFormat.format(it.currency1Amount)} ${it.currency1Code}",
            modifier = Modifier.weight(1f)
        )
        Icon(painterResource(R.drawable.baseline_arrow_right_alt_24), contentDescription = null)
        Text(
            text = "${BigDecimalFormat.format(it.currency2Amount)} ${it.currency2Code}",
            modifier = Modifier.weight(1f)
        )
    }
    Divider()
}