package com.mtsvetkova.currencyapp.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.mtsvetkova.currencyapp.R
import com.mtsvetkova.currencyapp.ui.analytics.AnalyticsRoute
import com.mtsvetkova.currencyapp.ui.converter.ConverterRoute
import com.mtsvetkova.currencyapp.ui.currencies.CurrenciesRoute
import com.mtsvetkova.currencyapp.ui.history.ExchangesRoute
import com.mtsvetkova.currencyapp.ui.history.FiltersRoute

private const val ROUTE_EXCHANGE = "exchange"
private const val ROUTE_CURRENCIES = "currencies"
private const val ROUTE_CONVERTER = "converter"
private const val ARG_CURRENCY_1 = "currency_1"
private const val ARG_CURRENCY_2 = "currency_2"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_EXCHANGES = "exchanges"
private const val ROUTE_FILTERS = "filters"
private const val ROUTE_ANALYTICS = "analytics"

@Composable
fun NavHost(navController: NavHostController, modifier: Modifier = Modifier) =
    NavHost(navController, ROUTE_EXCHANGE, modifier) {
        exchangeGraph(navController)
        historyGraph(navController)
        composable(ROUTE_ANALYTICS) { AnalyticsRoute() }
    }

private fun NavGraphBuilder.exchangeGraph(navController: NavController) =
    navigation(startDestination = ROUTE_CURRENCIES, route = ROUTE_EXCHANGE) {
        composable(ROUTE_CURRENCIES) {
            CurrenciesRoute(
                onNavigateToConverter = { code1, code2 ->
                    navController.navigate(
                        buildString {
                            append("$ROUTE_CONVERTER/$code1")
                            if (code2 != null) append("?$ARG_CURRENCY_2=$code2")
                        }
                    )
                }
            )
        }
        composable("$ROUTE_CONVERTER/{$ARG_CURRENCY_1}?$ARG_CURRENCY_2={$ARG_CURRENCY_2}") {
            ConverterRoute(onNavigateBack = navController::popBackStack)
        }
    }

private fun NavGraphBuilder.historyGraph(navController: NavController) =
    navigation(ROUTE_EXCHANGES, ROUTE_HISTORY) {
        composable(ROUTE_EXCHANGES) {
            ExchangesRoute(onNavigateToFilters = { navController.navigate(ROUTE_FILTERS) })
        }
        composable(ROUTE_FILTERS) {
            FiltersRoute(
                exchangesViewModel = viewModel(remember { navController.previousBackStackEntry!! }),
                onNavigateBack = navController::popBackStack
            )
        }
    }

fun SavedStateHandle.extractCurrency1Code(): String = this[ARG_CURRENCY_1]!!
fun SavedStateHandle.extractCurrency2Code(): String? = this[ARG_CURRENCY_2]


enum class TopLevelDestination(
    val route: String,
    @DrawableRes val iconId: Int,
    @StringRes val titleId: Int,
) {
    EXCHANGE(
        ROUTE_EXCHANGE,
        R.drawable.baseline_currency_exchange_24,
        R.string.top_destination_exchange
    ),
    HISTORY(
        ROUTE_HISTORY,
        R.drawable.baseline_history_24,
        R.string.top_destination_history,
    ),
    ANALYTICS(
        ROUTE_ANALYTICS,
        R.drawable.baseline_show_chart_24,
        R.string.top_destination_analytics,
    )
}