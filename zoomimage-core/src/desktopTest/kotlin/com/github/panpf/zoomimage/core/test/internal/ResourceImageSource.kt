package com.github.panpf.zoomimage.core.test.internal

import com.github.panpf.zoomimage.subsampling.ImageSource
import java.io.InputStream

/**
 * Create an image source from a resource id.
 */
fun ImageSource.Companion.fromResource(
    resourcePath: String,
    resourcesLoader: ResourceLoader = ResourceLoader.Default,
): ResourceImageSource {
    return ResourceImageSource(resourcePath, resourcesLoader)
}

class ResourceImageSource(
    val resourcePath: String,
    val resourcesLoader: ResourceLoader = ResourceLoader.Default,
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