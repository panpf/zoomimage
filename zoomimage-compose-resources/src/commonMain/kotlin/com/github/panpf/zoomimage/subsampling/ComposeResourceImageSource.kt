package com.github.panpf.zoomimage.subsampling

import okio.Buffer
import okio.Source
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

/**
 * Create an image source from a compose resource path.
 */
fun ImageSource.Companion.fromComposeResource(
    resourcePath: String,
): ComposeResourceImageSource {
    return ComposeResourceImageSource(resourcePath)
}

/**
 * Image source for compose resources.
 * @param resourcePath The path of the file to read in the compose resource's directory. For example:
 * * 'composeResources/sketch_root.sample.generated.resources/drawable/sample.png'
 * * Res.getUri("drawable/sample.png")
 */
class ComposeResourceImageSource(
    val resourcePath: String,
) : ImageSource {

    override val key: String = "compose.resource://$resourcePath"

    @OptIn(InternalResourceApi::class)
    override suspend fun openSource(): Result<Source> = kotlin.runCatching {
        val realResourcePath = parseComposeResourcePath(resourcePath)
        val bytes = readResourceBytes(realResourcePath)
        Buffer().write(bytes)
    }

    override fun toString(): String {
        return "ComposeResourceImageSource($resourcePath)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ComposeResourceImageSource
        return resourcePath == other.resourcePath
    }

    override fun hashCode(): Int {
        return resourcePath.hashCode()
    }
}

/**
 * Sample: 'compose.resource://composeResources/sketch_root.sample.generated.resources/drawable/sample.png'
 *
 * @param resourcePath The path of the file to read in the compose resource's directory. For example:
 * * 'composeResources/sketch_root.sample.generated.resources/drawable/sample.png'
 * * Res.getUri("drawable/sample.png")
 */
fun parseComposeResourcePath(resourcePath: String): String {
    // "composeResources/sketch_root.sample.generated.resources/drawable/sample.png"
    if (resourcePath.startsWith("composeResources/")) {
        return resourcePath
    }

    // file:/Users/panpf/Workspace/sketch/sample/build/processedResources/desktop/main/composeResources/sketch_root.sample.generated.resources/drawable/sample.png
    // jar:file:/data/app/com.github.panpf.sketch4.sample-kz2o4eobaLdvBww0SkguMw==/base.apk!/composeResources/sketch_root.sample.generated.resources/drawable/sample.png
    val index = resourcePath.indexOf("/composeResources/")
    if (index != -1) {
        val realResourcePath = resourcePath.substring(index + 1)
        return realResourcePath
    }

    throw IllegalArgumentException("Unsupported compose resource path: $resourcePath")
}