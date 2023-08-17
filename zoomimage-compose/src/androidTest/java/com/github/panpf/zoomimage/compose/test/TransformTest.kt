//package com.github.panpf.zoomimage.compose.test
//
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.unit.IntSize
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.github.panpf.zoomimage.ReadMode
//import com.github.panpf.zoomimage.compose.internal.ScaleFactor
//import com.github.panpf.zoomimage.compose.zoom.Transform
//import com.github.panpf.zoomimage.compose.internal.toCompat
//import com.github.panpf.zoomimage.compose.internal.toPlatform
//import com.github.panpf.zoomimage.compose.zoom.plus
//import com.github.panpf.zoomimage.compose.zoom.split
//import com.github.panpf.zoomimage.util.computeBaseTransform
//import com.github.panpf.zoomimage.util.concat
//import org.junit.Assert
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class TransformTest {
//
//    @Test
//    fun testConcatAndSplit() {
//        // todo 修复这个测试
//        val containerSize = IntSize(1080, 1656)
//        val contentSize = IntSize(7500, 232)
//        val contentScale = ContentScale.Fit
//        val contentAlignment = Alignment.Center
//        val readMode = ReadMode.Default
//
//        val baseTransform = computeBaseTransform(
//            containerSize = containerSize.toCompat(),
//            contentSize = contentSize.toCompat(),
//            contentScale = contentScale.toCompat(),
//            alignment = contentAlignment.toCompat(),
//            rotation = 0,
//        ).toPlatform().also {
//            val expected = Transform(ScaleFactor(0.144f), Offset(0f, 812f))
//            Assert.assertEquals(/* expected = */ expected,/* actual = */ it)
//        }
//
//        val readModeTransform = readMode.computeTransform(
//            containerSize = containerSize.toCompat(),
//            contentSize = contentSize.toCompat(),
//            baseTransform = baseTransform.toCompatTransform()
//        ).toPlatform().also {
//            val expected = Transform(ScaleFactor(7.137931f), Offset(0f, 0f))
//            Assert.assertEquals(/* expected = */ expected,/* actual = */ it)
//        }
//
//        val targetUserTransform = Transform(
//            scale = ScaleFactor(readModeTransform.scaleX / baseTransform.scaleX),
//            offset = Offset(0f, -40250f)
//        )
//        val userTransform = (readModeTransform + baseTransform).also {
//            Assert.assertEquals(/* expected = */ targetUserTransform,/* actual = */ it)
//        }
//        (baseTransform + userTransform).also {
//            Assert.assertEquals(/* expected = */ readModeTransform,/* actual = */ it)
//        }
//    }
//
//    @Test
//    fun testConcatAndSplit2() {
//        // todo 修复这个测试
//        val containerSize = IntSize(1080, 1656)
//        val contentSize = IntSize(7500, 232)
//        val contentScale = ContentScale.None
//        val contentAlignment = Alignment.Center
//        val readMode = ReadMode.Default
//
//        val baseTransform = computeBaseTransform(
//            containerSize = containerSize,
//            contentSize = contentSize,
//            contentScale = contentScale,
//            alignment = contentAlignment
//        ).also {
//            val expected = Transform(ScaleFactor(1f), Offset(-3210f, 712f))
//            Assert.assertEquals(/* expected = */ expected,/* actual = */ it)
//        }
//
//        val readModeTransform = readMode.computeTransform(
//            containerSize = containerSize.toCompat(),
//            contentSize = contentSize.toCompat(),
//            baseTransform = baseTransform.toCompatTransform()
//        ).toPlatform().also {
//            val expected = Transform(ScaleFactor(7.137931f), Offset(-22912.758f, 0f))
//            Assert.assertEquals(/* expected = */ expected,/* actual = */ it)
//        }
//
//        val targetUserTransform = Transform(
//            scale = ScaleFactor(readModeTransform.scaleX / baseTransform.scaleX),
//            offset = Offset(0f, -5082.2065f)
//        )
//        val userTransform = (readModeTransform - baseTransform).also {
//            Assert.assertEquals(/* expected = */ targetUserTransform,/* actual = */ it)
//        }
//        (baseTransform + userTransform).also {
//            Assert.assertEquals(/* expected = */ readModeTransform,/* actual = */ it)
//        }
//    }
//}