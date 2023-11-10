package com.github.panpf.zoomimage.sample.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp


@Composable
actual fun getSettingsDialogHeight(): Dp {
    return with(LocalDensity.current) {
        (LocalContext.current.resources.displayMetrics.heightPixels * 0.8f).toInt().toDp()
    }
}

@Composable
@Preview
private fun ZoomImageOptionsDialogPreview() {
    val state = remember {
        ZoomImageOptionsState()
    }
    ZoomImageOptionsDialog(my = true, supportIgnoreExifOrientation = true, state) {

    }
}

@Preview
@Composable
private fun SwitchMenuPreview() {
    SwitchMenu("Animate Scale", false) {

    }
}

@Preview
@Composable
private fun MyDropdownMenuPreview() {
    val values = remember {
        listOf("A", "B", "C", "D")
    }
    MyDropdownMenu("Animate Scale", "A", values) {

    }
}

@Preview
@Composable
private fun MyMultiChooseMenuPreview() {
    val values = remember {
        listOf("A", "B", "C", "D")
    }
    MyMultiChooseMenu(
        name = "Animate Scale",
        values = values,
        checkedList = listOf(true, false, true, false),
    ) { _, _ ->

    }
}