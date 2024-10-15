package com.github.panpf.zoomimage.sample.ui.gallery

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.panpf.assemblyadapter.recycler.ItemSpan
import com.github.panpf.assemblyadapter.recycler.divider.Divider
import com.github.panpf.assemblyadapter.recycler.divider.newAssemblyGridDividerItemDecoration
import com.github.panpf.assemblyadapter.recycler.divider.newAssemblyStaggeredGridDividerItemDecoration
import com.github.panpf.assemblyadapter.recycler.newAssemblyGridLayoutManager
import com.github.panpf.assemblyadapter.recycler.newAssemblyStaggeredGridLayoutManager
import com.github.panpf.assemblyadapter.recycler.paging.AssemblyPagingDataAdapter
import com.github.panpf.tools4a.dimen.ktx.dp2px
import com.github.panpf.tools4k.lang.asOrThrow
import com.github.panpf.zoomimage.sample.NavMainDirections
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.databinding.FragmentPhotoListBinding
import com.github.panpf.zoomimage.sample.getViewImageLoaderIcon
import com.github.panpf.zoomimage.sample.ui.SwitchImageLoaderDialogFragment
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
import com.github.panpf.zoomimage.sample.ui.common.list.LoadStateItemFactory
import com.github.panpf.zoomimage.sample.ui.common.list.MyLoadStateAdapter
import com.github.panpf.zoomimage.sample.ui.examples.BasePhotoGridItemFactory
import com.github.panpf.zoomimage.sample.ui.examples.CoilPhotoGridItemFactory
import com.github.panpf.zoomimage.sample.ui.examples.GlidePhotoGridItemFactory
import com.github.panpf.zoomimage.sample.ui.examples.PicassoPhotoGridItemFactory
import com.github.panpf.zoomimage.sample.ui.examples.SketchPhotoGridItemFactory
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.ui.model.PhotoDiffCallback
import com.github.panpf.zoomimage.sample.util.ignoreFirst
import com.github.panpf.zoomimage.sample.util.repeatCollectWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

abstract class BasePhotoListFragment : BaseBindingFragment<FragmentPhotoListBinding>() {

    abstract val animatedPlaceholder: Boolean
    abstract val photoPagingFlow: Flow<PagingData<Photo>>

    private var pagingFlowCollectJob: Job? = null
    private var loadStateFlowCollectJob: Job? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(
        binding: FragmentPhotoListBinding,
        savedInstanceState: Bundle?
    ) {
        binding.layoutImage.apply {
            val appSettings = context.appSettings
            appSettings.staggeredGridMode
                .repeatCollectWithLifecycle(viewLifecycleOwner, State.CREATED) {
                    val iconResId =
                        if (it) R.drawable.ic_layout_grid else R.drawable.ic_layout_grid_staggered
                    setImageResource(iconResId)
                }
            setOnClickListener {
                appSettings.staggeredGridMode.value = !appSettings.staggeredGridMode.value
            }
        }

        binding.imageLoader.apply {
            setOnClickListener {
                SwitchImageLoaderDialogFragment().show(childFragmentManager, null)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                appSettings.viewImageLoader.collect { viewImageLoaderName ->
                    setImageDrawable(getViewImageLoaderIcon(requireContext(), viewImageLoaderName))
                }
            }
        }

        binding.recycler.apply {
            setPadding(0, 0, 0, 80.dp2px)
            clipToPadding = false

            appSettings.staggeredGridMode
                .repeatCollectWithLifecycle(
                    viewLifecycleOwner,
                    State.CREATED
                ) { staggeredGridMode ->
                    val (layoutManager1, itemDecoration) =
                        newLayoutManagerAndItemDecoration(staggeredGridMode)
                    layoutManager = layoutManager1
                    (0 until itemDecorationCount).forEach { index ->
                        removeItemDecorationAt(index)
                    }
                    addItemDecoration(itemDecoration)

                    resetAdapter(binding)
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            appSettings.viewImageLoader.ignoreFirst().collect {
                resetAdapter(binding)
            }
        }
    }

    private fun resetAdapter(binding: FragmentPhotoListBinding) {
        val pagingAdapter = newPagingAdapter(binding)
        val loadStateAdapter = MyLoadStateAdapter().apply {
            noDisplayLoadStateWhenPagingEmpty(pagingAdapter)
        }
        binding.recycler.adapter = pagingAdapter.withLoadStateFooter(loadStateAdapter)

        bindRefreshAndAdapter(binding, pagingAdapter)
    }

    private fun newLayoutManagerAndItemDecoration(staggeredGridMode: Boolean): Pair<RecyclerView.LayoutManager, RecyclerView.ItemDecoration> {
        val layoutManager: RecyclerView.LayoutManager
        val itemDecoration: RecyclerView.ItemDecoration
        if (staggeredGridMode) {
            layoutManager = newAssemblyStaggeredGridLayoutManager(
                3,
                StaggeredGridLayoutManager.VERTICAL
            ) {
                fullSpanByItemFactory(LoadStateItemFactory::class)
            }
            itemDecoration =
                requireContext().newAssemblyStaggeredGridDividerItemDecoration {
                    val gridDivider =
                        requireContext().resources.getDimensionPixelSize(R.dimen.grid_divider)
                    divider(Divider.space(gridDivider))
                    sideDivider(Divider.space(gridDivider))
                    useDividerAsHeaderAndFooterDivider()
                    useSideDividerAsSideHeaderAndFooterDivider()
                }
        } else {
            layoutManager =
                requireContext().newAssemblyGridLayoutManager(
                    3,
                    GridLayoutManager.VERTICAL
                ) {
                    itemSpanByItemFactory(LoadStateItemFactory::class, ItemSpan.fullSpan())
                }
            itemDecoration = requireContext().newAssemblyGridDividerItemDecoration {
                val gridDivider =
                    requireContext().resources.getDimensionPixelSize(R.dimen.grid_divider)
                divider(Divider.space(gridDivider))
                sideDivider(Divider.space(gridDivider))
                useDividerAsHeaderAndFooterDivider()
                useSideDividerAsSideHeaderAndFooterDivider()
            }
        }
        return layoutManager to itemDecoration
    }

    private fun newPagingAdapter(binding: FragmentPhotoListBinding): PagingDataAdapter<*, *> {
        return AssemblyPagingDataAdapter(
            itemFactoryList = listOf(
                newPhotoGridItemFactory()
                    .setOnViewClickListener(R.id.image) { _, _, _, absoluteAdapterPosition, _ ->
                        startPhotoPager(binding, absoluteAdapterPosition)
                    }
            ),
            diffCallback = PhotoDiffCallback()
        ).apply {
            pagingFlowCollectJob?.cancel()
            pagingFlowCollectJob = viewLifecycleOwner.lifecycleScope.launch {
                photoPagingFlow.collect {
                    submitData(it)
                }
            }
        }
    }

    private fun newPhotoGridItemFactory(): BasePhotoGridItemFactory {
        return when (val viewImageLoader = appSettings.viewImageLoader.value) {
            "Sketch", "Basic" -> SketchPhotoGridItemFactory(childFragmentManager)
            "Coil" -> CoilPhotoGridItemFactory(childFragmentManager)
            "Glide" -> GlidePhotoGridItemFactory()
            "Picasso" -> PicassoPhotoGridItemFactory()
            else -> throw IllegalArgumentException("Unknown viewImageLoader: $viewImageLoader")
        }
    }

    private fun bindRefreshAndAdapter(
        binding: FragmentPhotoListBinding,
        pagingAdapter: PagingDataAdapter<*, *>
    ) {
        binding.refresh.setOnRefreshListener {
            pagingAdapter.refresh()
        }
        loadStateFlowCollectJob?.cancel()
        loadStateFlowCollectJob =
            viewLifecycleOwner.lifecycleScope.launch {
                pagingAdapter.loadStateFlow.collect { loadStates ->
                    when (val refreshState = loadStates.refresh) {
                        is LoadState.Loading -> {
                            binding.state.gone()
                            binding.refresh.isRefreshing = true
                        }

                        is LoadState.Error -> {
                            binding.refresh.isRefreshing = false
                            binding.state.error {
                                message(refreshState.error)
                                retryAction {
                                    pagingAdapter.refresh()
                                }
                            }
                        }

                        is LoadState.NotLoading -> {
                            binding.refresh.isRefreshing = false
                            if (pagingAdapter.itemCount <= 0) {
                                binding.state.empty {
                                    message("No Photos")
                                }
                            } else {
                                binding.state.gone()
                            }
                        }
                    }
                }
            }
    }

    private fun startPhotoPager(binding: FragmentPhotoListBinding, position: Int) {
        val items = binding.recycler
            .adapter!!.asOrThrow<ConcatAdapter>()
            .adapters.first().asOrThrow<AssemblyPagingDataAdapter<Photo>>()
            .currentList
        val totalCount = items.size
        val startPosition = (position - 100).coerceAtLeast(0)
        val endPosition = (position + 100).coerceAtMost(items.size - 1)
        val photos = items.asSequence()
            .filterNotNull()
            .filterIndexed { index, _ -> index in startPosition..endPosition }
            .toList()
        val photosJsonString = Json.encodeToString(photos)
        findNavController().navigate(
            NavMainDirections.actionGlobalPhotoPagerFragment(
                photos = photosJsonString,
                totalCount = totalCount,
                startPosition = startPosition,
                initialPosition = position,
            ),
        )
    }
}