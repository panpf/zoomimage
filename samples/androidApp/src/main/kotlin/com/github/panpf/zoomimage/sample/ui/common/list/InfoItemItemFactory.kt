/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.sample.ui.common.list

import android.content.Context
import androidx.core.view.isVisible
import com.github.panpf.zoomimage.sample.databinding.ListItemInfoBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingItemFactory
import com.github.panpf.zoomimage.sample.ui.model.InfoItem

class InfoItemItemFactory : BaseBindingItemFactory<InfoItem, ListItemInfoBinding>(InfoItem::class) {

    override fun initItem(
        context: Context,
        binding: ListItemInfoBinding,
        item: BindingItem<InfoItem, ListItemInfoBinding>
    ) {
    }

    override fun bindItemData(
        context: Context,
        binding: ListItemInfoBinding,
        item: BindingItem<InfoItem, ListItemInfoBinding>,
        bindingAdapterPosition: Int,
        absoluteAdapterPosition: Int,
        data: InfoItem
    ) {
        binding.titleText.text = data.title
        binding.titleText.isVisible = data.title != null
        binding.contentText.text = data.content
    }
}