package com.github.panpf.zoomimage.subsampling.internal

import java.io.InputStream

/**
 * Resource loader based on JVM current context class loader.
 *
 * Copy from jetbrains compose androidx.compose.ui.res.Resources.desktop.kt
 *
 * @see com.github.panpf.zoomimage.core.desktop.test.subsampling.internal.ClassLoaderResourceLoaderTest
 */
internal class ClassLoaderResourceLoader {

    companion object {
        val Default = ClassLoaderResourceLoader()
    }

    fun load(resourcePath: String): InputStream {
        // TODO(https://github.com/JetBrains/compose-jb/issues/618): probably we shouldn't use
        //  contextClassLoader here, as it is not defined in threads created by non-JVM
        val contextClassLoader = Thread.currentThread().contextClassLoader!!
        val resource = contextClassLoader.getResourceAsStream(resourcePath)
            ?: (::ClassLoaderResourceLoader.javaClass).getResourceAsStream(resourcePath)
        return requireNotNull(resource) { "Resource $resourcePath not found" }
    }
}