package com.github.panpf.zoomimage.compose.common.test

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.toFactory
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.test.waitMillis
import com.github.panpf.zoomimage.util.Logger
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ZoomStateTest {

    @Test
    fun testRememberZoomState() = runComposeUiTest {
        setContent {
            TestLifecycle {
                val zoomState = rememberZoomState()
                assertEquals(
                    expected = "ZoomImage",
                    actual = zoomState.logger.tag
                )
                assertEquals(
                    expected = zoomState.logger,
                    actual = zoomState.zoomable.logger
                )
                assertEquals(
                    expected = zoomState.logger,
                    actual = zoomState.subsampling.logger
                )

                assertEquals(
                    expected = Logger.Level.Info,
                    actual = zoomState.logger.level
                )
                val zoomState2 = rememberZoomState(logLevel = Logger.Level.Debug)
                assertEquals(
                    expected = Logger.Level.Debug,
                    actual = zoomState2.logger.level
                )
            }
        }
    }

    @Test
    fun testSetImageSource() = runTest {
        val hugeCardImageSource = ComposeResImageFiles.hugeCard.toImageSource()

        runComposeUiTest {
            var zoomStateHolder: ZoomState? = null
            setContent {
                TestLifecycle {
                    rememberZoomState().apply {
                        zoomStateHolder = this
                    }
                }
            }
            waitMillis(500)

            val zoomState = zoomStateHolder!!
            assertEquals(expected = IntSize.Zero, actual = zoomState.zoomable.containerSize)
            assertEquals(expected = IntSize.Zero, actual = zoomState.zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomState.zoomable.contentOriginSize)
            assertFalse(actual = zoomState.subsampling.ready)
        }

        runComposeUiTest {
            val originSize = IntSize(7557, 5669)
            val bitmapSize = originSize.div(16)

            var zoomStateHolder: ZoomState? = null
            setContent {
                TestLifecycle {
                    val zoomState = rememberZoomState().apply {
                        zoomStateHolder = this
                    }
                    LaunchedEffect(Unit) {
                        zoomState.zoomable.setContainerSize(IntSize(516, 516))
                        zoomState.zoomable.setContentSize(bitmapSize)
                        zoomState.setSubsamplingImage(hugeCardImageSource)
                    }
                }
            }
            waitMillis(500)

            val zoomState = zoomStateHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomState.zoomable.containerSize)
            assertEquals(expected = bitmapSize, actual = zoomState.zoomable.contentSize)
            assertEquals(expected = originSize, actual = zoomState.zoomable.contentOriginSize)
            assertTrue(actual = zoomState.subsampling.ready)
        }

        runComposeUiTest {
            val originSize = IntSize(7557, 5669)
            val bitmapSize = originSize.div(16)

            var zoomStateHolder: ZoomState? = null
            setContent {
                TestLifecycle {
                    val zoomState = rememberZoomState().apply {
                        zoomStateHolder = this
                    }
                    LaunchedEffect(Unit) {
                        zoomState.zoomable.setContainerSize(IntSize(516, 516))
                        zoomState.zoomable.setContentSize(bitmapSize)
                        zoomState.setSubsamplingImage(hugeCardImageSource.toFactory())
                    }
                }
            }
            waitMillis(500)

            val zoomState = zoomStateHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomState.zoomable.containerSize)
            assertEquals(expected = bitmapSize, actual = zoomState.zoomable.contentSize)
            assertEquals(expected = originSize, actual = zoomState.zoomable.contentOriginSize)
            assertTrue(actual = zoomState.subsampling.ready)
        }

        runComposeUiTest {
            val originSize = IntSize(7557, 5669)
            val bitmapSize = originSize.div(16)

            var zoomStateHolder: ZoomState? = null
            setContent {
                TestLifecycle {
                    val zoomState = rememberZoomState().apply {
                        zoomStateHolder = this
                    }
                    LaunchedEffect(Unit) {
                        zoomState.zoomable.setContainerSize(IntSize(516, 516))
                        zoomState.zoomable.setContentSize(bitmapSize)
                        zoomState.setSubsamplingImage(hugeCardImageSource)
                        zoomState.setSubsamplingImage(null as ImageSource.Factory?)
                    }
                }
            }
            waitMillis(500)

            val zoomState = zoomStateHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomState.zoomable.containerSize)
            assertEquals(expected = bitmapSize, actual = zoomState.zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomState.zoomable.contentOriginSize)
            assertFalse(actual = zoomState.subsampling.ready)
        }
    }
}