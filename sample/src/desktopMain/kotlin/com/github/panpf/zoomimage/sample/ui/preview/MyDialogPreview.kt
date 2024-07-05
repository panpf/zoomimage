package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_gamepad
import com.github.panpf.zoomimage.sample.ui.components.MyDialog
import com.github.panpf.zoomimage.sample.ui.components.rememberMyDialogState
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