package com.github.panpf.zoomimage.sample.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.github.panpf.zoomimage.sample.databinding.FragmentViewNavHostBinding
import com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingFragment

class ViewNavHostFragment : BaseBindingFragment<FragmentViewNavHostBinding>() {

    override fun onViewCreated(binding: FragmentViewNavHostBinding, savedInstanceState: Bundle?) {

    }

    @SuppressLint("RestrictedApi")
    override fun onFirstResume() {
        super.onFirstResume()
        findNavController().apply {
            setOnBackPressedDispatcher(requireActivity().onBackPressedDispatcher)
            enableOnBackPressed(true)
        }
    }
}