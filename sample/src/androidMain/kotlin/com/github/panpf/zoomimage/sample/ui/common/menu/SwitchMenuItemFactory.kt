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
import com.github.panpf.zoomimage.sample.databinding.ListItemMenuSwitchBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingItemFactory

class SwitchMenuItemFactory :
    BaseBindingItemFactory<SwitchMenu, ListItemMenuSwitchBinding>(SwitchMenu::class) {

    override fun initItem(
        context: Context,
        binding: ListItemMenuSwitchBinding,
        item: BindingItem<SwitchMenu, ListItemMenuSwitchBinding>
    ) {
        binding.root.setOnClickListener {
            binding.switchView.isChecked = !binding.switchView.isChecked
        }
        binding.root.setOnLongClickListener {
            val data = item.dataOrThrow
            data.onLongClick?.invoke()
            true
        }
        binding.switchView.setOnCheckedChangeListener { _, isChecked ->
            val data = item.dataOrThrow
            if (data.isChecked != isChecked) {
                data.isChecked = isChecked
            }
        }
    }

    override fun bindItemData(
        context: Context,
        binding: ListItemMenuSwitchBinding,
        item: BindingItem<SwitchMenu, ListItemMenuSwitchBinding>,
        bindingAdapterPosition: Int,
        absoluteAdapterPosition: Int,
        data: SwitchMenu
    ) {
        binding.root.isEnabled = !data.disabled
        binding.titleText.isEnabled = !data.disabled
        binding.switchView.isEnabled = !data.disabled

        binding.titleText.text = data.title
        binding.switchView.isChecked = if (data.disabled) false else data.isChecked
    }
}
