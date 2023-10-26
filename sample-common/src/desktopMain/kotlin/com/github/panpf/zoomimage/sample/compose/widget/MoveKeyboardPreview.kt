package com.github.panpf.zoomimage.sample.compose.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset


@Preview
@Composable
fun MoveKeyboardPreview() {
    MoveKeyboard(rememberMoveKeyboardState(Offset(100f, 100f)))
}