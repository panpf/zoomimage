package com.github.panpf.zoomimage.sample.ui.model

import androidx.navigation.NavDirections
import com.github.panpf.assemblyadapter.recycler.DiffKey

data class ViewTestItem(
    val title: String,
    val navDirections: NavDirections,
) : DiffKey {

    override val diffKey: String = "Link-$title"
}