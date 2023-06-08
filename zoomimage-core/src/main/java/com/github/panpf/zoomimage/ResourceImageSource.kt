package com.github.panpf.zoomimage

import android.content.Context
import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class ResourceImageSource(
    val resources: Resources,
    @RawRes @DrawableRes val drawableId: Int
) : ImageSource {

    constructor(
        context: Context,
        @RawRes @DrawableRes drawableId: Int
    ) : this(context.resources, drawableId)

    override val key: String = "android.resources://$drawableId"

    override suspend fun openInputStream(): Result<InputStream> {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                resources.openRawResource(drawableId)
            }
        }
    }
}