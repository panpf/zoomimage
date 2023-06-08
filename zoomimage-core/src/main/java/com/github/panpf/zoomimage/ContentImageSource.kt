package com.github.panpf.zoomimage

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.InputStream

class ContentImageSource(val context: Context, val uri: Uri) : ImageSource {

    override val key: String = uri.toString()

    override suspend fun openInputStream(): Result<InputStream> {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                context.contentResolver.openInputStream(uri)
                    ?: throw FileNotFoundException("Unable to open stream. uri='$uri'")
            }
        }
    }
}