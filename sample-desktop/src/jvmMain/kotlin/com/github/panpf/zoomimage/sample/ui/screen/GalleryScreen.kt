package com.github.panpf.zoomimage.sample.ui.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.sample.ui.Page
import com.github.panpf.zoomimage.sample.ui.model.ImageResource
import com.github.panpf.zoomimage.sample.ui.navigation.Navigation


@Composable
@Preview
fun GalleryScreen(navigation: Navigation) {
    val imageResourceList = remember {
        listOf(
            ImageResource("sample_cat.jpg"),
            ImageResource("sample_dog.jpg"),
            ImageResource("sample_elephant.jpg"),
            ImageResource("sample_whale.jpg"),
            ImageResource("sample_huge_card.jpg", "sample_huge_card_thumbnail.jpg"),
            ImageResource("sample_huge_china.jpg", "sample_huge_china_thumbnail.jpg"),
            ImageResource("sample_huge_world.jpg", "sample_huge_world_thumbnail.jpg"),
            ImageResource("sample_long_qmsht.jpg", "sample_long_qmsht_thumbnail.jpg"),
            ImageResource("sample_long_comic.jpg", "sample_long_comic_thumbnail.jpg"),
            ImageResource(
                "sample_exif_girl_flip_hor.jpeg",
                "sample_exif_girl_flip_hor_thumbnail.jpeg"
            ),
            ImageResource(
                "sample_exif_girl_flip_ver.jpeg",
                "sample_exif_girl_flip_ver_thumbnail.jpeg"
            ),
            ImageResource(
                "sample_exif_girl_rotate_90.jpeg",
                "sample_exif_girl_rotate_90_thumbnail.jpeg"
            ),
            ImageResource(
                "sample_exif_girl_rotate_180.jpeg",
                "sample_exif_girl_rotate_180_thumbnail.jpeg"
            ),
            ImageResource(
                "sample_exif_girl_rotate_270.jpeg",
                "sample_exif_girl_rotate_270_thumbnail.jpeg"
            ),
            ImageResource(
                "sample_exif_girl_transpose.jpeg",
                "sample_exif_girl_transpose_thumbnail.jpeg"
            ),
            ImageResource(
                "sample_exif_girl_transverse.jpeg",
                "sample_exif_girl_transverse_thumbnail.jpeg"
            ),
        )
    }
    val divider = Arrangement.spacedBy(4.dp)

    Box(Modifier.fillMaxSize()) {
        val state: LazyGridState = rememberLazyGridState()
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = divider,
            verticalArrangement = divider,
            modifier = Modifier.fillMaxSize(),
            state = state,
        ) {
            itemsIndexed(imageResourceList) { index, imageResource ->
                Image(
                    painter = painterResource(imageResource.thumbnailResourcePath),
                    contentDescription = "image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable {
                            navigation.push(Page.Slideshow(imageResourceList, index))
                        }
                )
                // 不支持子采样，始终加载原图
//                val painter = asyncPainterResource(imageResource.resourcePath)
//                KamelImage(
//                    resource = painter,
//                    contentDescription = "image",
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .aspectRatio(1f)
//                )
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state
            )
        )
    }
}