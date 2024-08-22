package com.github.panpf.zoomimage.test.sketch

import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.SketchSingleton
import com.github.panpf.sketch.fetch.AssetUriFetcher
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.request.ImageRequest

object Sketchs {

    init {
        SketchSingleton.setSketch {
            val context = InstrumentationRegistry.getInstrumentation().context
            Sketch.Builder(context).apply {
                components {
                    addFetcher(NewAssetUriFetcher.Factory())
                }
            }.build()
        }
    }

    fun sketch(): Sketch {
        val context = InstrumentationRegistry.getInstrumentation().context
        return SketchSingleton.sketch(context)
    }

    class NewAssetUriFetcher(
        val sketch: Sketch,
        val request: ImageRequest,
        val fileName: String
    ) : Fetcher {

        companion object {
            const val SCHEME = "file"
            const val PATH_ROOT = "android_asset"

            /**
             * Check if the uri is a android asset uri
             *
             * Support 'file:///android_asset/test.png' uri
             */
            fun isAssetUri(uri: Uri): Boolean =
                SCHEME.equals(uri.scheme, ignoreCase = true)
                        && uri.authority?.takeIf { it.isNotEmpty() } == null
                        && PATH_ROOT.equals(uri.pathSegments.firstOrNull(), ignoreCase = true)
        }

        @WorkerThread
        override suspend fun fetch(): Result<FetchResult> = kotlin.runCatching {
            return AssetUriFetcher(sketch, request, fileName).fetch()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as NewAssetUriFetcher
            if (sketch != other.sketch) return false
            if (request != other.request) return false
            if (fileName != other.fileName) return false
            return true
        }

        override fun hashCode(): Int {
            var result = sketch.hashCode()
            result = 31 * result + request.hashCode()
            result = 31 * result + fileName.hashCode()
            return result
        }

        override fun toString(): String {
            return "AssetUriFetcher('$fileName')"
        }

        class Factory : Fetcher.Factory {

            override fun create(sketch: Sketch, request: ImageRequest): NewAssetUriFetcher? {
                val uri = request.uriString.toUri()
                if (!isAssetUri(uri)) return null
                val fileName = uri.pathSegments.drop(1).joinToString("/")
                return NewAssetUriFetcher(sketch = sketch, request = request, fileName = fileName)
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                return other != null && this::class == other::class
            }

            override fun hashCode(): Int {
                return this::class.hashCode()
            }

            override fun toString(): String = "AssetUriFetcher"
        }
    }
}