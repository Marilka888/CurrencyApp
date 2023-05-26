package com.mtsvetkova.currencyapp.ui.currencies

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mtsvetkova.currencyapp.R
import com.mtsvetkova.currencyapp.data.storage.Currency
import com.mtsvetkova.currencyapp.ui.ShowSnackbarLaunchedEffect
import com.mtsvetkova.currencyapp.ui.TopAppBar

@Composable
fun CurrenciesRoute(
    onNavigateToConverter: (currencyCode1: String, currencyCode2: String?) -> Unit,
    viewModel: CurrenciesViewModel = viewModel()
) {
    val state = viewModel.state.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    Screen(
        state = state,
        snackbarHostState = snackbarHostState,
        onRefreshClick = viewModel::refresh,
        onCurrencyFavoriteClick = viewModel::toggleFavorite,
        onCurrencyClick = { clickedCurrency ->
            if (state.selectedCurrency != null) {
                onNavigateToConverter(state.selectedCurrency.code, clickedCurrency.code)
            } else {
                onNavigateToConverter(clickedCurrency.code, null)
            }
        },
        onCurrencyLongClick = viewModel::selectCurrency,
        onSelectedCurrencyClick = viewModel::deselectCurrency,
    )
    if (state.error) ShowSnackbarLaunchedEffect(snackbarHostState) { viewModel.clearError() }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun Screen(
    state: CurrenciesViewModel.State,
    snackbarHostState: SnackbarHostState,
    onRefreshClick: () -> Unit,
    onCurrencyFavoriteClick: (Currency) -> Unit,
    onCurrencyClick: (Currency) -> Unit,
    onCurrencyLongClick: (Currency) -> Unit,
    onSelectedCurrencyClick: () -> Unit,
) = Scaffold(
    topBar = { TopAppBar(R.string.title_currencies) },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    floatingActionButton = {
        FloatingActionButton(onRefreshClick) {
            if (state.loading) CircularProgressIndicator(Modifier.size(24.dp))
            else Icon(Icons.Default.Refresh, contentDescription = null)
        }
    }
) {
    Column(Modifier.padding(it)) {
        if (state.selectedCurrency != null) {
            Currency(
                currency = state.selectedCurrency,
                onFavoriteClick = onCurrencyFavoriteClick,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onSelectedCurrencyClick)
            )
        }
        LazyColumn {
            items(state.currencies, key = Currency::code) { currency ->
                Currency(
                    currency = currency,
                    onFavoriteClick = onCurrencyFavoriteClick,
                    modifier = Modifier
                        .animateItemPlacement()
                        .combinedClickable(
                            onClick = { onCurrencyClick(currency) },
                            onLongClick = { onCurrencyLongClick(currency) },
                        ),
                )
            }
        }
    }
}

@Composable
private fun Currency(
    currency: Currency,
    onFavoriteClick: (Currency) -> Unit,
    modifier: Modifier = Modifier,
) = Column(modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(currency.code)
        IconButton(onClick = { onFavoriteClick(currency) }) {
            Icon(
                painter = painterResource(
                    if (currency.favorite) R.drawable.baseline_star_24
                    else R.drawable.baseline_star_outline_24
                ),
                contentDescription = null
            )
        }
    }
    Divider()
}