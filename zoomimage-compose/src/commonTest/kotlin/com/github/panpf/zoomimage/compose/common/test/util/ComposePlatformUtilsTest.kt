package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.compose.util.thenIfNotNull
import com.github.panpf.zoomimage.compose.util.toDp
import com.github.panpf.zoomimage.compose.util.toPx
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class ComposePlatformUtilsTest {

    @Test
    fun testDpToPx() = runComposeUiTest {
        setContent {
            assertEquals(
                expected = with(LocalDensity.current) { 13.5f.dp.toPx() },
                actual = 13.5f.dp.toPx()
            )
            assertEquals(
                expected = with(LocalDensity.current) { 75.3f.dp.toPx() },
                actual = 75.3f.dp.toPx()
            )
        }
    }

    @Test
    fun testFloatToDp() = runComposeUiTest {
        setContent {
            assertEquals(
                expected = with(LocalDensity.current) { 13.5f.toDp() },
                actual = 13.5f.toDp()
            )
            assertEquals(
                expected = with(LocalDensity.current) { 75.3f.toDp() },
                actual = 75.3f.toDp()
            )
        }
    }

    @Test
    fun thenIfNotNull() {
        val modifier = Modifier.background(Color.Red)
        val modifier1 = modifier.thenIfNotNull(null as Int?) {
            Modifier.border(10.dp, Color.Blue)
        }
        val modifier2 = modifier.thenIfNotNull(4) {
            Modifier.border(10.dp, Color.Blue)
        }
        assertEquals(
            expected = modifier,
            actual = modifier1
        )
        assertNotEquals(
            illegal = modifier2,
            actual = modifier
        )
    }
}