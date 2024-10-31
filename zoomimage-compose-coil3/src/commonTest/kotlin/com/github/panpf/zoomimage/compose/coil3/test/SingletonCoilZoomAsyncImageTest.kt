package com.github.panpf.zoomimage.compose.coil3.test

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter.State
import coil3.compose.AsyncImagePainter.State.Empty
import coil3.compose.AsyncImagePainter.State.Error
import coil3.compose.AsyncImagePainter.State.Loading
import coil3.compose.AsyncImagePainter.State.Success
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy.DISABLED
import coil3.request.ImageRequest.Builder
import coil3.size.Precision.EXACT
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.CoilZoomState
import com.github.panpf.zoomimage.images.coil.platformComposeSubsamplingImageGenerators
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.github.panpf.zoomimage.test.Platform
import com.github.panpf.zoomimage.test.Platform.iOS
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.test.coil.Coils
import com.github.panpf.zoomimage.test.current
import com.github.panpf.zoomimage.test.waitMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SingletonCoilZoomAsyncImageTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testCoilZoomAsyncImage1() {
        if (Platform.current == iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
        Coils.imageLoader()

        runComposeUiTest {
            var onLoadingResultHolder: Loading? = null
            var onSuccessResultHolder: Success? = null
            var onErrorResultHolder: Error? = null
            var zoomStateHolder: CoilZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val subsamplingImageGenerators =
                            remember { platformComposeSubsamplingImageGenerators() }
                        val zoomState = rememberCoilZoomState(subsamplingImageGenerators)
                            .apply { zoomStateHolder = this }
                        CoilZoomAsyncImage(
                            model = Builder(LocalPlatformContext.current).apply {
                                data(ResourceImages.hugeChina.uri)
                                precision(EXACT)
                                memoryCachePolicy(DISABLED)
                            }.build(),
                            contentDescription = "",
                            placeholder = null,
                            onLoading = { onLoadingResultHolder = it },
                            onSuccess = { onSuccessResultHolder = it },
                            onError = { onErrorResultHolder = it },
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                        )
                    }
                }
            }
            waitMillis(1000)

            val onLoadingResult = onLoadingResultHolder
            val onSuccessResult = onSuccessResultHolder
            val onErrorResult = onErrorResultHolder
            val zoomState = zoomStateHolder
            assertNotNull(actual = onLoadingResult)
            assertNotNull(actual = onSuccessResult)
            assertNull(actual = onErrorResult)
            assertNotNull(actual = zoomState)

            assertEquals(
                expected = "500 x 500",
                actual = zoomState.zoomable.containerSize.toString()
            )
            assertEquals(
                expected = "500 x 359",
                actual = zoomState.zoomable.contentSize.toString()
            )
            assertEquals(
                expected = "6799 x 4882",
                actual = zoomState.zoomable.contentOriginSize.toString()
            )
            assertEquals(
                expected = ContentScale.Fit,
                actual = zoomState.zoomable.contentScale
            )
            assertEquals(
                expected = Alignment.Center,
                actual = zoomState.zoomable.alignment
            )
            assertEquals(
                expected = "IntRect.fromLTRB(0, 71, 500, 430)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(0, 0, 500, 359)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{16=(2, 2), 8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }

        // contentScale, alignment
        runComposeUiTest {
            var zoomStateHolder: CoilZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val subsamplingImageGenerators =
                            remember { platformComposeSubsamplingImageGenerators() }
                        val zoomState = rememberCoilZoomState(subsamplingImageGenerators)
                            .apply { zoomStateHolder = this }
                        CoilZoomAsyncImage(
                            model = Builder(LocalPlatformContext.current).apply {
                                data(ResourceImages.hugeChina.uri)
                                precision(EXACT)
                                memoryCachePolicy(DISABLED)
                            }.build(),
                            contentDescription = "",
                            placeholder = null,
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                            contentScale = ContentScale.None,
                            alignment = Alignment.BottomEnd,
                        )
                    }
                }
            }
            waitMillis(1000)

            val zoomState = zoomStateHolder!!

            assertEquals(
                expected = "500 x 500",
                actual = zoomState.zoomable.containerSize.toString()
            )
            assertEquals(
                expected = "696 x 500",
                actual = zoomState.zoomable.contentSize.toString()
            )
            assertEquals(
                expected = "6799 x 4882",
                actual = zoomState.zoomable.contentOriginSize.toString()
            )
            assertEquals(
                expected = ContentScale.None,
                actual = zoomState.zoomable.contentScale
            )
            assertEquals(
                expected = Alignment.BottomEnd,
                actual = zoomState.zoomable.alignment
            )
            assertEquals(
                expected = "IntRect.fromLTRB(-196, 0, 500, 500)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(196, 0, 696, 500)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testCoilZoomAsyncImage2() {
        if (Platform.current == iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
        Coils.imageLoader()

        runComposeUiTest {
            var onStateResult: State? = null
            var zoomStateHolder: CoilZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val subsamplingImageGenerators =
                            remember { platformComposeSubsamplingImageGenerators() }
                        val zoomState = rememberCoilZoomState(subsamplingImageGenerators)
                            .apply { zoomStateHolder = this }
                        CoilZoomAsyncImage(
                            model = Builder(LocalPlatformContext.current).apply {
                                data(ResourceImages.hugeChina.uri)
                                precision(EXACT)
                                memoryCachePolicy(DISABLED)
                            }.build(),
                            contentDescription = "",
                            onState = { state ->
                                onStateResult = when (state) {
                                    is Loading -> state
                                    is Success -> state
                                    is Error -> state
                                    is Empty -> state
                                }
                            },
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                        )
                    }
                }
            }
            waitMillis(1000)

            val onState = onStateResult
            val zoomState = zoomStateHolder
            assertTrue(onState is Success)
            assertNotNull(actual = zoomState)

            assertEquals(
                expected = "500 x 500",
                actual = zoomState.zoomable.containerSize.toString()
            )
            assertEquals(
                expected = "500 x 359",
                actual = zoomState.zoomable.contentSize.toString()
            )
            assertEquals(
                expected = "6799 x 4882",
                actual = zoomState.zoomable.contentOriginSize.toString()
            )
            assertEquals(
                expected = ContentScale.Fit,
                actual = zoomState.zoomable.contentScale
            )
            assertEquals(
                expected = Alignment.Center,
                actual = zoomState.zoomable.alignment
            )
            assertEquals(
                expected = "IntRect.fromLTRB(0, 71, 500, 430)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(0, 0, 500, 359)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{16=(2, 2), 8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }

        // contentScale, alignment
        runComposeUiTest {
            var zoomStateHolder: CoilZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val subsamplingImageGenerators =
                            remember { platformComposeSubsamplingImageGenerators() }
                        val zoomState = rememberCoilZoomState(subsamplingImageGenerators)
                            .apply { zoomStateHolder = this }
                        CoilZoomAsyncImage(
                            model = Builder(LocalPlatformContext.current).apply {
                                data(ResourceImages.hugeChina.uri)
                                precision(EXACT)
                                memoryCachePolicy(DISABLED)
                            }.build(),
                            contentDescription = "",
                            onState = null,
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                            contentScale = ContentScale.None,
                            alignment = Alignment.BottomEnd,
                        )
                    }
                }
            }
            waitMillis(1000)

            val zoomState = zoomStateHolder!!

            assertEquals(
                expected = "500 x 500",
                actual = zoomState.zoomable.containerSize.toString()
            )
            assertEquals(
                expected = "696 x 500",
                actual = zoomState.zoomable.contentSize.toString()
            )
            assertEquals(
                expected = "6799 x 4882",
                actual = zoomState.zoomable.contentOriginSize.toString()
            )
            assertEquals(
                expected = ContentScale.None,
                actual = zoomState.zoomable.contentScale
            )
            assertEquals(
                expected = Alignment.BottomEnd,
                actual = zoomState.zoomable.alignment
            )
            assertEquals(
                expected = "IntRect.fromLTRB(-196, 0, 500, 500)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(196, 0, 696, 500)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }
    }
}