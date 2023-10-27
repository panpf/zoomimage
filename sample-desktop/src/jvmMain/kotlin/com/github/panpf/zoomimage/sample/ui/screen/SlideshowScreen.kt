package com.github.panpf.zoomimage.sample.ui.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.zoomimage.sample.ui.model.ImageResource
import com.github.panpf.zoomimage.sample.ui.navigation.Navigation
import com.github.panpf.zoomimage.sample.ui.screen.base.ToolbarScreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun SlideshowScreen(
    navigation: Navigation,
    imageResources: List<ImageResource>,
    initialIndex: Int
) {
    ToolbarScreen(navigation) {
        val pagerState = rememberPagerState(initialPage = initialIndex) {
            imageResources.size
        }
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 0,
            modifier = Modifier.fillMaxSize()
        ) { index ->
            ViewerScreen(navigation, imageResources[index])
        }
        PageNumber(
            number = pagerState.currentPage + 1,
            total = imageResources.size,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun PageNumber(modifier: Modifier = Modifier, number: Int, total: Int) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = "${number}\n·\n${total}",
        textAlign = TextAlign.Center,
        color = colors.onTertiary,
        style = TextStyle(lineHeight = 12.sp),
        modifier = Modifier
            .padding(20.dp) // margin
            .background(
                color = colors.tertiary.copy(alpha = 0.7f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .then(modifier)
    )
}