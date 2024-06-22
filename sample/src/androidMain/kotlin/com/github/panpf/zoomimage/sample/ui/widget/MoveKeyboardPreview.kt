package com.github.panpf.zoomimage.sample.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.zoomimage.sample.ui.widget.MoveKeyboard
import com.github.panpf.zoomimage.sample.ui.widget.rememberMoveKeyboardState


@Preview
@Composable
fun MoveKeyboardPreview() {
    MoveKeyboard(rememberMoveKeyboardState())
}