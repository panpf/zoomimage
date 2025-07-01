package com.github.panpf.zoomimage.sample

import android.annotation.SuppressLint
import android.util.Log
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sample.image.CoilComposeResourceUriFetcher
import com.github.panpf.zoomimage.sample.image.CoilComposeResourceUriKeyer
import com.github.panpf.zoomimage.sample.image.PicassoComposeResourceRequestHandler
import com.github.panpf.zoomimage.sample.util.ignoreFirst
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

actual fun initialApp(context: PlatformContext) {
    startKoin {
        modules(commonModule(context))
        modules(platformModule(context))
    }

    handleSSLHandshake()

    Picasso.setSingletonInstance(Picasso.Builder(context).apply {
        addRequestHandler(PicassoComposeResourceRequestHandler())
        loggingEnabled(true)
    }.build())

    Glide.init(context, GlideBuilder().setLogLevel(Log.DEBUG))

    val appSettings: AppSettings = KoinPlatform.getKoin().get()
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

actual fun platformModule(context: PlatformContext): Module = module {

}

actual fun Sketch.Builder.platformSketchInitial(context: PlatformContext) {

}

actual fun ImageLoader.Builder.platformCoilInitial(context: coil3.PlatformContext) {
    components {
        add(CoilComposeResourceUriFetcher.Factory())
        add(CoilComposeResourceUriKeyer())
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

private fun onToggleImageLoader(context: PlatformContext, sketch: Sketch, newImageLoader: String) {
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
            val cacheField = Picasso::class.java.getDeclaredField("cache")
            cacheField.isAccessible = true
            val cache = cacheField.get(picasso) as? com.squareup.picasso.Cache
            cache?.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}