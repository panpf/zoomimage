package com.github.panpf.zoomimage.compose.coil2.test

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter.State
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.CoilZoomState
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.test.coil.Coils
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
        Coils.imageLoader()

        runComposeUiTest {
            var onLoadingResultHolder: State.Loading? = null
            var onSuccessResultHolder: State.Success? = null
            var onErrorResultHolder: State.Error? = null
            var zoomStateHolder: CoilZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberCoilZoomState()
                            .apply { zoomStateHolder = this }
                        CoilZoomAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).apply {
                                data(ResourceImages.hugeChina.uri)
                                precision(Precision.EXACT)
                                memoryCachePolicy(CachePolicy.DISABLED)
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
                        val zoomState = rememberCoilZoomState()
                            .apply { zoomStateHolder = this }
                        CoilZoomAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).apply {
                                data(ResourceImages.hugeChina.uri)
                                precision(Precision.EXACT)
                                memoryCachePolicy(CachePolicy.DISABLED)
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
                expected = "696 x 501",
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
                expected = "IntRect.fromLTRB(-196, -1, 500, 500)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(196, 1, 696, 501)",
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
        Coils.imageLoader()

        runComposeUiTest {
            var onStateResult: State? = null
            var zoomStateHolder: CoilZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberCoilZoomState()
                            .apply { zoomStateHolder = this }
                        CoilZoomAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).apply {
                                data(ResourceImages.hugeChina.uri)
                                precision(Precision.EXACT)
                                memoryCachePolicy(CachePolicy.DISABLED)
                            }.build(),
                            contentDescription = "",
                            onState = { state ->
                                onStateResult = when (state) {
                                    is State.Loading -> state
                                    is State.Success -> state
                                    is State.Error -> state
                                    is State.Empty -> state
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
            assertTrue(onState is State.Success)
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
                        val zoomState = rememberCoilZoomState()
                            .apply { zoomStateHolder = this }
                        CoilZoomAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).apply {
                                data(ResourceImages.hugeChina.uri)
                                precision(Precision.EXACT)
                                memoryCachePolicy(CachePolicy.DISABLED)
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
                expected = "696 x 501",
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
                expected = "IntRect.fromLTRB(-196, -1, 500, 500)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(196, 1, 696, 501)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }
    }
}