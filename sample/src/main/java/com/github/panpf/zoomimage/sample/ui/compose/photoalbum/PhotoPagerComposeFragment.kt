/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.zoomimage.sample.ui.compose.photoalbum

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.fragment.navArgs
import com.github.panpf.zoomimage.sample.ui.compose.ComposeZoomImageType
import com.github.panpf.zoomimage.sample.ui.compose.base.ComposeFragment
import com.github.panpf.zoomimage.sample.ui.compose.telephoto.ZoomableAsyncImageFullSample
import com.github.panpf.zoomimage.sample.ui.compose.zoomimage.MyZoomImageFullSample
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel

class PhotoPagerComposeFragment : ComposeFragment() {

    private val args by navArgs<PhotoPagerComposeFragmentArgs>()

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun DrawContent() {
        val imageUrlList = remember { args.imageUris.split(",") }
        val pagerState = rememberPagerState(initialPage = args.position - args.startPosition)
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                pageCount = imageUrlList.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { index ->
                if (args.composeZoomImageType == ComposeZoomImageType.MyZoomImage.name) {
                    MyZoomImageFullSample()
                } else {
                    ZoomableAsyncImageFullSample(
                        sketchUri2CoilModel(
                            LocalContext.current,
                            imageUrlList[index]
                        )
                    )
                }
            }
            PageNumber(
                number = pagerState.currentPage + 1,
                total = args.totalCount,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun PageNumber(modifier: Modifier = Modifier, number: Int, total: Int) {
    Text(
        text = "${number}\n·\n${total}",
        textAlign = TextAlign.Center,
        color = Color.White,
        style = TextStyle(lineHeight = 12.sp),
        modifier = Modifier
            .padding(12.dp) // margin
            .background(
                color = Color(0x60000000),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .then(modifier)
    )
}

@Preview
@Composable
private fun PageNumberPreview() {
    PageNumber(number = 9, total = 22)
}