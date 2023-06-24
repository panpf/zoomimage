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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState.Loading
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.resize.LongImageClipPrecisionDecider
import com.github.panpf.sketch.resize.LongImageScaleDecider
import com.github.panpf.sketch.resize.Precision.SAME_ASPECT_RATIO
import com.github.panpf.sketch.stateimage.IconStateImage
import com.github.panpf.sketch.stateimage.ResColor
import com.github.panpf.zoomimage.sample.NavMainDirections
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.compose.ComposeZoomImageType
import com.github.panpf.zoomimage.sample.ui.compose.base.AppBarFragment
import com.github.panpf.zoomimage.sample.ui.view.photoalbum.Photo
import com.github.panpf.zoomimage.sample.ui.view.photoalbum.PhotoAlbumViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState

class PhotoAlbumComposeFragment : AppBarFragment() {

    private val photoAlbumViewModel by viewModels<PhotoAlbumViewModel>()
    private val args by navArgs<PhotoAlbumComposeFragmentArgs>()

    override fun getTitle(): String {
        return "Photo Album (Compose)"
    }

    override fun getSubtitle(): String {
        return ComposeZoomImageType.valueOf(args.composeZoomImageType).title
    }

    @Composable
    override fun DrawContent() {
        val pagingItems = photoAlbumViewModel.pagingFlow.collectAsLazyPagingItems()
        SwipeRefresh(
            modifier = Modifier.fillMaxSize(),
            state = SwipeRefreshState(pagingItems.loadState.refresh is Loading),
            onRefresh = { pagingItems.refresh() }
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(dimensionResource(id = R.dimen.grid_divider)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.grid_divider)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.grid_divider)),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(LocalContext.current.resources.getColor(R.color.windowBackgroundDark)))
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = { pagingItems.peek(it)?.diffKey ?: "" },
                ) { index ->
                    val photo = pagingItems.peek(index)
                    com.github.panpf.sketch.compose.AsyncImage(
                        request = DisplayRequest(LocalContext.current, photo?.uri) {
                            placeholder(
                                IconStateImage(
                                    R.drawable.ic_image_outline,
                                    ResColor(R.color.placeholder_bg)
                                )
                            )
                            error(
                                IconStateImage(
                                    R.drawable.ic_error,
                                    ResColor(R.color.placeholder_bg)
                                )
                            )
                            crossfade()
                            resizeApplyToDrawable()
                            resizePrecision(LongImageClipPrecisionDecider(SAME_ASPECT_RATIO))
                            resizeScale(LongImageScaleDecider())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable {
                                startImageDetail(pagingItems, index)
                            },
                        contentScale = ContentScale.Crop,
                        contentDescription = "photo",
                    )
                }
            }
        }
    }

    private fun startImageDetail(pagingItems: LazyPagingItems<Photo>, position: Int) {
        val startPosition = (position - 50).coerceAtLeast(0)
        val totalCount = pagingItems.itemCount
        val endPosition = (position + 50).coerceAtMost(totalCount - 1)
        val imageList = (startPosition..endPosition).map {
            pagingItems.peek(it)?.uri
        }
        findNavController().navigate(
            NavMainDirections.actionGlobalPhotoPagerComposeFragment(
                composeZoomImageType = args.composeZoomImageType,
                imageUris = imageList.joinToString(separator = ","),
                position = position,
                startPosition = startPosition,
                totalCount = totalCount
            ),
        )
    }
}
