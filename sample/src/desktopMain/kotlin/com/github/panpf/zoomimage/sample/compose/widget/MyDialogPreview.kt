package com.github.panpf.zoomimage.sample.compose.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.sample.compose.icGamepadPainter

@Preview
@Composable
fun MyDialogPreview() {
    MyDialog(state = rememberMyDialogState(true)) {
        Image(
            painter = icGamepadPainter(),
            contentDescription = ""
        )
    }
}