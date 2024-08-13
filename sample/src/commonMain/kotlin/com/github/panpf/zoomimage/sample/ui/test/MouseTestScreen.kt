package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.util.onPointerEvent

class MouseTestScreen : BaseScreen() {

    @Composable
    @OptIn(ExperimentalComposeUiApi::class)
    override fun DrawContent() {
        ToolbarScaffold("Mouse", ignoreNavigationBarInsets = true) {
            var number by remember { mutableStateOf(0f) }
            var color by remember { mutableStateOf(Color(0, 0, 0)) }
            var pointerPosition by remember { mutableStateOf(Offset(0f, 0f)) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = color)
                    .onPointerEvent(PointerEventType.Scroll) {
                        number += it.changes.first().scrollDelta.y
                    }
                    .onPointerEvent(PointerEventType.Move) {
                        val position = it.changes.first().position
                        pointerPosition = position
                        color = Color(position.x.toInt() % 256, position.y.toInt() % 256, 0)
                    }
            ) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = "scroll: $number")
                    Text(text = "pointerPosition: $pointerPosition")
                }
            }
        }
    }
}