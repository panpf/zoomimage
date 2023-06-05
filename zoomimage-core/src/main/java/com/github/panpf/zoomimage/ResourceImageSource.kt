package com.github.panpf.zoomimage

import android.content.Context
import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
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
        return kotlin.runCatching {
            resources.openRawResource(drawableId)
        }
    }
}