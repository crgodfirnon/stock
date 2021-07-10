package com.example.stock.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stock.R
import com.example.stock.databinding.FragmentStockMainBinding
import com.example.stock.databinding.NewsItemBinding
import com.example.stock.databinding.TickerItemBinding
import com.example.stock.domain.Article
import com.example.stock.domain.TickerQuote
import com.example.stock.viewmodels.StockMainViewModel
import com.robinhood.ticker.TickerView


class StockMainFragment : Fragment() {

    private val viewModel: StockMainViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewmodel after onViewCreated()"
        }
        ViewModelProvider(this, StockMainViewModel.Factory(activity.application)).get(
            StockMainViewModel::class.java
        )
    }

    private var recyclerAdapter: TickerQuoteAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Setting up the binding
        val binding: FragmentStockMainBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_stock_main,
            container,
            false
        )

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        recyclerAdapter = TickerQuoteAdapter(TickerClick {
            viewModel.searchTicker(it.name)
        })
        binding.followingList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerAdapter
        }

        binding.symbolSearchTextView.setAdapter(
            ArrayAdapter<String>(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item
            )
        )

        viewModel.tickerNotFoundEvent.observe(viewLifecycleOwner, Observer {
            it?.let {
                Toast.makeText(
                    context,
                    viewModel.tickerNotFoundEvent.value + " Not Found",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.tickerNotFoundEventComplete()
            }
        })

        viewModel.navigateToTickerEvent.observe(viewLifecycleOwner, Observer {
            it?.let {
                findNavController().navigate(
                    StockMainFragmentDirections.actionStockMainFragmentToStockSymbolFragment(
                        viewModel.navigateToTickerEvent.value!!
                    )
                )
                viewModel.navigateToTickerComplete()
            }
        })

        viewModel.refreshCompleteEvent.observe(viewLifecycleOwner, Observer {
            if(it == true){
                binding.swipeRefresh.isRefreshing = false
                viewModel.refreshEventFinished()
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    class TickerClick(val block: (TickerQuote) -> Unit) {
        fun onClick(ticker: TickerQuote) = block(ticker)
    }

    class TickerQuoteAdapter(val callBack: TickerClick) :
        ListAdapter<TickerQuote, TickerQuoteViewHolder>(TickerDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TickerQuoteViewHolder {
            val binding: TickerItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                TickerQuoteViewHolder.LAYOUT,
                parent,
                false
            )
            return TickerQuoteViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TickerQuoteViewHolder, position: Int) {
            holder.viewDataBinding.also {
                it.ticker = getItem(position)
                it.tickerCallBack = callBack
                it.executePendingBindings()
            }
        }

        class TickerDiffCallback :
            DiffUtil.ItemCallback<TickerQuote>() {
            override fun areItemsTheSame(oldItem: TickerQuote, newItem: TickerQuote): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: TickerQuote, newItem: TickerQuote): Boolean {
                return oldItem.name == newItem.name
            }

        }
    }

    class TickerQuoteViewHolder(val viewDataBinding: TickerItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.ticker_item
        }
    }
}