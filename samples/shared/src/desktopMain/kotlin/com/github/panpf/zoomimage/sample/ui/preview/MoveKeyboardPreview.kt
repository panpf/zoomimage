package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.sample.ui.components.MoveKeyboard
import com.github.panpf.zoomimage.sample.ui.components.rememberMoveKeyboardState


@Preview
@Composable
fun MoveKeyboardPreview() {
    MoveKeyboard(rememberMoveKeyboardState())
}