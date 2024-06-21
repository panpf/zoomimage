package com.github.panpf.zoomimage.sample.ui.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.sample.ui.navigation.Navigation

@Composable
@Preview
fun MainScreen(navigation: Navigation) {
    GalleryScreen(navigation)
}