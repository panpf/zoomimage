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

package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.subsampling.RegionDecoder

/**
 * Get the platform's default RegionDecoder
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.internal.DecodesAndroidTest.testDefaultRegionDecoder
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal.DecodesNonAndroidTest.testDefaultRegionDecoder
 */
@Deprecated("Use defaultRegionDecoders instead")
expect fun defaultRegionDecoder(): RegionDecoder.Factory

/**
 * Get the platform's default RegionDecoder List
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.internal.DecodesAndroidTest.testDefaultRegionDecoders
 * @see com.github.panpf.zoomimage.core.desktop.test.subsampling.internal.DecodesDesktopTest.testDefaultRegionDecoders
 * @see com.github.panpf.zoomimage.core.ios.test.subsampling.internal.DecodesIosTest.testDefaultRegionDecoders
 * @see com.github.panpf.zoomimage.core.macos.test.subsampling.internal.DecodesMacosTest.testDefaultRegionDecoders
 * @see com.github.panpf.zoomimage.core.jscommon.test.subsampling.internal.DecodesJsCommonTest.testDefaultRegionDecoders
 */
expect fun defaultRegionDecoders(): List<RegionDecoder.Factory>
