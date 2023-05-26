package com.mtsvetkova.currencyapp.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mtsvetkova.currencyapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyApplication() {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val topLevelDestinations = listOf(
        TopLevelDestination.EXCHANGE,
        TopLevelDestination.HISTORY,
        TopLevelDestination.ANALYTICS,
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                topLevelDestinations.forEach { topLevelDestination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy
                            ?.any { it.route == topLevelDestination.route } == true,
                        onClick = {
                            navController.navigate(topLevelDestination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(topLevelDestination.iconId),
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(topLevelDestination.titleId)) }
                    )
                }
            }
        }
    ) {
        NavHost(navController, Modifier.padding(it))
    }
}