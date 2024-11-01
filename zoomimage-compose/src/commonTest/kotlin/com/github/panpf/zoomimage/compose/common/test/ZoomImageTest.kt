package com.github.panpf.zoomimage.compose.common.test

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.test.Platform
import com.github.panpf.zoomimage.test.SizeColorPainter
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.test.current
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.test.waitMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ZoomImageTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun testZoomImage() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }

        runComposeUiTest {
            var zoomStateHolder: ZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberZoomState()
                            .apply { zoomStateHolder = this }
                        ZoomImage(
                            painter = SizeColorPainter(Color.Cyan, Size(425f, 305f)),
                            contentDescription = "",
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                        )
                        LaunchedEffect(Unit) {
                            zoomState.setSubsamplingImage(ResourceImages.hugeChina.toImageSource())
                        }
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
            var zoomStateHolder: ZoomState? = null
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    TestLifecycle {
                        val zoomState = rememberZoomState()
                            .apply { zoomStateHolder = this }
                        ZoomImage(
                            painter = SizeColorPainter(Color.Cyan, Size(425f, 305f)),
                            contentDescription = "",
                            modifier = Modifier.size(500.dp),
                            zoomState = zoomState,
                            contentScale = ContentScale.None,
                            alignment = Alignment.BottomEnd,
                        )
                        LaunchedEffect(Unit) {
                            zoomState.setSubsamplingImage(ResourceImages.hugeChina.toImageSource())
                        }
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