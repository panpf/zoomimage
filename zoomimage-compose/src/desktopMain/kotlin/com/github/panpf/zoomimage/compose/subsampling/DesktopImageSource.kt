package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.ResourceLoader
import com.github.panpf.zoomimage.subsampling.ImageSource
import java.io.InputStream

/**
 * Create an image source from a resource id.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun ImageSource.Companion.fromResource(
    resourcesLoader: ResourceLoader = ResourceLoader.Default,
    resourcePath: String
): ResourceImageSource {
    return ResourceImageSource(resourcesLoader, resourcePath)
}

@OptIn(ExperimentalComposeUiApi::class)
class ResourceImageSource(
    val resourcesLoader: ResourceLoader = ResourceLoader.Default,
    val resourcePath: String
) : ImageSource {

    override val key: String = resourcePath

    override fun openInputStream(): Result<InputStream> = kotlin.runCatching {
        return Result.success(resourcesLoader.load(resourcePath))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ResourceImageSource
        if (resourcesLoader != other.resourcesLoader) return false
        if (resourcePath != other.resourcePath) return false
        return true
    }

    override fun hashCode(): Int {
        var result = resourcesLoader.hashCode()
        result = 31 * result + resourcePath.hashCode()
        return result
    }

    override fun toString(): String {
        return "ResourceImageSource($resourcePath)"
    }
}