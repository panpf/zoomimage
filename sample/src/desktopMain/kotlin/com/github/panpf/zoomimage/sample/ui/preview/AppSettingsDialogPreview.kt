package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.sample.ui.AppSettingsDialog
import com.github.panpf.zoomimage.sample.ui.AppSettingsDialogState
import com.github.panpf.zoomimage.sample.ui.MyDropdownMenu
import com.github.panpf.zoomimage.sample.ui.MyMultiChooseMenu
import com.github.panpf.zoomimage.sample.ui.SwitchMenu


@Preview
@Composable
private fun AppSettingsDialogPreview() {
    val state = remember {
        AppSettingsDialogState()
    }
    AppSettingsDialog(my = true, state) {

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