package com.mtsvetkova.currencyapp.ui.converter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mtsvetkova.currencyapp.R
import com.mtsvetkova.currencyapp.ui.ShowSnackbarLaunchedEffect
import com.mtsvetkova.currencyapp.ui.TopAppBar

@Composable
fun ConverterRoute(viewModel: ConverterViewModel = viewModel(), onNavigateBack: () -> Unit) {
    val state = viewModel.state.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    Screen(
        state = state,
        snackbarHostState = snackbarHostState,
        onCurrencyFromValueChange = viewModel::setCurrency1,
        onCurrencyToChange = viewModel::setCurrency2,
        onExchangeClick = viewModel::exchange,
        onBackClick = onNavigateBack,
    )
    if (state.exchangeResult?.isSuccess == true) LaunchedEffect(Unit) { onNavigateBack() }
    if (state.currency1Value?.isFailure == true ||
        state.currency2Value?.isFailure == true
    ) {
        ShowSnackbarLaunchedEffect(snackbarHostState)
    }
    if (state.exchangeResult?.isFailure == true) {
        ShowSnackbarLaunchedEffect(snackbarHostState, R.string.exchange_error)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Screen(
    state: ConverterViewModel.State,
    snackbarHostState: SnackbarHostState,
    onCurrencyFromValueChange: (String) -> Unit,
    onCurrencyToChange: (String) -> Unit,
    onExchangeClick: () -> Unit,
    onBackClick: () -> Unit
) = Scaffold(
    topBar = { TopAppBar(R.string.title_converter, onBackClick) },
    snackbarHost = { SnackbarHost(snackbarHostState) }
) { padding ->
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
    ) {
        TextField(state.currency1Value, state.currency1Code, onCurrencyFromValueChange)
        TextField(state.currency2Value, state.currency2Code, onCurrencyToChange)
        Spacer(Modifier.height(16.dp))
        Button(onExchangeClick, Modifier.fillMaxWidth(), enabled = state.exchangeAvailable) {
            Text(stringResource(R.string.exchange))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextField(value: Result<String>?, label: String?, onValueChange: (String) -> Unit) =
    TextField(
        value = value?.getOrNull().orEmpty(),
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = value != null,
        label = if (label != null) {
            { Text(label) }
        } else null,
        trailingIcon = if (value == null) {
            { CircularProgressIndicator(Modifier.size(24.dp)) }
        } else if (value.isFailure) {
            { Icon(painterResource(R.drawable.baseline_error_24), null) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )