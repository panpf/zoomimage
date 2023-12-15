/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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
import com.github.panpf.zoomimage.sample.databinding.MultiChooseMenuItemBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingItemFactory

class MultiChooseMenuItemFactory(private val activity: Activity) :
    BaseBindingItemFactory<MultiChooseMenu, MultiChooseMenuItemBinding>(MultiChooseMenu::class) {

    override fun initItem(
        context: Context,
        binding: MultiChooseMenuItemBinding,
        item: BindingItem<MultiChooseMenu, MultiChooseMenuItemBinding>
    ) {
        binding.root.setOnClickListener {
            val data = item.dataOrThrow
            showDialog(data) {
                binding.multiChooseMenuItemInfoText.text =
                    data.getCheckedList().count { it }.toString()
            }
        }
    }

    override fun bindItemData(
        context: Context,
        binding: MultiChooseMenuItemBinding,
        item: BindingItem<MultiChooseMenu, MultiChooseMenuItemBinding>,
        bindingAdapterPosition: Int,
        absoluteAdapterPosition: Int,
        data: MultiChooseMenu
    ) {
        binding.multiChooseMenuItemTitleText.text = data.title
        binding.multiChooseMenuItemInfoText.text = data.getCheckedList().count { it }.toString()
    }

    private fun showDialog(data: MultiChooseMenu, after: () -> Unit) {
        AlertDialog.Builder(activity).apply {
            setMultiChoiceItems(
                data.values.toTypedArray(),
                data.getCheckedList().toBooleanArray()
            ) { _, which, isChecked ->
                data.onSelected(which, isChecked)
                after()
            }
        }.show()
    }
}
