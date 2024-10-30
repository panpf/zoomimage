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

package com.github.panpf.zoomimage.subsampling

fun SubsamplingImage(
    imageSource: ImageSource.Factory?,
    imageInfo: ImageInfo? = null
): SubsamplingImage? {
    return imageSource?.let { SubsamplingImage(it, imageInfo) }
}

fun SubsamplingImage(imageSource: ImageSource?, imageInfo: ImageInfo? = null): SubsamplingImage? {
    return imageSource?.let { SubsamplingImage(ImageSource.WrapperFactory(it), imageInfo) }
}

data class SubsamplingImage(val imageSource: ImageSource.Factory, val imageInfo: ImageInfo?) {
    val key: String by lazy { "${imageSource.key}&imageInfo=$imageInfo" }
}