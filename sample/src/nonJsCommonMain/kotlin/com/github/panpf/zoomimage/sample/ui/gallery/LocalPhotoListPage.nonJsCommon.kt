package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.github.panpf.zoomimage.sample.ui.gridCellsMinSize
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun LocalPhotoListPage(screen: Screen) {
    val navigator = LocalNavigator.current!!
    val localPhotoListViewModel: LocalPhotoListViewModel = koinViewModel()
    PagingPhotoList(
        photoPagingFlow = localPhotoListViewModel.pagingFlow,
        gridCellsMinSize = gridCellsMinSize,
        onClick = { photos, _, index ->
            val params = buildPhotoPagerScreenParams(photos, index)
            navigator.push(PhotoPagerScreen(params))
        },
    )
}