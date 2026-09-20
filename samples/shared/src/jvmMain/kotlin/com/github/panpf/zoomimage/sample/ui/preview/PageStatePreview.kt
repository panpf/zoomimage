package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.zoomimage.sample.ui.components.PageState

@Preview
@Composable
fun PageStatePreview1() {
    PageState(PageState.Loading)
}

@Preview
@Composable
fun PageStatePreview2() {
    PageState(PageState.Error {})
}