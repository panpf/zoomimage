package com.github.panpf.zoomimage.compose.coil2.core.test

import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import coil.ImageLoader
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.coil.CoilModelToImageSourceImpl
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.github.panpf.zoomimage.subsampling.ImageSource.Factory
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
                    expected = listOf(CoilModelToImageSourceImpl()).joinToString { it::class.toString() },
                    actual = zoomState1.modelToImageSources.joinToString { it::class.toString() }
                )

                val modelToImageSources = remember {
                    listOf(TestCoilModelToImageSource()).toImmutableList()
                }
                val zoomState2 = rememberCoilZoomState(
                    modelToImageSources = modelToImageSources,
                )
                assertEquals(
                    expected = listOf(
                        TestCoilModelToImageSource(),
                        CoilModelToImageSourceImpl()
                    ).joinToString { it::class.toString() },
                    actual = zoomState2.modelToImageSources.joinToString { it::class.toString() }
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

    class TestCoilModelToImageSource : CoilModelToImageSource {

        override suspend fun modelToImageSource(
            context: Context,
            imageLoader: ImageLoader,
            model: Any
        ): Factory? {
            return null
        }
    }
}