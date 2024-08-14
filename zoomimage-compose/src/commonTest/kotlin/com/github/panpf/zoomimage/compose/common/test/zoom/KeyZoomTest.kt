package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.ui.input.key.Key
import com.github.panpf.zoomimage.compose.util.AssistKey
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.util.platformAssistKey
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveDownKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveLeftKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveRightKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultMoveUpKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultScaleInKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultScaleOutKeyMatchers
import com.github.panpf.zoomimage.compose.zoom.DefaultZoomKeyHandlers
import com.github.panpf.zoomimage.compose.zoom.MoveArrow
import com.github.panpf.zoomimage.compose.zoom.MoveKeyHandler
import com.github.panpf.zoomimage.compose.zoom.ScaleKeyHandler
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals

class KeyZoomTest {

    @Test
    fun testDefaultScaleInKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.ZoomIn),
                KeyMatcher(key = Key.Equals, assistKey = platformAssistKey()),
                KeyMatcher(key = Key.Equals, assistKey = AssistKey.Alt),
                KeyMatcher(key = Key.DirectionUp, assistKey = AssistKey.Alt),
                KeyMatcher(key = Key.DirectionUp, assistKey = platformAssistKey()),
            ).toImmutableList(),
            actual = DefaultScaleInKeyMatchers
        )
    }

    @Test
    fun testDefaultScaleOutKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.ZoomOut),
                KeyMatcher(key = Key.Minus, assistKey = platformAssistKey()),
                KeyMatcher(key = Key.Minus, assistKey = AssistKey.Alt),
                KeyMatcher(key = Key.DirectionDown, assistKey = AssistKey.Alt),
                KeyMatcher(key = Key.DirectionDown, assistKey = platformAssistKey()),
            ).toImmutableList(),
            actual = DefaultScaleOutKeyMatchers
        )
    }

    @Test
    fun testDefaultMoveUpKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.DirectionUp),
            ).toImmutableList(),
            actual = DefaultMoveUpKeyMatchers
        )
    }

    @Test
    fun testDefaultMoveDownKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.DirectionDown),
            ).toImmutableList(),
            actual = DefaultMoveDownKeyMatchers
        )
    }

    @Test
    fun testDefaultMoveLeftKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.DirectionLeft),
            ).toImmutableList(),
            actual = DefaultMoveLeftKeyMatchers
        )
    }

    @Test
    fun testDefaultMoveRightKeyMatchers() {
        assertEquals(
            expected = listOf(
                KeyMatcher(key = Key.DirectionRight),
            ).toImmutableList(),
            actual = DefaultMoveRightKeyMatchers
        )
    }

    @Test
    fun testDefaultZoomKeyHandlers() {
        assertEquals(
            expected = listOf(
                ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = true),
                ScaleKeyHandler(keyMatchers = DefaultScaleOutKeyMatchers, scaleIn = false),
                MoveKeyHandler(
                    keyMatchers = DefaultMoveUpKeyMatchers,
                    moveArrow = MoveArrow.Up
                ),
                MoveKeyHandler(
                    keyMatchers = DefaultMoveDownKeyMatchers,
                    moveArrow = MoveArrow.Down
                ),
                MoveKeyHandler(
                    keyMatchers = DefaultMoveLeftKeyMatchers,
                    moveArrow = MoveArrow.Left
                ),
                MoveKeyHandler(
                    keyMatchers = DefaultMoveRightKeyMatchers,
                    moveArrow = MoveArrow.Right
                ),
            ).toImmutableList(),
            actual = DefaultZoomKeyHandlers
        )
    }
}