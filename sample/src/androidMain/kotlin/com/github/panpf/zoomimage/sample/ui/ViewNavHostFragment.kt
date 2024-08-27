package com.github.panpf.zoomimage.sample.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.github.panpf.zoomimage.sample.databinding.FragmentNavHostBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment

class ViewNavHostFragment : BaseBindingFragment<FragmentNavHostBinding>() {

    override fun onViewCreated(binding: FragmentNavHostBinding, savedInstanceState: Bundle?) {

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