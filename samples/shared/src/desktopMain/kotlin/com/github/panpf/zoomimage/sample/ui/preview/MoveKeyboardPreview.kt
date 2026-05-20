package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.zoomimage.sample.ui.components.MoveKeyboard
import com.github.panpf.zoomimage.sample.ui.components.rememberMoveKeyboardState

@Preview
@Composable
fun MoveKeyboardPreview() {
    MoveKeyboard(rememberMoveKeyboardState())
}