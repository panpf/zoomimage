package com.github.panpf.zoomimage.sample

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.multidex.MultiDexApplication
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.SketchFactory
import com.github.panpf.sketch.cache.internal.LruMemoryCache
import com.github.panpf.sketch.util.Logger
import com.github.panpf.sketch.util.Logger.Level.DEBUG
import com.github.panpf.sketch.util.Logger.Level.INFO
import com.github.panpf.zoomimage.sample.util.getMaxAvailableMemoryCacheBytes
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import com.tencent.mmkv.MMKV
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class MyApplication : MultiDexApplication(), SketchFactory, ImageLoaderFactory {

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)

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

    override fun createSketch(): Sketch {
        return Sketch.Builder(this)
            .logger(Logger(if (BuildConfig.DEBUG) DEBUG else INFO))
            .memoryCache(LruMemoryCache(maxSize = getMemoryCacheMaxSize()))
            .build()
    }

    private fun getMemoryCacheMaxSize(): Long {
        // 集成了四个图片加载器所以要把内存缓存分成四份
        val imageLoaderCount = 4
        return getMaxAvailableMemoryCacheBytes() / imageLoaderCount
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .logger(DebugLogger())
            .memoryCache(
                MemoryCache.Builder(this).apply {
                    maxSizeBytes(getMemoryCacheMaxSize().toInt())
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