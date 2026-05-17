package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun PexelsPhotoListPage(screen: Screen) {
    val navigator = LocalNavigator.current!!
    val pexelsPhotoListViewModel: PexelsPhotoListViewModel = koinViewModel()
    PhotoList(
        photoPaging = pexelsPhotoListViewModel.photoPaging,
        modifier = Modifier.fillMaxSize(),
        gridCellsMinSize = 150.dp,
        onClick = { photos1, _, index ->
            val params = buildPhotoPagerScreenParams(photos1, index)
            navigator.push(PhotoPagerScreen(params))
        }
    )
}