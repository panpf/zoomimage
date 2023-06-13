package com.github.panpf.zoomimage.imagesource

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class AssetImageSource(val context: Context, val assetFileName: String) : ImageSource {

    override val key: String = "asset://$assetFileName"

    override suspend fun openInputStream(): Result<InputStream> {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                context.assets.open(assetFileName)
            }
        }
    }
}