package com.github.panpf.zoomimage.sample.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScaleTransition
import com.github.panpf.zoomimage.sample.EventBus
import com.github.panpf.zoomimage.sample.ui.theme.AppTheme

expect val homeScreen: Screen

@Composable
fun App() {
    AppTheme {
        Box(Modifier.fillMaxSize()) {
            Navigator(homeScreen) { navigator ->
                ScaleTransition(navigator = navigator)
            }

            val snackbarHostState = remember { SnackbarHostState() }
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
            )
            LaunchedEffect(Unit) {
                EventBus.toastFlow.collect {
                    snackbarHostState.showSnackbar(it)
                }
            }
        }
    }
}