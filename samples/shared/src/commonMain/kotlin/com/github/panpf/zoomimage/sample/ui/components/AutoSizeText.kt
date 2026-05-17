package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit

/**
 * Resizeable Text element that contains all the default behavior and description
 * that you can find in [Text].
 * Minimization applied only by height. If you want to reach result,
 * you need to set [maxLines], otherwise [fontSize] will be used.
 *
 * future releases:
 * 1. Support overflow
 * 2. Support maxFontSize
 * 3. Add types: Maximisation, Minimization, Balanced
 *
 * Extra parameters:
 * @param minFontSize - Allows you to specify minimum allowed font size for text.
 *
 * https://github.com/idapgroup/AutoSizeText
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = TextUnit.Unspecified,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val defaultFontSize = coerceTextUnit(
        expected = fontSize,
        default = style.fontSize
    )
    val defaultLineHeight = coerceTextUnit(
        expected = lineHeight,
        default = style.lineHeight
    )

    var overriddenMetrics by remember(key1 = text) {
        mutableStateOf(
            InnerMetrics(
                fontSize = defaultFontSize,
                lineHeight = defaultLineHeight
            )
        )
    }
    var textReadyToDraw by remember(key1 = text) {
        mutableStateOf(false)
    }

    Text(
        modifier = modifier.drawWithContent {
            if (textReadyToDraw) drawContent()
        },
        text = text,
        color = color,
        textAlign = textAlign,
        fontSize = overriddenMetrics.fontSize,
        fontFamily = fontFamily,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        lineHeight = overriddenMetrics.lineHeight,
        style = style,
        maxLines = maxLines,
        minLines = minLines,
        softWrap = softWrap,
        onTextLayout = { result ->
            if (minFontSize == TextUnit.Unspecified || overriddenMetrics.fontSize > minFontSize) {
                if (result.didOverflowHeight) {
                    overriddenMetrics = overriddenMetrics.copy(
                        fontSize = overriddenMetrics.fontSize.times(SIZE_DECREASER),
                        lineHeight = overriddenMetrics.lineHeight.times(SIZE_DECREASER)
                    )
                } else {
                    textReadyToDraw = true
                }
            } else {
                if (overriddenMetrics.fontSize <= minFontSize) {
                    val lineHeightMultiplier = minFontSize.value.div(defaultFontSize.value)
                    val minLineHeight = defaultLineHeight.times(lineHeightMultiplier)
                    overriddenMetrics = InnerMetrics(
                        fontSize = minFontSize,
                        lineHeight = minLineHeight
                    )
                    textReadyToDraw = true
                }
            }
            onTextLayout(result)
        },
    )
}

internal const val SIZE_DECREASER = 0.9f

internal data class InnerMetrics(
    val fontSize: TextUnit,
    val lineHeight: TextUnit,
)

internal fun coerceTextUnit(
    expected: TextUnit,
    default: TextUnit
) = if (expected != TextUnit.Unspecified) expected else default
