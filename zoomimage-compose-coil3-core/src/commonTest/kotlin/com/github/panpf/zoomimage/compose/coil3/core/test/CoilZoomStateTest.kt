package com.github.panpf.zoomimage.compose.coil3.core.test

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.SuccessResult
import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.coil.internal.AnimatableCoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.coil.internal.EngineCoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.util.Logger.Level.Debug
import com.github.panpf.zoomimage.util.Logger.Level.Info
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CoilZoomStateTest {

    @Test
    fun testRememberCoilZoomState() = runComposeUiTest {
        setContent {
            TestLifecycle {
                val zoomState1 = rememberCoilZoomState()
                assertEquals(
                    expected = "CoilZoomAsyncImage",
                    actual = zoomState1.logger.tag
                )
                assertEquals(
                    expected = listOf(
                        AnimatableCoilComposeSubsamplingImageGenerator(),
                        EngineCoilComposeSubsamplingImageGenerator()
                    ).joinToString { it::class.toString() },
                    actual = zoomState1.subsamplingImageGenerators.joinToString { it::class.toString() }
                )

                val modelToImageSources = remember {
                    listOf(TestCoilComposeSubsamplingImageGenerator()).toImmutableList()
                }
                val zoomState2 = rememberCoilZoomState(
                    subsamplingImageGenerators = modelToImageSources,
                )
                assertEquals(
                    expected = listOf(
                        TestCoilComposeSubsamplingImageGenerator(),
                        AnimatableCoilComposeSubsamplingImageGenerator(),
                        EngineCoilComposeSubsamplingImageGenerator()
                    ).joinToString { it::class.toString() },
                    actual = zoomState2.subsamplingImageGenerators.joinToString { it::class.toString() }
                )

                assertEquals(
                    expected = Info,
                    actual = zoomState1.logger.level
                )
                val zoomState3 = rememberCoilZoomState(logLevel = Debug)
                assertEquals(
                    expected = Debug,
                    actual = zoomState3.logger.level
                )
            }
        }
    }

    class TestCoilComposeSubsamplingImageGenerator : CoilComposeSubsamplingImageGenerator {

        override suspend fun generateImage(
            context: PlatformContext,
            imageLoader: ImageLoader,
            result: SuccessResult,
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
            return "TestCoilComposeSubsamplingImageGenerator"
        }
    }
}