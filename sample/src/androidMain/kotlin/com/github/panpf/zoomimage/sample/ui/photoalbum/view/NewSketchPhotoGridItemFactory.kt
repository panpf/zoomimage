/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.sample.ui.photoalbum.view

import android.widget.ImageView
import com.github.panpf.sketch.loadImage
import com.github.panpf.sketch.resize.LongImagePrecisionDecider
import com.github.panpf.sketch.resize.LongImageScaleDecider
import com.github.panpf.sketch.resize.Precision
import com.github.panpf.sketch.state.IconDrawableStateImage
import com.github.panpf.zoomimage.sample.R

class NewSketchPhotoGridItemFactory : NewBasePhotoGridItemFactory() {

    override fun loadImage(imageView: ImageView, sketchImageUri: String) {
        imageView.loadImage(sketchImageUri) {
            placeholder(
                IconDrawableStateImage(
                    icon = R.drawable.ic_image_outline,
                    background = R.color.placeholder_bg
                )
            )
            error(
                IconDrawableStateImage(
                    icon = R.drawable.ic_error_baseline,
                    background = R.color.placeholder_bg
                )
            )
            crossfade()
            resizeOnDraw()
            precision(LongImagePrecisionDecider(Precision.SAME_ASPECT_RATIO))
            scale(LongImageScaleDecider())
            // TODO long click image info
        }
    }
}