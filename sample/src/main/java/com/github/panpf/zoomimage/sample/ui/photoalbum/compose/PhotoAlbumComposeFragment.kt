package com.github.panpf.zoomimage.sample.ui.photoalbum.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState.Loading
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.resize.LongImageClipPrecisionDecider
import com.github.panpf.sketch.resize.LongImageScaleDecider
import com.github.panpf.sketch.resize.Precision.SAME_ASPECT_RATIO
import com.github.panpf.sketch.stateimage.IconStateImage
import com.github.panpf.sketch.stateimage.ResColor
import com.github.panpf.zoomimage.sample.NavMainDirections
import com.github.panpf.zoomimage.sample.R.color
import com.github.panpf.zoomimage.sample.R.dimen
import com.github.panpf.zoomimage.sample.R.drawable
import com.github.panpf.zoomimage.sample.ui.base.compose.AppBarFragment
import com.github.panpf.zoomimage.sample.ui.examples.compose.ZoomImageType
import com.github.panpf.zoomimage.sample.ui.photoalbum.Photo
import com.github.panpf.zoomimage.sample.ui.photoalbum.PhotoAlbumViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState

class PhotoAlbumComposeFragment : AppBarFragment() {

    private val photoAlbumViewModel by viewModels<PhotoAlbumViewModel>()
    private val args by navArgs<PhotoAlbumComposeFragmentArgs>()

    override fun getTitle(): String {
        return "Photo Album (Compose)"
    }

    override fun getSubtitle(): String {
        return ZoomImageType.valueOf(args.zoomImageType).title
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
                columns = Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = dimen.grid_divider)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = dimen.grid_divider)),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(ResourcesCompat.getColor(LocalContext.current.resources, color.windowBackgroundDark, null)))
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = { pagingItems.peek(it)?.diffKey ?: "" },
                ) { index ->
                    val photo = pagingItems.peek(index)
                    AsyncImage(
                        request = DisplayRequest(LocalContext.current, photo?.uri) {
                            placeholder(
                                IconStateImage(
                                    drawable.ic_image_outline,
                                    ResColor(color.placeholder_bg)
                                )
                            )
                            error(
                                IconStateImage(
                                    drawable.ic_error,
                                    ResColor(color.placeholder_bg)
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
            NavMainDirections.actionGlobalPhotoSlideshowComposeFragment(
                zoomImageType = args.zoomImageType,
                imageUris = imageList.joinToString(separator = ","),
                position = position,
                startPosition = startPosition,
                totalCount = totalCount
            ),
        )
    }
}