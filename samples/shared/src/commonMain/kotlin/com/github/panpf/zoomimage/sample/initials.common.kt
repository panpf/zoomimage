package com.github.panpf.zoomimage.sample

import coil3.ImageLoader
import coil3.util.DebugLogger
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sample.data.api.pexels.PexelsApi
import com.github.panpf.zoomimage.sample.ui.gallery.LocalPhotoListViewModel
import com.github.panpf.zoomimage.sample.ui.gallery.PexelsPhotoListViewModel
import com.github.panpf.zoomimage.sample.ui.test.OverlayTestViewModel
import com.github.panpf.zoomimage.sample.util.ignoreFirst
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

expect fun initialApp(context: PlatformContext, koinAppDeclaration: KoinAppDeclaration? = null)

fun commonModule(context: PlatformContext): Module = module {
    single { AppSettings(context) }
    single { AppEvents() }
    single { newSketch(context, appSettings = get()) }
    single { newHttpClient() }
    single { PexelsApi(get()) }
    viewModelOf(::LocalPhotoListViewModel)
    viewModelOf(::PexelsPhotoListViewModel)
    viewModelOf(::OverlayTestViewModel)
}

expect fun platformModule(context: PlatformContext): Module

private fun newSketch(context: PlatformContext, appSettings: AppSettings): Sketch {
    return Sketch(context) {
        // For print the Sketch initialization log
        val loggerLevel = appSettings.imageLoaderLogLevel.value
        val logger = com.github.panpf.sketch.util.Logger(level = loggerLevel).apply {
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch {
                appSettings.imageLoaderLogLevel.ignoreFirst().collect {
                    level = it
                }
            }
        }
        logger(logger)

        platformSketchInitial(context)
    }
}

expect fun Sketch.Builder.platformSketchInitial(context: PlatformContext)

fun newCoil(context: coil3.PlatformContext): ImageLoader {
    val appSettings: AppSettings = KoinPlatform.getKoin().get()
    return ImageLoader.Builder(context).apply {
        platformCoilInitial(context)
        val loggerLevel = appSettings.imageLoaderLogLevel.value
        logger(DebugLogger(loggerLevel.toCoilLogLevel()).apply {
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch {
                appSettings.imageLoaderLogLevel.ignoreFirst().collect {
                    minLevel = it.toCoilLogLevel()
                }
            }
        })
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

fun com.github.panpf.sketch.util.Logger.Level.toCoilLogLevel(): coil3.util.Logger.Level {
    return when (this) {
        com.github.panpf.sketch.util.Logger.Level.Verbose -> coil3.util.Logger.Level.Verbose
        com.github.panpf.sketch.util.Logger.Level.Debug -> coil3.util.Logger.Level.Debug
        com.github.panpf.sketch.util.Logger.Level.Info -> coil3.util.Logger.Level.Info
        com.github.panpf.sketch.util.Logger.Level.Warn -> coil3.util.Logger.Level.Warn
        com.github.panpf.sketch.util.Logger.Level.Error -> coil3.util.Logger.Level.Error
        com.github.panpf.sketch.util.Logger.Level.Assert -> coil3.util.Logger.Level.Error
    }
}