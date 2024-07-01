package com.github.panpf.zoomimage.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScaleTransition
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.supportSkiaAnimatedWebp
import com.github.panpf.sketch.decode.supportSkiaGif
import com.github.panpf.sketch.decode.supportSvg
import com.github.panpf.sketch.fetch.supportComposeResources
import com.github.panpf.sketch.http.KtorStack
import com.github.panpf.sketch.util.Logger
import com.github.panpf.zoomimage.sample.ui.HomeScreen
import com.github.panpf.zoomimage.sample.ui.theme.AppTheme
import com.github.panpf.zoomimage.sample.util.PexelsCompatibleRequestInterceptor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {
    initialSketch()
    application {
        val coroutineScope = rememberCoroutineScope()
        Window(
            title = "ZoomImage",
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(size = DpSize(1000.dp, 800.dp)),
            onKeyEvent = {
                coroutineScope.launch {
                    EventBus.keyEvent.emit(it)
                }
                false
            }
        ) {
            AppTheme {
                Box(Modifier.fillMaxSize()) {
                    Navigator(HomeScreen) { navigator ->
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
    }
}

fun initialSketch() {
    val context = PlatformContext.INSTANCE
    val appSettings = context.appSettings
    SingletonSketch.setSafe {
        Sketch.Builder(it).apply {
            httpStack(KtorStack())
            components {
                supportSvg()
                supportSkiaGif()
                supportSkiaAnimatedWebp()
                supportComposeResources()

                addRequestInterceptor(PexelsCompatibleRequestInterceptor())
            }

            // To be able to print the Sketch initialization log
            logger(level = if (appSettings.debugLog.value) Logger.Level.Debug else Logger.Level.Info)
        }.build().apply {
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch {
                appSettings.debugLog.collect { debugLog ->
                    logger.level = if (debugLog) Logger.Level.Debug else Logger.Level.Info
                }
            }
        }
    }
}