package com.github.panpf.zoomimage.compose.glide.test

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import com.bumptech.glide.Glide
import com.github.panpf.zoomimage.glide.GlideSubsamplingImageGenerator
import com.github.panpf.zoomimage.glide.internal.AnimatableGlideSubsamplingImageGenerator
import com.github.panpf.zoomimage.glide.internal.EngineGlideSubsamplingImageGenerator
import com.github.panpf.zoomimage.rememberGlideZoomState
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.util.Logger
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class GlideZoomStateTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun testRememberGlideZoomState() {
        rule.setContent {
            TestLifecycle {
                val zoomState1 = rememberGlideZoomState()
                assertEquals(
                    expected = "GlideZoomAsyncImage",
                    actual = zoomState1.logger.tag
                )
                assertEquals(
                    expected = listOf(
                        AnimatableGlideSubsamplingImageGenerator(),
                        EngineGlideSubsamplingImageGenerator()
                    ).joinToString { it::class.toString() },
                    actual = zoomState1.subsamplingImageGenerators.joinToString { it::class.toString() }
                )

                val modelToImageSources = remember {
                    listOf(TestGlideSubsamplingImageGenerator()).toImmutableList()
                }
                val zoomState2 = rememberGlideZoomState(
                    subsamplingImageGenerators = modelToImageSources,
                )
                assertEquals(
                    expected = listOf(
                        TestGlideSubsamplingImageGenerator(),
                        AnimatableGlideSubsamplingImageGenerator(),
                        EngineGlideSubsamplingImageGenerator()
                    ).joinToString { it::class.toString() },
                    actual = zoomState2.subsamplingImageGenerators.joinToString { it::class.toString() }
                )

                assertEquals(
                    expected = Logger.Level.Info,
                    actual = zoomState1.logger.level
                )
                val zoomState3 = rememberGlideZoomState(logLevel = Logger.Level.Debug)
                assertEquals(
                    expected = Logger.Level.Debug,
                    actual = zoomState3.logger.level
                )
            }
        }
    }

    class TestGlideSubsamplingImageGenerator : GlideSubsamplingImageGenerator {

        override suspend fun generateImage(
            context: Context,
            glide: Glide,
            model: Any,
            drawable: Drawable
        ): SubsamplingImageGenerateResult? {
            return null
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String {
            return "TestGlideSubsamplingImageGenerator"
        }
    }
}