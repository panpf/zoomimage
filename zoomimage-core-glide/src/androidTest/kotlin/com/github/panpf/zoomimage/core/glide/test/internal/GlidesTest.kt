package com.github.panpf.zoomimage.core.glide.test.internal

import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.internalGlideContext
import com.bumptech.glide.internalModel
import com.bumptech.glide.internalRequestOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.SingleRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class GlidesTest {

    @Test
    fun testSingleRequestInternalRequestOptions() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val request = SingleRequest.obtain<Any>(
            /* context = */ context,
            /* glideContext = */ glide.internalGlideContext,
            /* requestLock = */ Any(),
            /* model = */ "https://sample.com/sample.jpeg",
            /* transcodeClass = */ null,
            /* requestOptions = */ RequestOptions.sizeMultiplierOf(0.5f),
            /* overrideWidth = */ 0,
            /* overrideHeight = */ 0,
            /* priority = */ Priority.HIGH,
            /* target = */ null,
            /* targetListener = */ null,
            /* requestListeners = */ null,
            /* requestCoordinator = */ null,
            /* engine = */ null,
            /* animationFactory = */ null,
            /* callbackExecutor = */ null,
        )
        assertEquals(
            expected = RequestOptions.sizeMultiplierOf(0.5f),
            actual = request.internalRequestOptions
        )
    }

    @Test
    fun testSingleRequestInternalModel() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val request = SingleRequest.obtain<Any>(
            /* context = */ context,
            /* glideContext = */ glide.internalGlideContext,
            /* requestLock = */ Any(),
            /* model = */ "https://sample.com/sample.jpeg",
            /* transcodeClass = */ null,
            /* requestOptions = */ RequestOptions.sizeMultiplierOf(0.5f),
            /* overrideWidth = */ 0,
            /* overrideHeight = */ 0,
            /* priority = */ Priority.HIGH,
            /* target = */ null,
            /* targetListener = */ null,
            /* requestListeners = */ null,
            /* requestCoordinator = */ null,
            /* engine = */ null,
            /* animationFactory = */ null,
            /* callbackExecutor = */ null,
        )
        assertEquals(
            expected = "https://sample.com/sample.jpeg",
            actual = request.internalModel
        )
    }

    @Test
    fun testRequestBuilderInternalModel() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val builder = Glide.with(context).load("https://sample.com/sample.jpeg")
        assertEquals(
            expected = "https://sample.com/sample.jpeg",
            actual = builder.internalModel
        )
    }

    @Test
    fun testInternalGlideContext() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        glide.internalGlideContext
    }
}