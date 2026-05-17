package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun PermissionContainer(
    permission: Any?,
    permissionRequired: Boolean,
    content: @Composable () -> Unit
) {
    content()
}