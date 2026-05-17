/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.sample.ui.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Looper
import android.view.MotionEvent
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.core.content.res.ResourcesCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlin.math.roundToInt

@Composable
actual fun windowSize(): IntSize {
    val context = LocalContext.current
    return context.resources.displayMetrics.let { displayMetrics ->
        IntSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
}

actual fun ImageBitmap.crop(rect: IntRect): ImageBitmap {
    return this.asAndroidBitmap().crop(rect.toAndroidRect()).asImageBitmap()
}

fun Bitmap.crop(rect: Rect): Bitmap {
    require(!rect.isEmpty) { "Rect must not be empty. rect=$rect" }
    require(
        rect.left >= 0
                && rect.top >= 0
                && rect.right <= this@crop.width
                && rect.bottom <= this@crop.height
    ) {
        "Rect must be within the bounds of the image. imageSize=${this@crop.width}x${this@crop.height}, rect=$rect"
    }
    val androidBitmap = this
    val croppedBitmap = Bitmap.createBitmap(
        /* source = */ androidBitmap,
        /* x = */ rect.left,
        /* y = */ rect.top,
        /* width = */ rect.width(),
        /* height = */ rect.height()
    )
    return croppedBitmap
}

fun Resources.getDrawableCompat(@DrawableRes id: Int, theme: Theme? = null): Drawable {
    return checkNotNull(ResourcesCompat.getDrawable(this, id, theme)) {
        "Can't find drawable by id=$id"
    }
}

fun Context.getDrawableCompat(@DrawableRes resId: Int): Drawable {
    val drawable = AppCompatResources.getDrawable(this, resId)
    return checkNotNull(drawable) { "Invalid resource ID: $resId" }
}

fun Context.isDarkTheme(): Boolean {
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun Context.getWindowBackgroundColor(): Int {
    val array = theme.obtainStyledAttributes(
        intArrayOf(android.R.attr.windowBackground)
    )
    val windowBackground = array.getColor(0, 0xFF00FF)
    array.recycle()
    return windowBackground
}

fun Context.isNightMode(): Boolean {
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}


fun requiredMainThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) {
        "This method must be executed in the UI thread"
    }
}

fun requiredWorkThread() {
    check(Looper.myLooper() != Looper.getMainLooper()) {
        "This method must be executed in the work thread"
    }
}

fun getPointerIndex(action: Int): Int {
    return action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
}

val ZeroRect = Rect(0, 0, 0, 0)

fun IntSizeCompat.times(scale: Float): IntSizeCompat =
    IntSizeCompat(
        (this.width * scale).roundToInt(),
        (this.height * scale).roundToInt()
    )

fun Rect.scale(scale: Float): Rect {
    return Rect(
        left = (left * scale).roundToInt(),
        top = (top * scale).roundToInt(),
        right = (right * scale).roundToInt(),
        bottom = (bottom * scale).roundToInt()
    )
}

fun Rect(left: Int, top: Int, right: Int, bottom: Int): Rect {
    return Rect(left, top, right, bottom)
}

fun Rect.toIntRectCompat(): IntRectCompat {
    return IntRectCompat(left, top, right, bottom)
}

fun IntRectCompat.toAndroidRect(): Rect {
    return Rect(left, top, right, bottom)
}

fun computeImageViewSize(context: Context): IntSizeCompat {
    val displayMetrics = context.resources.displayMetrics
    val width = (displayMetrics.widthPixels * 0.7f).roundToInt()
    val height = (displayMetrics.widthPixels * 0.7f * 0.7f).roundToInt()
    return IntSizeCompat(width, height)
}