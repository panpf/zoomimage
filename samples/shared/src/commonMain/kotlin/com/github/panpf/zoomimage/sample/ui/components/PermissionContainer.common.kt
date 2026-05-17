package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun PermissionContainer(
    permission: Any?,
    permissionRequired: Boolean,
    content: @Composable () -> Unit
)