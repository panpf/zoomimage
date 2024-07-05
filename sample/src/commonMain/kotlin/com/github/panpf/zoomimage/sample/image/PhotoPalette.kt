package com.github.panpf.zoomimage.sample.image

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun PhotoPalette(palette: SimplePalette): PhotoPalette {
    return PhotoPalette(
        palette = palette,
        primaryColor = 0xFFFFFF,
        tertiaryColor = 0xFFFFFF
    )
}

fun PhotoPalette(palette: SimplePalette?, colorScheme: ColorScheme): PhotoPalette {
    return PhotoPalette(
        palette = palette,
        primaryColor = colorScheme.primary.toArgb(),
        tertiaryColor = colorScheme.tertiary.toArgb()
    )
}

fun PhotoPalette(colorScheme: ColorScheme): PhotoPalette {
    return PhotoPalette(
        palette = null,
        colorScheme = colorScheme
    )
}

fun PhotoPalette(primaryColor: Int, tertiaryColor: Int): PhotoPalette {
    return PhotoPalette(
        palette = null,
        primaryColor = primaryColor,
        tertiaryColor = tertiaryColor,
    )
}


data class PhotoPalette constructor(
    private val palette: SimplePalette?,
    private val primaryColor: Int,
    private val tertiaryColor: Int
) {

    val containerColor: Color by lazy {
        val preferredSwatch = palette?.run {
            listOfNotNull(
                darkMutedSwatch,
                mutedSwatch,
                lightMutedSwatch,
                darkVibrantSwatch,
                vibrantSwatch,
                lightVibrantSwatch,
            ).firstOrNull()
        }
        if (preferredSwatch != null) {
            Color(preferredSwatch.rgb).copy(0.6f)
        } else {
            Color(primaryColor).copy(0.6f)
        }
    }

    val containerColorInt: Int by lazy { containerColor.toArgb() }

    val accentColor: Color by lazy {
        val preferredSwatch = palette?.run {
            listOfNotNull(
                lightVibrantSwatch,
                vibrantSwatch,
                darkVibrantSwatch,
                lightMutedSwatch,
                mutedSwatch,
                darkMutedSwatch,
            ).firstOrNull()
        }
        if (preferredSwatch != null) {
            Color(preferredSwatch.rgb).copy(0.6f)
        } else {
            Color(tertiaryColor).copy(0.6f)
        }
    }

    val accentColorInt: Int by lazy { accentColor.toArgb() }

    val contentColor: Color = Color.White
    val contentColorInt: Int = contentColor.toArgb()
}