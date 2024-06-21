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

package com.github.panpf.zoomimage.sample.ui.base.view

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.github.panpf.tools4a.display.ktx.getStatusBarHeight
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.util.view.createViewBinding

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseToolbarBindingFragment<VIEW_BINDING : ViewBinding> : Fragment() {

    protected var toolbar: Toolbar? = null
    protected var binding: VIEW_BINDING? = null

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_toolbar_page, container, false).apply {
        val statusBarBgView = findViewById<View>(R.id.statusBarBgView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val contentContainer = findViewById<FrameLayout>(R.id.content)

        setTransparentStatusBar(statusBarBgView)

        @Suppress("UNCHECKED_CAST")
        val binding = createViewBinding(inflater, contentContainer) as VIEW_BINDING
        contentContainer.addView(binding.root)

        this@BaseToolbarBindingFragment.toolbar = toolbar
        this@BaseToolbarBindingFragment.binding = binding
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ObsoleteSdkInt")
    private fun setTransparentStatusBar(statusBarBgView: View) {
        val window = requireActivity().window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            && (window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) != 0
        ) {
            statusBarBgView.updateLayoutParams {
                height = statusBarBgView.context.getStatusBarHeight()
            }
        }
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreated(this.toolbar!!, this.binding!!, savedInstanceState)
    }

    protected open fun onViewCreated(
        toolbar: Toolbar,
        binding: VIEW_BINDING,
        savedInstanceState: Bundle?
    ) {

    }

    override fun onDestroyView() {
        this.binding = null
        this.toolbar = null
        super.onDestroyView()
    }
}