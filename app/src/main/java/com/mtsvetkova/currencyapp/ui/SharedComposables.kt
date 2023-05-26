package com.mtsvetkova.currencyapp.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mtsvetkova.currencyapp.R

@Composable
fun ShowSnackbarLaunchedEffect(
    snackbarHostState: SnackbarHostState,
    @StringRes messageId: Int = R.string.error,
    onDisappear: () -> Unit = {},
) {
    val message = stringResource(messageId)
    LaunchedEffect(snackbarHostState) { snackbarHostState.showSnackbar(message) }
    onDisappear()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    @StringRes titleId: Int,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) = TopAppBar(
    title = { Text(stringResource(titleId)) },
    navigationIcon = if (onBackClick != null) {
        { IconButton(onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }
    } else {
        {}
    },
    actions = actions
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableTextField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    TextField(
        value,
        onValueChange = {},
        modifier,
        enabled = enabled,
        readOnly = true,
        interactionSource = interactionSource
    )
    if (interactionSource.collectIsPressedAsState().value) onClick()
}