package com.github.panpf.zoomimage.sample.ui.settings

import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.ui.components.ClickableSettingItem
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

@Composable
actual fun PlatformOtherSettingsList(appSettings: AppSettings, page: AppSettingsPage) {
    if (page == AppSettingsPage.LIST) {
        ClickableSettingItem(
            title = "Local Album Path",
            desc = "Add a local album path. Long press to clear.",
            value = appSettings.localPhotosDirPath,
            onClick = {
                SwingUtilities.invokeLater {
                    val dir = pickDir()
                    if (dir != null) {
                        appSettings.localPhotosDirPath.value = dir.absolutePath
                    }
                }
            },
            onLongClick = {
                appSettings.localPhotosDirPath.value = ""
            }
        )
    }
}

private fun pickDir(): File? {
    val chooser = JFileChooser().apply {
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        dialogTitle = "Select directory"
        isAcceptAllFileFilterUsed = false
    }
    val result = chooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        return chooser.selectedFile
    }

    return null
}