package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test

class ZoomMatcherKeyHandlerTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun test() {
        // TODO test
//        runComposeUiTest {
//            var zoomableHolder: ZoomableState? = null
//            setContent {
//                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
//                zoomable.containerSize = IntSize(516, 516)
//                zoomable.contentSize = IntSize(86, 1522)
//                LaunchedEffect(Unit) {
//                    zoomable.scale(zoomable.maxScale, animated = false)
//                }
//            }
//            val zoomable = zoomableHolder!!
//            assertEquals(
//                expected = -415.42f,
//                actual = currentScaleKeyHandler.getValue(zoomable).format(2)
//            )
//        }
    }
//
//    private class TestZoomMatcherKeyHandler : ZoomMatcherKeyHandler() {
//        override fun getScaleKey(zoomable: ZoomableState): Float {
//            return -415.42f
//        }
//    }
}