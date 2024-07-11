package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.zoomimage.sample.ui.model.InfoItem
import kotlinx.collections.immutable.ImmutableList

@Composable
fun InfoItems(infoItems: ImmutableList<InfoItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp)
    ) {
        items(infoItems.size) {
            if (it > 0) {
                Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))
            }
            InfoItem(infoItems[it])
        }
    }
}

@Composable
fun InfoItem(infoItem: InfoItem) {
    Column {
        if (infoItem.title != null) {
            Text(
                text = infoItem.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = infoItem.content,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
    }
}