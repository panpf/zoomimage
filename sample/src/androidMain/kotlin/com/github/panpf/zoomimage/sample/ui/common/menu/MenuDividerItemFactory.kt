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

package com.github.panpf.zoomimage.sample.ui.common.menu

import android.content.Context
import com.github.panpf.zoomimage.sample.databinding.ListItemMenuDividerBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingItemFactory

class MenuDividerItemFactory :
    BaseBindingItemFactory<MenuDivider, ListItemMenuDividerBinding>(MenuDivider::class) {

    override fun initItem(
        context: Context,
        binding: ListItemMenuDividerBinding,
        item: BindingItem<MenuDivider, ListItemMenuDividerBinding>
    ) {

    }

    override fun bindItemData(
        context: Context,
        binding: ListItemMenuDividerBinding,
        item: BindingItem<MenuDivider, ListItemMenuDividerBinding>,
        bindingAdapterPosition: Int,
        absoluteAdapterPosition: Int,
        data: MenuDivider
    ) {

    }
}
