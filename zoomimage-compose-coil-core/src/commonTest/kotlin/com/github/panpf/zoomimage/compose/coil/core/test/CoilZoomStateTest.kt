package com.github.panpf.zoomimage.compose.coil.core.test

import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import coil3.ImageLoader
import coil3.PlatformContext
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.coil.CoilModelToImageSourceImpl
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.github.panpf.zoomimage.subsampling.ImageSource.Factory
import com.github.panpf.zoomimage.test.TestLifecycle
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
                    expected = listOf(CoilModelToImageSourceImpl()).joinToString { it::class.qualifiedName!! },
                    actual = zoomState1.modelToImageSources.joinToString { it::class.qualifiedName!! }
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
                    ).joinToString { it::class.qualifiedName!! },
                    actual = zoomState2.modelToImageSources.joinToString { it::class.qualifiedName!! }
                )
            }
        }
    }

    class TestCoilModelToImageSource : CoilModelToImageSource {

        override suspend fun modelToImageSource(
            context: PlatformContext,
            imageLoader: ImageLoader,
            model: Any
        ): Factory? {
            return null
        }
    }
}