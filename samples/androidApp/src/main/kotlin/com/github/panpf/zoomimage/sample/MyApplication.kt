package com.github.panpf.zoomimage.sample

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sample.image.PicassoComposeResourceRequestHandler
import com.github.panpf.zoomimage.sample.ui.ZoomImageSettingsViewModel
import com.github.panpf.zoomimage.sample.ui.gallery.CaptureViewModel
import com.github.panpf.zoomimage.sample.ui.gallery.PhotoPaletteViewModel
import com.github.panpf.zoomimage.sample.ui.test.ImageSwitchViewModel
import com.github.panpf.zoomimage.sample.util.ignoreFirst
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MyApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        handleSSLHandshake()

        initialApp(this@MyApplication) {
            modules(viewModule())
        }

        val context = this@MyApplication
        val appSettings: AppSettings = KoinPlatform.getKoin().get()
        val logLevel = appSettings.imageLoaderLogLevel.value.toLogLevel()

        Picasso.setSingletonInstance(Picasso.Builder(context).apply {
            addRequestHandler(PicassoComposeResourceRequestHandler())
            loggingEnabled(logLevel <= Log.DEBUG)
        }.build())

        // TODO Glide support compose resources
        Glide.init(context, GlideBuilder().setLogLevel(logLevel))

        val sketch: Sketch = KoinPlatform.getKoin().get()
        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch(Dispatchers.Main) {
            appSettings.viewImageLoader.ignoreFirst().collect {
                onToggleImageLoader(context, sketch, it)
            }
        }
        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch(Dispatchers.Main) {
            appSettings.composeImageLoader.ignoreFirst().collect {
                onToggleImageLoader(context, sketch, it)
            }
        }
        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch(Dispatchers.Main) {
            appSettings.composePage.ignoreFirst().collect {
                val newImageLoader = if (it) {
                    appSettings.composeImageLoader.value
                } else {
                    appSettings.viewImageLoader.value
                }
                onToggleImageLoader(context, sketch, newImageLoader)
            }
        }
    }

    private fun viewModule(): Module = module {
        viewModelOf(::PhotoPaletteViewModel)
        viewModelOf(::ZoomImageSettingsViewModel)
        viewModelOf(::CaptureViewModel)
        viewModelOf(::ImageSwitchViewModel)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return newCoil(context)
    }

    private fun onToggleImageLoader(
        context: PlatformContext,
        sketch: Sketch,
        newImageLoader: String
    ) {
        Log.d("ZoomImage", "Switch image loader to $newImageLoader")

        if (newImageLoader != "Sketch" && newImageLoader != "Basic") {
            Log.d("ZoomImage", "Clean Sketch memory cache")
            sketch.memoryCache.clear()
        }
        if (newImageLoader != "Coil") {
            Log.d("ZoomImage", "Clean Coil memory cache")
            SingletonImageLoader.get(context).memoryCache?.clear()
        }
        if (newImageLoader != "Glide") {
            Log.d("ZoomImage", "Clean Glide memory cache")
            Glide.get(context).clearMemory()
        }
        if (newImageLoader != "Picasso") {
            Log.d("ZoomImage", "Clean Picasso memory cache")
            val picasso = Picasso.get()
            try {
                Picasso::class.java.getDeclaredField("cache")
                    .apply { isAccessible = true }
                    .get(picasso)
                    .let { it as? com.squareup.picasso.Cache }
                    ?.clear()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * for api.pexels.com on Android 5.0
     */
    private fun handleSSLHandshake() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate?> {
                        return arrayOfNulls(0)
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(
                        certs: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(
                        certs: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }
                })
            val sc = SSLContext.getInstance("TLS")
            // trustAllCerts trust all certificates
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun com.github.panpf.sketch.util.Logger.Level.toLogLevel(): Int {
    return when (this) {
        com.github.panpf.sketch.util.Logger.Level.Verbose -> Log.VERBOSE
        com.github.panpf.sketch.util.Logger.Level.Debug -> Log.DEBUG
        com.github.panpf.sketch.util.Logger.Level.Info -> Log.INFO
        com.github.panpf.sketch.util.Logger.Level.Warn -> Log.WARN
        com.github.panpf.sketch.util.Logger.Level.Error -> Log.ERROR
        com.github.panpf.sketch.util.Logger.Level.Assert -> Log.ASSERT
    }
}