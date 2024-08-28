/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("PackageDirectoryMismatch")

package com.bumptech.glide.load.engine

import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.engine.cache.DiskCacheAdapter
import com.bumptech.glide.load.engine.cache.MemoryCache
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.request.RequestOptions
import java.io.File

/**
 * Get the disk cache from Glide
 *
 * @see com.github.panpf.zoomimage.core.glide.test.internal.GlideEnginesTest.testInternalDiskCache
 */
internal val Glide.internalDiskCache: DiskCache?
    get() {
        return try {
            val engine = javaClass.getDeclaredField("engine")
                .apply { isAccessible = true }
                .get(this) as Engine
            val diskCacheProvider = engine.javaClass.getDeclaredField("diskCacheProvider")
                .apply { isAccessible = true }
                .get(engine) as DecodeJob.DiskCacheProvider
            diskCacheProvider.diskCache?.takeIf { it !is DiskCacheAdapter }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

/**
 * Create a new EngineKey instance
 *
 * @see com.github.panpf.zoomimage.core.glide.test.internal.GlideEnginesTest.testNewEngineKey
 */
internal fun newEngineKey(key: String): EngineKey {
    val options = RequestOptions()
    return EngineKey(
        key,
        options.signature,
        0,
        0,
        options.transformations,
        options.resourceClass,
        File::class.java,
        options.options
    )
}

/**
 * Create a GlideEngine instance from Glide
 *
 * @see com.github.panpf.zoomimage.core.glide.test.internal.GlideEnginesTest.testCreateGlideEngine
 */
internal fun createGlideEngine(glide: Glide): GlideEngine? {
    return try {
        val engine = glide.javaClass.getDeclaredField("engine")
            .apply { isAccessible = true }
            .get(glide) as Engine
        val cache = engine.javaClass.getDeclaredField("cache")
            .apply { isAccessible = true }
            .get(engine) as MemoryCache
        val activeResources = engine.javaClass.getDeclaredField("activeResources")
            .apply { isAccessible = true }
            .get(engine) as ActiveResources
        GlideEngine(glide, engine, cache, activeResources)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

internal class EngineResourceWrapper(private val engineResource: EngineResource<Bitmap>) {

    val bitmap: Bitmap
        get() = engineResource.get()

    fun setIsDisplayed(displayed: Boolean) {
        if (displayed) {
            engineResource.acquire()
        } else {
            engineResource.release()
        }
    }
}

internal class GlideEngine(
    private val glide: Glide,
    private val engine: Engine,
    private val cache: MemoryCache,
    private val activeResources: ActiveResources,
) {

    fun loadFromMemory(
        key: EngineKey, isMemoryCacheable: Boolean
    ): EngineResourceWrapper? {
        if (!isMemoryCacheable) {
            return null
        }
        val active = loadFromActiveResources(key)
        if (active != null) {
            @Suppress("UNCHECKED_CAST")
            return EngineResourceWrapper(active as EngineResource<Bitmap>)
        }
        val cached = loadFromCache(key)
        if (cached != null) {
            @Suppress("UNCHECKED_CAST")
            return EngineResourceWrapper(cached as EngineResource<Bitmap>)
        }
        return null
    }

    private fun loadFromActiveResources(key: Key): EngineResource<*>? {
        return activeResources.get(key)
    }

    private fun loadFromCache(key: Key): EngineResource<*>? {
        val cached = getEngineResourceFromCache(key)
        if (cached != null) {
            activeResources.activate(key, cached)
        }
        return cached
    }

    private fun getEngineResourceFromCache(key: Key): EngineResource<*>? {
        val result: EngineResource<*>? = when (val cached: Resource<*>? = cache.remove(key)) {
            null -> {
                null
            }

            is EngineResource<*> -> {
                // Save an object allocation if we've cached an EngineResource (the typical case).
                cached
            }

            else -> {
                EngineResource(
                    cached,  /* isMemoryCacheable= */
                    true,  /* isRecyclable= */
                    true,
                    key,  /* listener= */
                    engine
                )
            }
        }
        return result
    }

    fun put(bitmap: Bitmap, key: Key): EngineResource<Bitmap> {
        val engineResource = newEngineResource(bitmap, key)
        activeResources.activate(key, engineResource)
        return engineResource
    }

    fun newEngineResource(bitmap: Bitmap, key: Key): EngineResource<Bitmap> {
        val resource = BitmapResource(bitmap, glide.bitmapPool)
        return EngineResource(resource, true, true, key, engine)
    }
}