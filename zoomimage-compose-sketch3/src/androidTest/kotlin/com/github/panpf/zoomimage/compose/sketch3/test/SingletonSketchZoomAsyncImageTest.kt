package com.github.panpf.zoomimage.compose.sketch3.test

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
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.compose.AsyncImageState
import com.github.panpf.sketch.compose.rememberAsyncImageState
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.LoadState
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.SketchZoomState
import com.github.panpf.zoomimage.rememberSketchZoomState
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.test.sketch.Sketchs
import com.github.panpf.zoomimage.test.waitMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SingletonSketchZoomAsyncImageTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testSketchZoomAsyncImage1() {
        Sketchs.sketch()

        runComposeUiTest {
            var zoomStateHolder: SketchZoomState? = null
            var stateHolder: AsyncImageState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberSketchZoomState()
                            .apply { zoomStateHolder = this }
                        val state = rememberAsyncImageState()
                            .apply { stateHolder = this }
                        SketchZoomAsyncImage(
                            imageUri = ResourceImages.hugeChina.uri,
                            contentDescription = "",
                            placeholder = null,
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                            state = state,
                        )
                    }
                }
            }
            waitMillis(1000)

            val zoomState = zoomStateHolder
            val state = stateHolder
            assertNotNull(actual = zoomState)
            assertNotNull(actual = state)

            assertTrue(
                actual = state.loadState is LoadState.Success,
                message = "state.loadState is ${state.loadState}"
            )

            assertEquals(
                expected = "500 x 500",
                actual = zoomState.zoomable.containerSize.toString()
            )
            assertEquals(
                expected = "425 x 305",
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
                expected = "IntRect.fromLTRB(0, 0, 425, 305)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{16=(2, 2), 8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }

        // contentScale, alignment
        runComposeUiTest {
            var zoomStateHolder: SketchZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberSketchZoomState()
                            .apply { zoomStateHolder = this }
                        SketchZoomAsyncImage(
                            imageUri = ResourceImages.hugeChina.uri,
                            contentDescription = "",
                            modifier = Modifier.size(500.dp),
                            placeholder = null,
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
                expected = "425 x 305",
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
                expected = "IntRect.fromLTRB(75, 195, 500, 500)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(0, 0, 425, 305)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{16=(2, 2), 8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testSketchZoomAsyncImage2() {
        Sketchs.sketch()

        runComposeUiTest {
            var zoomStateHolder: SketchZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberSketchZoomState()
                            .apply { zoomStateHolder = this }
                        SketchZoomAsyncImage(
                            request = DisplayRequest(
                                LocalContext.current,
                                ResourceImages.hugeChina.uri
                            ),
                            contentDescription = "",
                            placeholder = null,
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                        )
                    }
                }
            }
            waitMillis(1000)

            val zoomState = zoomStateHolder
            assertNotNull(actual = zoomState)

            assertEquals(
                expected = "500 x 500",
                actual = zoomState.zoomable.containerSize.toString()
            )
            assertEquals(
                expected = "425 x 305",
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
                expected = "IntRect.fromLTRB(0, 0, 425, 305)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{16=(2, 2), 8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }

        // contentScale, alignment
        runComposeUiTest {
            var zoomStateHolder: SketchZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberSketchZoomState()
                            .apply { zoomStateHolder = this }
                        SketchZoomAsyncImage(
                            request = DisplayRequest(
                                LocalContext.current,
                                ResourceImages.hugeChina.uri
                            ),
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
                expected = "425 x 305",
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
                expected = "IntRect.fromLTRB(75, 195, 500, 500)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(0, 0, 425, 305)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{16=(2, 2), 8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testSketchZoomAsyncImage3() {
        Sketchs.sketch()

        runComposeUiTest {
            var zoomStateHolder: SketchZoomState? = null
            var stateHolder: AsyncImageState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberSketchZoomState()
                            .apply { zoomStateHolder = this }
                        val state = rememberAsyncImageState()
                            .apply { stateHolder = this }
                        SketchZoomAsyncImage(
                            imageUri = ResourceImages.hugeChina.uri,
                            contentDescription = "",
                            onPainterState = null,
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                            state = state,
                        )
                    }
                }
            }
            waitMillis(1000)

            val zoomState = zoomStateHolder
            val state = stateHolder
            assertNotNull(actual = zoomState)
            assertNotNull(actual = state)

            assertTrue(
                actual = state.loadState is LoadState.Success,
                message = "state.loadState is ${state.loadState}"
            )

            assertEquals(
                expected = "500 x 500",
                actual = zoomState.zoomable.containerSize.toString()
            )
            assertEquals(
                expected = "425 x 305",
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
                expected = "IntRect.fromLTRB(0, 0, 425, 305)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{16=(2, 2), 8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }

        // contentScale, alignment
        runComposeUiTest {
            var zoomStateHolder: SketchZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberSketchZoomState()
                            .apply { zoomStateHolder = this }
                        SketchZoomAsyncImage(
                            imageUri = ResourceImages.hugeChina.uri,
                            contentDescription = "",
                            onPainterState = null,
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
                expected = "425 x 305",
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
                expected = "IntRect.fromLTRB(75, 195, 500, 500)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(0, 0, 425, 305)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{16=(2, 2), 8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testSketchZoomAsyncImage4() {
        Sketchs.sketch()

        runComposeUiTest {
            var zoomStateHolder: SketchZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberSketchZoomState()
                            .apply { zoomStateHolder = this }
                        SketchZoomAsyncImage(
                            request = DisplayRequest(
                                LocalContext.current,
                                ResourceImages.hugeChina.uri
                            ),
                            contentDescription = "",
                            onPainterState = null,
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                        )
                    }
                }
            }
            waitMillis(1000)

            val zoomState = zoomStateHolder
            assertNotNull(actual = zoomState)

            assertEquals(
                expected = "500 x 500",
                actual = zoomState.zoomable.containerSize.toString()
            )
            assertEquals(
                expected = "425 x 305",
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
                expected = "IntRect.fromLTRB(0, 0, 425, 305)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{16=(2, 2), 8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }

        // contentScale, alignment
        runComposeUiTest {
            var zoomStateHolder: SketchZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberSketchZoomState()
                            .apply { zoomStateHolder = this }
                        SketchZoomAsyncImage(
                            request = DisplayRequest(
                                LocalContext.current,
                                ResourceImages.hugeChina.uri
                            ),
                            contentDescription = "",
                            onPainterState = null,
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
                expected = "425 x 305",
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
                expected = "IntRect.fromLTRB(75, 195, 500, 500)",
                actual = zoomState.zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = "IntRect.fromLTRB(0, 0, 425, 305)",
                actual = zoomState.zoomable.contentVisibleRect.toString()
            )
            assertEquals(
                expected = "{16=(2, 2), 8=(4, 3), 4=(7, 5), 2=(14, 10), 1=(28, 20)}",
                actual = zoomState.subsampling.tileGridSizeMap.toString()
            )
        }
    }
}