package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.sample.ui.LocalNavBackStack
import com.github.panpf.zoomimage.sample.ui.PhotoPagerRoute
import com.github.panpf.zoomimage.sample.ui.gridCellsMinSize
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PexelsPhotoListPage() {
    val navBackStack = LocalNavBackStack.current
    val pexelsPhotoListViewModel: PexelsPhotoListViewModel = koinViewModel()
    PagingPhotoList(
        photoPagingFlow = pexelsPhotoListViewModel.pagingFlow,
        gridCellsMinSize = gridCellsMinSize,
        onClick = { photos, _, index ->
            val params = buildPhotoPagerScreenParams(photos, index)
            navBackStack.add(PhotoPagerRoute(params))
        },
    )
}