package com.github.panpf.zoomimage.sample

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.multidex.MultiDexApplication
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.util.DebugLogger
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.LruMemoryCache
import com.github.panpf.sketch.decode.supportAnimatedGif
import com.github.panpf.sketch.decode.supportMovieGif
import com.github.panpf.sketch.fetch.supportComposeResources
import com.github.panpf.sketch.util.Logger
import com.github.panpf.zoomimage.sample.util.getMaxAvailableMemoryCacheBytes
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class MyApplication : MultiDexApplication(), SingletonSketch.Factory, SingletonImageLoader.Factory {

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        handleSSLHandshake()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Picasso.setSingletonInstance(Picasso.Builder(this).apply {
                loggingEnabled(true)
                memoryCache(LruCache(getMemoryCacheMaxSize().toInt()))
            }.build())
        }

        Glide.init(
            this,
            GlideBuilder()
                .setMemoryCache(LruResourceCache(getMemoryCacheMaxSize()))
                .setLogLevel(Log.DEBUG)
        )
    }

    override fun createSketch(context: PlatformContext): Sketch {
        return Sketch.Builder(this).apply {
            logger(level = if (BuildConfig.DEBUG) Logger.Level.Debug else Logger.Level.Info)
            memoryCache(LruMemoryCache(maxSize = getMemoryCacheMaxSize()))
            components {
                supportComposeResources()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    supportAnimatedGif()
                } else {
                    supportMovieGif()
                }
            }
        }.build()
    }

    private fun getMemoryCacheMaxSize(): Long {
        // Four image loaders are integrated, so the memory cache must be divided into four parts.
        val imageLoaderCount = 4
        return getMaxAvailableMemoryCacheBytes() / imageLoaderCount
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(this)
            .logger(DebugLogger())
            .memoryCache(
                MemoryCache.Builder().apply {
                    maxSizeBytes(getMemoryCacheMaxSize())
                }.build()
            )
            .build()
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
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}