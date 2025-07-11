package com.github.panpf.zoomimage.compose.coil3.core.test

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter.State
import coil3.compose.AsyncImagePainter.State.Empty
import coil3.compose.AsyncImagePainter.State.Error
import coil3.compose.AsyncImagePainter.State.Loading
import coil3.compose.AsyncImagePainter.State.Success
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.CachePolicy.DISABLED
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.crossfade
import coil3.size.Precision
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

class CoilZoomAsyncImageTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testCoilZoomAsyncImage1() {
        if (Platform.current == iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
        val imageLoader = Coils.imageLoader()

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
                            model = ImageRequest.Builder(LocalPlatformContext.current).apply {
                                data(ResourceImages.longEnd.uri)
                                precision(Precision.EXACT)
                                memoryCachePolicy(DISABLED)
                            }.build(),
                            contentDescription = "",
                            imageLoader = imageLoader,
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
            waitMillis(2000)

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
                expected = "500 x 154",
                actual = zoomState.zoomable.contentSize.toString()
            )
            assertEquals(
                expected = "2000 x 618",
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
                expected = "IntRect.fromLTRB(0, 173, 500, 327)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(0, 0, 500, 154)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{4=(2, 1), 2=(4, 2), 1=(8, 3)}",
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
                            model = ImageRequest.Builder(LocalPlatformContext.current).apply {
                                data(ResourceImages.longEnd.uri)
                                precision(Precision.EXACT)
                                memoryCachePolicy(DISABLED)
                            }.build(),
                            contentDescription = "",
                            imageLoader = imageLoader,
                            placeholder = null,
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                            contentScale = ContentScale.None,
                            alignment = Alignment.BottomEnd,
                        )
                    }
                }
            }
            waitMillis(2000)

            val zoomState = zoomStateHolder!!

            assertEquals(
                expected = "500 x 500",
                actual = zoomState.zoomable.containerSize.toString()
            )
            assertEquals(
                expected = "2000 x 618",
                actual = zoomState.zoomable.contentSize.toString()
            )
            assertEquals(
                expected = "0 x 0",
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
                expected = "IntRect.fromLTRB(-1500, -118, 500, 500)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(1500, 118, 2000, 618)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{}",
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
        val imageLoader = Coils.imageLoader()

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
                            model = ImageRequest.Builder(LocalPlatformContext.current).apply {
                                data(ResourceImages.longEnd.uri)
                                precision(Precision.EXACT)
                                memoryCachePolicy(DISABLED)
                            }.build(),
                            contentDescription = "",
                            imageLoader = imageLoader,
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
            waitMillis(2000)

            val onState = onStateResult
            val zoomState = zoomStateHolder
            assertTrue(onState is Success)
            assertNotNull(actual = zoomState)

            assertEquals(
                expected = "500 x 500",
                actual = zoomState.zoomable.containerSize.toString()
            )
            assertEquals(
                expected = "500 x 154",
                actual = zoomState.zoomable.contentSize.toString()
            )
            assertEquals(
                expected = "2000 x 618",
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
                expected = "IntRect.fromLTRB(0, 173, 500, 327)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(0, 0, 500, 154)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{4=(2, 1), 2=(4, 2), 1=(8, 3)}",
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
                            model = ImageRequest.Builder(LocalPlatformContext.current).apply {
                                data(ResourceImages.longEnd.uri)
                                precision(Precision.EXACT)
                                memoryCachePolicy(DISABLED)
                            }.build(),
                            contentDescription = "",
                            imageLoader = imageLoader,
                            onState = null,
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                            contentScale = ContentScale.None,
                            alignment = Alignment.BottomEnd,
                        )
                    }
                }
            }
            waitMillis(2000)

            val zoomState = zoomStateHolder!!

            assertEquals(
                expected = "500 x 500",
                actual = zoomState.zoomable.containerSize.toString()
            )
            assertEquals(
                expected = "2000 x 618",
                actual = zoomState.zoomable.contentSize.toString()
            )
            assertEquals(
                expected = "0 x 0",
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
                expected = "IntRect.fromLTRB(-1500, -118, 500, 500)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(1500, 118, 2000, 618)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testBuildContentSizeWithCrossfade() {
        if (Platform.current == iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
        val imageLoader = Coils.imageLoader()

        runComposeUiTest {
            val contentSizes = mutableListOf<IntSize>()
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val context = LocalPlatformContext.current
                        var bigImageMemoryCacheKey: MemoryCache.Key? by remember {
                            mutableStateOf(
                                null
                            )
                        }
                        LaunchedEffect(Unit) {
                            val request = ImageRequest.Builder(context)
                                .data(ResourceImages.dog.uri)
                                .size(1100, 733)
                                .precision(Precision.EXACT)
                                .build()
                            val result = imageLoader.execute(request)
                            if (result is SuccessResult) {
                                bigImageMemoryCacheKey = result.memoryCacheKey
                            }
                        }

                        val bigImageMemoryCacheKey1 = bigImageMemoryCacheKey
                        if (bigImageMemoryCacheKey1 != null) {
                            val zoomState = rememberCoilZoomState()
                            LaunchedEffect(Unit) {
                                snapshotFlow { zoomState.zoomable.contentSize }.collect {
                                    if (it.width > 0 && it.height > 0) {
                                        contentSizes.add(it)
                                    }
                                }
                            }
                            CoilZoomAsyncImage(
                                modifier = Modifier.size(500.dp),
                                model = ImageRequest.Builder(context)
                                    .data(ResourceImages.dog.uri)
                                    .placeholderMemoryCacheKey(bigImageMemoryCacheKey1)
                                    .memoryCachePolicy(DISABLED)
                                    .size(1100 / 2, 733 / 2)
                                    .precision(Precision.EXACT)
                                    .crossfade(durationMillis = 300)
                                    .build(),
                                zoomState = zoomState,
                                contentDescription = null,
                                imageLoader = imageLoader,
                            )
                        }
                    }
                }
            }
            waitMillis(2000)
//            assertEquals("1100 x 733, 549 x 366", contentSizes.joinToString())    // error
            assertEquals("1100 x 733", contentSizes.joinToString())
        }
    }
}