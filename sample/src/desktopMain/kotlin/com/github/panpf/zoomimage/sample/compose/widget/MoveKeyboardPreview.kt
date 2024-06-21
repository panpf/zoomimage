package com.github.panpf.zoomimage.sample.compose.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable


@Preview
@Composable
fun MoveKeyboardPreview() {
    MoveKeyboard(rememberMoveKeyboardState())
}