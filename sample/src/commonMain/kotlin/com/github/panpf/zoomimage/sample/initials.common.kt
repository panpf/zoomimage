package com.github.panpf.zoomimage.sample

import coil3.ImageLoader
import coil3.util.DebugLogger
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.util.Logger
import com.github.panpf.zoomimage.sample.data.api.pexels.PexelsApi
import com.github.panpf.zoomimage.sample.ui.gallery.LocalPhotoListViewModel
import com.github.panpf.zoomimage.sample.ui.gallery.PexelsPhotoListViewModel
import com.github.panpf.zoomimage.sample.util.ignoreFirst
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect fun initialApp(context: PlatformContext)

fun commonModule(context: PlatformContext): Module = module {
    single { AppSettings(context) }
    single { AppEvents() }
    single { newSketch(context, appSettings = get()) }
    single { newHttpClient() }
    single { PexelsApi(get()) }
    viewModelOf(::LocalPhotoListViewModel)
    viewModelOf(::PexelsPhotoListViewModel)
}

expect fun platformModule(context: PlatformContext): Module

private fun newSketch(context: PlatformContext, appSettings: AppSettings): Sketch {
    return Sketch(context) {
        // For print the Sketch initialization log
        val loggerLevel = if (appSettings.debugLog.value) Logger.Level.Debug else Logger.Level.Info
        val logger = Logger(level = loggerLevel).apply {
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch {
                appSettings.debugLog.ignoreFirst().collect { debugLog ->
                    level = if (debugLog) Logger.Level.Debug else Logger.Level.Info
                }
            }
        }
        logger(logger)

        platformSketchInitial(context)
    }
}

expect fun Sketch.Builder.platformSketchInitial(context: PlatformContext)

fun newCoil(context: coil3.PlatformContext): ImageLoader {
    return ImageLoader.Builder(context).apply {
        platformCoilInitial(context)
        logger(DebugLogger())
    }.build()
}

expect fun ImageLoader.Builder.platformCoilInitial(context: coil3.PlatformContext)

private fun newHttpClient(): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }
}