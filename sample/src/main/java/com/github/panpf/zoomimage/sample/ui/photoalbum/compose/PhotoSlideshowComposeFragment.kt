package com.github.panpf.zoomimage.sample.ui.photoalbum.compose

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.fragment.navArgs
import com.github.panpf.zoomimage.sample.ui.base.compose.ComposeFragment
import com.github.panpf.zoomimage.sample.ui.examples.compose.ZoomImageType

class PhotoSlideshowComposeFragment : ComposeFragment() {

    private val args by navArgs<PhotoSlideshowComposeFragmentArgs>()

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun DrawContent() {
        val imageUrlList = remember { args.imageUris.split(",") }
        val pagerState = rememberPagerState(initialPage = args.position - args.startPosition)
        val zoomImageType = remember { ZoomImageType.valueOf(args.zoomImageType) }
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                pageCount = imageUrlList.size,
                state = pagerState,
                beyondBoundsPageCount = 1,
                modifier = Modifier.fillMaxSize()
            ) { index ->
                zoomImageType.drawContent(imageUrlList[index])
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