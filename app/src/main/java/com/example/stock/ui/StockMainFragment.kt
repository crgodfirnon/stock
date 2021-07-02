package com.example.stock.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.stock.R
import com.example.stock.databinding.FragmentStockMainBinding
import com.example.stock.viewmodels.StockMainViewModel


class StockMainFragment : Fragment() {

    private val viewModel: StockMainViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewmodel after onViewCreated()"
        }
        ViewModelProvider(this, StockMainViewModel.Factory(activity.application)).get(StockMainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Setting up the binding
        val binding: FragmentStockMainBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_stock_main,
            container,
            false)

        binding.viewModel = viewModel

        // Inflate the layout for this fragment
        return binding.root
    }
}