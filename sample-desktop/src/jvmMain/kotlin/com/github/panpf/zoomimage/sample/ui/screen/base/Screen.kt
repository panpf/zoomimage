package com.github.panpf.zoomimage.sample.ui.screen.base

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.sample.ui.navigation.Navigation

@Composable
fun ToolbarScreen(navigation: Navigation, content: @Composable BoxScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        val theme = MaterialTheme.colorScheme
        Row(Modifier.fillMaxWidth().height(50.dp).background(theme.primary)) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(50.dp).clickable { navigation.back() }.padding(10.dp),
                tint = theme.onPrimary
            )
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            content()
        }
    }
}