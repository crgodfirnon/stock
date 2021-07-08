package com.example.stock.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.stock.R
import com.example.stock.databinding.FragmentStockSymbolBinding
import com.example.stock.viewmodels.StockSymbolViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class StockSymbolFragment : Fragment() {

    private val viewModel: StockSymbolViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewmodel after onViewCreated()"
        }
        ViewModelProvider(this, StockSymbolViewModel.Factory(activity.application, args.tickerName)).get(
            StockSymbolViewModel::class.java)
    }

    private val args: StockSymbolFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding : FragmentStockSymbolBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_stock_symbol,
            container,
            false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.tickerCurrentPrice.animationInterpolator = OvershootInterpolator()

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.refresh()
            }
            binding.swipeRefresh.isRefreshing = false
        }
        return binding.root
    }
}