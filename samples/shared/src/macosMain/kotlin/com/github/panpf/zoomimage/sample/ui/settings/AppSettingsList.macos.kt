package com.github.panpf.zoomimage.sample.ui.settings

import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.ui.components.ClickableSettingItem
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AppKit.NSModalResponseOK
import platform.AppKit.NSOpenPanel

@Composable
actual fun PlatformOtherSettingsList(appSettings: AppSettings, page: AppSettingsPage) {
    if (page == AppSettingsPage.LIST) {
        ClickableSettingItem(
            title = "Local Album Path",
            desc = "Add a local album path. Long press to clear.",
            value = appSettings.localPhotosDirPath,
            onClick = {
                pickDir()?.let { appSettings.localPhotosDirPath.value = it }
            },
            onLongClick = {
                appSettings.localPhotosDirPath.value = ""
            }
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun pickDir(): String? {
    val panel = NSOpenPanel.openPanel()
    panel.canChooseDirectories = true
    panel.canChooseFiles = false
    panel.allowsMultipleSelection = false
    panel.title = "Select directory"
    return if (panel.runModal() == NSModalResponseOK) panel.URL?.path else null
}
