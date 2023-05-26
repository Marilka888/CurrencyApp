package com.mtsvetkova.currencyapp.ui.history

import android.app.DatePickerDialog
import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mtsvetkova.currencyapp.R
import com.mtsvetkova.currencyapp.ui.ClickableTextField
import com.mtsvetkova.currencyapp.ui.TopAppBar
import com.mtsvetkova.currencyapp.ui.format
import com.mtsvetkova.currencyapp.ui.history.FiltersViewModel.State.PeriodType
import java.time.Instant
import java.time.LocalDate

@Composable
fun FiltersRoute(
    exchangesViewModel: ExchangesViewModel,
    onNavigateBack: () -> Unit,
    viewModel: FiltersViewModel = viewModel(),
) {
    viewModel.state.collectAsState().value?.let { state ->
        Screen(
            state = state,
            onPeriodTypeClick = viewModel::setPeriodType,
            onBeginDateChange = viewModel::setBeginDate,
            onEndDateChange = viewModel::setEndDate,
            onCurrencyCheckedChange = viewModel::toggleCurrencyFilter,
            onApplyClick = {
                with(viewModel) { exchangesViewModel.setFilters(period, currencies) }
                onNavigateBack()
            },
            onBackClick = onNavigateBack,
        )
    }
    LaunchedEffect(Unit) {
        with(exchangesViewModel) { viewModel.setInitialFilters(period, currencies) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Screen(
    state: FiltersViewModel.State,
    onPeriodTypeClick: (PeriodType) -> Unit,
    onBeginDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onCurrencyCheckedChange: (String, Boolean) -> Unit,
    onApplyClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(titleId = R.string.filters, onBackClick) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Text(
                text = stringResource(R.string.period),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            RadioItem(
                textId = R.string.period_all,
                selected = state.periodType == PeriodType.ALL,
                onClick = { onPeriodTypeClick(PeriodType.ALL) }
            )
            RadioItem(
                textId = R.string.period_week,
                selected = state.periodType == PeriodType.WEEK,
                onClick = { onPeriodTypeClick(PeriodType.WEEK) }
            )
            RadioItem(
                textId = R.string.period_month,
                selected = state.periodType == PeriodType.MONTH,
                onClick = { onPeriodTypeClick(PeriodType.MONTH) }
            )
            RadioItem(
                textId = R.string.period_custom,
                selected = state.periodType == PeriodType.CUSTOM,
                onClick = { onPeriodTypeClick(PeriodType.CUSTOM) }
            )
            Row(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(16.dp)) {
                val enabled = state.periodType == PeriodType.CUSTOM
                DatePickerField(
                    date = state.beginDate,
                    enabled = enabled,
                    onDatePick = onBeginDateChange,
                )
                DatePickerField(
                    date = state.endDate,
                    enabled = enabled,
                    onDatePick = onEndDateChange,
                )
            }
            Text(
                text = stringResource(R.string.currencies),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            LazyColumn(Modifier.weight(1f)) {
                items(state.currencies) { (currency, checked) ->
                    CheckboxItem(
                        text = currency,
                        checked = checked,
                        onCheckedChange = { onCurrencyCheckedChange(currency, it) })
                }
            }
            Button(
                onClick = onApplyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.apply))
            }
        }
    }
}

@Composable
private fun RadioItem(@StringRes textId: Int, selected: Boolean, onClick: () -> Unit) =
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(stringResource(textId), Modifier.padding(horizontal = 16.dp))
        RadioButton(selected, onClick)
    }

@Composable
private fun CheckboxItem(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) =
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(text, Modifier.padding(horizontal = 16.dp))
        Checkbox(checked, onCheckedChange)
    }

@Composable
private fun RowScope.DatePickerField(
    date: LocalDate,
    enabled: Boolean,
    onDatePick: (LocalDate) -> Unit,
) {
    val context = LocalContext.current
    ClickableTextField(
        value = date.format(),
        onClick = {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    onDatePick(LocalDate.of(year, month + 1, dayOfMonth))
                },
                date.year,
                date.monthValue - 1,
                date.dayOfMonth,
            ).apply {
                datePicker.maxDate = Instant.now().toEpochMilli()
            }.show()
        },
        modifier = Modifier.weight(1f),
        enabled = enabled
    )
}