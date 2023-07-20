package com.github.panpf.zoomimage.sketch.internal

import android.content.Context
import androidx.annotation.WorkerThread
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.datasource.BasedStreamDataSource
import com.github.panpf.sketch.request.Depth
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.zoomimage.subsampling.ImageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class SketchImageSource(
    private val context: Context,
    private val sketch: Sketch,
    private val imageUri: String,
) : ImageSource {

    override val key: String = imageUri

    @WorkerThread
    override suspend fun openInputStream(): Result<InputStream> {
        val request = LoadRequest(context, imageUri) {
            downloadCachePolicy(CachePolicy.ENABLED)
            depth(Depth.LOCAL)   // Do not download image, by default go here The image have been downloaded
        }
        val fetcher = try {
            sketch.components.newFetcherOrThrow(request)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val fetchResult = withContext(Dispatchers.IO) {
            fetcher.fetch()
        }.let {
            it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!)
        }
        val dataSource = fetchResult.dataSource
        if (dataSource !is BasedStreamDataSource) {
            return Result.failure(IllegalStateException("DataSource is not BasedStreamDataSource. imageUri='$imageUri'"))
        }
        return kotlin.runCatching { dataSource.newInputStream() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SketchImageSource
        if (context != other.context) return false
        if (sketch != other.sketch) return false
        if (imageUri != other.imageUri) return false
        return true
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        result = 31 * result + sketch.hashCode()
        result = 31 * result + imageUri.hashCode()
        return result
    }

    override fun toString(): String {
        return "SketchImageSource('$imageUri')"
    }
}