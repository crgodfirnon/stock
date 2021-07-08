package com.example.stock.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stock.R
import com.example.stock.databinding.FragmentStockSymbolBinding
import com.example.stock.databinding.NewsItemBinding
import com.example.stock.domain.Article
import com.example.stock.viewmodels.StockSymbolViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import kotlinx.coroutines.launch


class StockSymbolFragment : Fragment() {

    private val viewModel: StockSymbolViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewmodel after onViewCreated()"
        }
        ViewModelProvider(this, StockSymbolViewModel.Factory(activity.application, args.tickerName)).get(
            StockSymbolViewModel::class.java)
    }

    private val args: StockSymbolFragmentArgs by navArgs()

    private var viewModelAdapter: NewsArticleAdapter? = null

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

        // configure ticker view
        binding.tickerCurrentPrice.animationInterpolator = OvershootInterpolator()

        configureCandleStickChart(binding)

        viewModelAdapter = NewsArticleAdapter(ArticleClick {
            viewModel.onArticleClicked(it)
        })

        viewModel.navigateToArticle.observe( viewLifecycleOwner, Observer { article ->
            article?.let {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                startActivity(intent)
                viewModel.navigateToArticleComplete()
            }
        })

        binding.tickerNews.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }

            /*binding.swipeRefresh.setOnRefreshListener {
                lifecycleScope.launch {
                    viewModel.refresh()
                }
                binding.swipeRefresh.isRefreshing = false
            }*/
            return binding.root
    }

    private fun configureCandleStickChart(binding: FragmentStockSymbolBinding) {
        binding.candleStickChart.setPinchZoom(false)
        binding.candleStickChart.setBackgroundColor(Color.WHITE)
        binding.candleStickChart.description.isEnabled = false
        binding.candleStickChart.setMaxVisibleValueCount(60)
        binding.candleStickChart.setDrawGridBackground(false)
        binding.candleStickChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.candleStickChart.xAxis.setDrawGridLines(false)
        binding.candleStickChart.axisLeft.setDrawGridLines(false)
        binding.candleStickChart.axisLeft.setDrawAxisLine(false)
        binding.candleStickChart.axisRight.setDrawAxisLine(false)
        binding.candleStickChart.legend.isEnabled = false
    }

    class ArticleClick(val block: (Article) -> Unit) {
        fun onClick(article: Article) = block(article)
    }

    class NewsArticleAdapter(val callBack: ArticleClick):
        ListAdapter<Article, NewsViewHolder>(NewsArticleDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
            val binding : NewsItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                NewsViewHolder.LAYOUT,
                parent,
                false
            )
            return NewsViewHolder(binding)
        }

        override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
            holder.viewDataBinding.also {
                it.article = getItem(position)
                it.articleCallBack = callBack
                it.executePendingBindings()
            }
        }

        class NewsArticleDiffCallback:
            DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.url == newItem.url
            }

        }
    }

    class NewsViewHolder(val viewDataBinding: NewsItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.news_item
        }
    }

}