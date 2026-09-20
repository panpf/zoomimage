package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.zoomimage.sample.ui.settings.AppSettingsList
import com.github.panpf.zoomimage.sample.ui.settings.AppSettingsPage
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Preview
@Composable
fun AppSettingsListPreview1() {
    AppSettingsList(AppSettingsPage.VIEWER)
}

@OptIn(ExperimentalResourceApi::class)
@Preview
@Composable
fun AppSettingsListPreview2() {
    AppSettingsList(AppSettingsPage.LIST)
}