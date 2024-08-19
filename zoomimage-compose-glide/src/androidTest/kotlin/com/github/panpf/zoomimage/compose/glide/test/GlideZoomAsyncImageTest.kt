package com.github.panpf.zoomimage.compose.glide.test

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.GlideZoomAsyncImage
import com.github.panpf.zoomimage.GlideZoomState
import com.github.panpf.zoomimage.compose.glide.ExperimentalGlideComposeApi
import com.github.panpf.zoomimage.rememberGlideZoomState
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.test.waitMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GlideZoomAsyncImageTest {

    @Test
    @OptIn(ExperimentalTestApi::class, ExperimentalGlideComposeApi::class)
    fun testGlideZoomAsyncImage() {
        runComposeUiTest {
            var zoomStateHolder: GlideZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberGlideZoomState()
                            .apply { zoomStateHolder = this }
                        GlideZoomAsyncImage(
                            model = ResourceImages.hugeChina.uri,
                            contentDescription = "",
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
            var zoomStateHolder: GlideZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberGlideZoomState()
                            .apply { zoomStateHolder = this }
                        GlideZoomAsyncImage(
                            model = ResourceImages.hugeChina.uri,
                            contentDescription = "",
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
                expected = "500 x 359",
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
                expected = "IntRect.fromLTRB(0, 141, 500, 500)",
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
    }
}