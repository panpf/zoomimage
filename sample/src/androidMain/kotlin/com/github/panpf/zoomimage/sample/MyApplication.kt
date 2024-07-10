package com.github.panpf.zoomimage.sample

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sample.image.PicassoComposeResourceRequestHandler
import com.github.panpf.zoomimage.sample.util.ignoreFirst
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class MyApplication : Application(), SingletonSketch.Factory, SingletonImageLoader.Factory {

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        handleSSLHandshake()

        Picasso.setSingletonInstance(Picasso.Builder(this).apply {
            addRequestHandler(PicassoComposeResourceRequestHandler())
            loggingEnabled(true)
        }.build())

        Glide.init(
            this,
            GlideBuilder().setLogLevel(Log.DEBUG)
        )

        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch(Dispatchers.Main) {
            appSettings.viewImageLoader.ignoreFirst().collect {
                onToggleImageLoader(it)
            }
        }
        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch(Dispatchers.Main) {
            appSettings.composeImageLoader.ignoreFirst().collect {
                onToggleImageLoader(it)
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
                onToggleImageLoader(newImageLoader)
            }
        }
    }

    override fun createSketch(context: PlatformContext): Sketch {
        return newSketch(context)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return newCoil(context)
    }

    private fun handleSSLHandshake() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
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

    private fun onToggleImageLoader(newImageLoader: String) {
        Log.d("ZoomImage", "Switch image loader to $newImageLoader")

        if (newImageLoader != "Sketch" && newImageLoader != "Basic") {
            Log.d("ZoomImage", "Clean Sketch memory cache")
            SingletonSketch.get(this).memoryCache.clear()
        }
        if (newImageLoader != "Coil") {
            Log.d("ZoomImage", "Clean Coil memory cache")
            SingletonImageLoader.get(this).memoryCache?.clear()
        }
        if (newImageLoader != "Glide") {
            Log.d("ZoomImage", "Clean Glide memory cache")
            Glide.get(this).clearMemory()
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
}