package com.github.panpf.zoomimage

import android.content.Context
import java.io.InputStream

class AssetImageSource(val context: Context, val assetFileName: String) : ImageSource {

    override val key: String = "asset://$assetFileName"

    override suspend fun openInputStream(): Result<InputStream> {
        return kotlin.runCatching {
            context.assets.open(assetFileName)
        }
    }
}