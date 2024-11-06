package com.github.panpf.zoomimage.compose.coil2.core.test

import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.test.junit4.createComposeRule
import coil.ImageLoader
import coil.request.SuccessResult
import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.coil.internal.AnimatableCoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.coil.internal.EngineCoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.util.Logger
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class CoilZoomStateTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun testRememberCoilZoomState() {
        rule.setContent {
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
                val subsamplingImageGenerators = remember {
                    listOf(TestCoilComposeSubsamplingImageGenerator()).toImmutableList()
                }
                val zoomState2 = rememberCoilZoomState(
                    subsamplingImageGenerators = subsamplingImageGenerators,
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
                    expected = Logger.Level.Info,
                    actual = zoomState1.logger.level
                )
                val zoomState3 = rememberCoilZoomState(logLevel = Logger.Level.Debug)
                assertEquals(
                    expected = Logger.Level.Debug,
                    actual = zoomState3.logger.level
                )
            }
        }
    }

    class TestCoilComposeSubsamplingImageGenerator : CoilComposeSubsamplingImageGenerator {

        override suspend fun generateImage(
            context: Context,
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