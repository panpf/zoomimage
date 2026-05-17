package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.sample.ui.test.GraphicsLayerTestScreen


@Preview
@Composable
fun GraphicsLayerScreenPreview() {
    remember { GraphicsLayerTestScreen() }.DrawContent()
}