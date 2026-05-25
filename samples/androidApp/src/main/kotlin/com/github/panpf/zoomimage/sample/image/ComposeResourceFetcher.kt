package com.github.panpf.zoomimage.sample.image

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.github.panpf.sketch.util.toUri
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import java.io.IOException
import java.io.InputStream

class ComposeResourceFetcher(private val uriString: String) : DataFetcher<InputStream> {

    private var inputStream: InputStream? = null

    @OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        try {
            val data = uriString.toUri()
            val resourcePath = data.pathSegments.drop(1).joinToString("/")
            val bytes = runBlocking {
                readResourceBytes(resourcePath)
            }
            inputStream = bytes.inputStream()
            callback.onDataReady(inputStream)
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }

    override fun cleanup() {
        try {
            inputStream?.close()
        } catch (e: IOException) {
            // Ignore
        }
    }

    override fun cancel() {

    }

    override fun getDataClass(): Class<InputStream> = InputStream::class.java

    override fun getDataSource(): DataSource = DataSource.LOCAL
}