package com.github.panpf.zoomimage.compose.sketch4.core.test

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.zoomimage.compose.sketch.SketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.sketch.internal.AnimatableSketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.sketch.internal.EngineSketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.util.Logger.Level.Debug
import com.github.panpf.zoomimage.util.Logger.Level.Info
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SketchZoomStateTest {

    @Test
    fun testRememberSketchZoomState() = runComposeUiTest {
        setContent {
            TestLifecycle {
                val zoomState1 = rememberSketchZoomState()
                assertEquals(
                    expected = "SketchZoomAsyncImage",
                    actual = zoomState1.logger.tag
                )

                assertEquals(
                    expected = listOf(
                        AnimatableSketchComposeSubsamplingImageGenerator(),
                        EngineSketchComposeSubsamplingImageGenerator()
                    ).joinToString { it::class.toString() },
                    actual = zoomState1.subsamplingImageGenerators.joinToString { it::class.toString() }
                )
                val subsamplingImageGenerators = remember {
                    listOf(TestSketchComposeSubsamplingImageGenerator()).toImmutableList()
                }
                val zoomState2 = rememberSketchZoomState(
                    subsamplingImageGenerators = subsamplingImageGenerators,
                )
                assertEquals(
                    expected = listOf(
                        TestSketchComposeSubsamplingImageGenerator(),
                        AnimatableSketchComposeSubsamplingImageGenerator(),
                        EngineSketchComposeSubsamplingImageGenerator()
                    ).joinToString { it::class.toString() },
                    actual = zoomState2.subsamplingImageGenerators.joinToString { it::class.toString() }
                )

                assertEquals(
                    expected = Info,
                    actual = zoomState1.logger.level
                )
                val zoomState3 = rememberSketchZoomState(logLevel = Debug)
                assertEquals(
                    expected = Debug,
                    actual = zoomState3.logger.level
                )
            }
        }
    }

    class TestSketchComposeSubsamplingImageGenerator :
        SketchComposeSubsamplingImageGenerator {

        override suspend fun generateImage(
            sketch: Sketch,
            result: ImageResult.Success,
            painter: Painter
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
            return "TestSketchComposeSubsamplingImageGenerator"
        }
    }
}