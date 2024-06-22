package com.github.panpf.zoomimage.sample.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.zoomimage.sample.ui.icGamepadPainter
import com.github.panpf.zoomimage.sample.ui.widget.MyDialog
import com.github.panpf.zoomimage.sample.ui.widget.rememberMyDialogState

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