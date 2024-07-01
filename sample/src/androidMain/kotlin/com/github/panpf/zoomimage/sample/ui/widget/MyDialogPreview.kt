package com.github.panpf.zoomimage.sample.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_gamepad
import org.jetbrains.compose.resources.painterResource

@Preview
@Composable
fun MyDialogPreview() {
    MyDialog(state = rememberMyDialogState(true)) {
        Image(
            painter = painterResource(Res.drawable.ic_gamepad),
            contentDescription = ""
        )
    }
}