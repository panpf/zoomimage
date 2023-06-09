/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.zoomimage.sample.ui.common.view.menu

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.github.panpf.zoomimage.sample.databinding.MultiSelectMenuItemBinding
import com.github.panpf.zoomimage.sample.ui.base.view.MyBindingItemFactory

class MultiSelectMenuItemFactory(private val activity: Activity) :
    MyBindingItemFactory<MultiSelectMenu, MultiSelectMenuItemBinding>(MultiSelectMenu::class) {

    override fun initItem(
        context: Context,
        binding: MultiSelectMenuItemBinding,
        item: BindingItem<MultiSelectMenu, MultiSelectMenuItemBinding>
    ) {
        binding.root.setOnClickListener {
            val data = item.dataOrThrow
            showDialog(data) {
                binding.multiSelectMenuItemInfoText.text = data.getValue()
            }
        }
    }

    override fun bindItemData(
        context: Context,
        binding: MultiSelectMenuItemBinding,
        item: BindingItem<MultiSelectMenu, MultiSelectMenuItemBinding>,
        bindingAdapterPosition: Int,
        absoluteAdapterPosition: Int,
        data: MultiSelectMenu
    ) {
        binding.multiSelectMenuItemTitleText.text = data.title
        binding.multiSelectMenuItemInfoText.text = data.getValue()
    }

    private fun showDialog(data: MultiSelectMenu, after: () -> Unit) {
        AlertDialog.Builder(activity).apply {
            setItems(data.values.toTypedArray()) { _, which ->
                data.onSelect(which, data.values[which])
                after()
            }
        }.show()
    }
}
