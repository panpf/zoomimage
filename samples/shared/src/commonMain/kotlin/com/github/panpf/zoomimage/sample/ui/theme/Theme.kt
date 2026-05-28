package com.github.panpf.zoomimage.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.DarkMode
import org.koin.compose.koinInject

@Composable
fun AppTheme(
    darkMode: DarkMode? = null,
    content: @Composable() () -> Unit
) {
    val appSettings: AppSettings = koinInject()
    val settingsDarkMode by appSettings.darkMode.collectAsState()
    val realDarkMode = darkMode ?: settingsDarkMode
    val useDarkTheme = when (realDarkMode) {
        DarkMode.SYSTEM -> isSystemInDarkTheme()
        DarkMode.DARK -> true
        DarkMode.LIGHT -> false
    }
    val colors = if (useDarkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}