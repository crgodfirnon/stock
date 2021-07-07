package com.example.stock.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.stock.R
import com.example.stock.databinding.FragmentStockSymbolBinding
import com.example.stock.viewmodels.StockMainViewModel
import com.example.stock.viewmodels.StockSymbolViewModel


class StockSymbolFragment : Fragment() {

    private val viewModel: StockSymbolViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewmodel after onViewCreated()"
        }
        ViewModelProvider(this, StockSymbolViewModel.Factory(activity.application)).get(
            StockSymbolViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding : FragmentStockSymbolBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_stock_symbol,
            container,
            false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stock_symbol, container, false)
    }
}