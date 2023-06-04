package com.github.panpf.zoomimage.sample.ui.base.compose

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import com.github.panpf.zoomimage.sample.R

@Composable
fun MyTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val primary = Color(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, null))
    val onPrimary = Color(ResourcesCompat.getColor(context.resources, R.color.colorOnPrimary, null))
    val secondary = Color(ResourcesCompat.getColor(context.resources, R.color.colorSecondary, null))
    val onSecondary = Color(ResourcesCompat.getColor(context.resources, R.color.colorOnSecondary, null))
    val tertiary = Color(ResourcesCompat.getColor(context.resources, R.color.colorTertiary, null))
    val onTertiary = Color(ResourcesCompat.getColor(context.resources, R.color.colorOnTertiary, null))
    val background = Color(ResourcesCompat.getColor(context.resources, R.color.windowBackground, null))
    val colorScheme = darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        tertiary = tertiary,
        onTertiary = onTertiary,
        background = background
    )
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
//            activity.window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(activity.window, view)
                .isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography3,
        content = content
    )
}