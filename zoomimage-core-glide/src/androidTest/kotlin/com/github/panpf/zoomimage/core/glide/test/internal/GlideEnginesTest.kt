package com.github.panpf.zoomimage.core.glide.test.internal

import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.createGlideEngine
import com.bumptech.glide.load.engine.internalDiskCache
import com.bumptech.glide.load.engine.newEngineKey
import kotlin.test.Test
import kotlin.test.assertNotNull

class GlideEnginesTest {

    @Test
    fun testInternalDiskCache() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        assertNotNull(glide.internalDiskCache)
    }

    @Test
    fun testNewEngineKey() {
        newEngineKey("key1")
    }

    @Test
    fun testCreateGlideEngine() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        assertNotNull(createGlideEngine(glide))
    }
}