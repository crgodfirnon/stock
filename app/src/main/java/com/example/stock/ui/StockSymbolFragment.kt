package com.example.stock.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import com.google.android.material.snackbar.Snackbar


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

    private lateinit var binding: FragmentStockSymbolBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_stock_symbol,
            container,
            false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setVisibilityHelper(INVISIBLE)

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

        viewModel.followingEvent.observe(viewLifecycleOwner, Observer {
            it?.let {
                val displayText: String = if (it){
                    "Following ${viewModel.tickerName}"
                } else {
                    "Unfollowed ${viewModel.tickerName}"
                }
                Snackbar.make(binding.root, displayText, Snackbar.LENGTH_SHORT).show()
                viewModel.followingEventComplete()
            }
        })

        viewModel.dataState.observe(viewLifecycleOwner, Observer {
            it?.let{
                updateViews(it)
            }
        })

        viewModel.newsState.observe(viewLifecycleOwner, Observer {
            when(it){
                StockSymbolViewModel.DataState.Error-> {
                    binding.newsStatusText.text = "Network Error - Unable to retrieve news"
                    binding.newsStatusText.visibility = VISIBLE
                    binding.tickerNews.visibility = INVISIBLE
                }
                StockSymbolViewModel.DataState.Done -> {
                    if (viewModel.tickerArticles.value.isNullOrEmpty()){
                        binding.tickerNews.visibility = INVISIBLE
                        binding.newsStatusText.text = "No News Found"
                        binding.newsStatusText.visibility = VISIBLE
                    }
                }
                null -> {
                    return@Observer
                }
            }
            viewModel.newsStateHandled()
        })

        return binding.root
    }

    private fun updateViews(dataState: StockSymbolViewModel.DataState) {
        when(dataState){
            StockSymbolViewModel.DataState.Loading -> {
                binding.loadingImage.visibility = VISIBLE
                setVisibilityHelper(INVISIBLE)
            }
            StockSymbolViewModel.DataState.Done -> {
                binding.loadingImage.visibility = INVISIBLE
                setVisibilityHelper(VISIBLE)
            }
            StockSymbolViewModel.DataState.Error -> {
                binding.loadingImage.visibility = INVISIBLE
                setVisibilityHelper(INVISIBLE)
                binding.newsStatusText.visibility = INVISIBLE
                binding.networkErrorImage.visibility = VISIBLE
                Snackbar.make(binding.root, "Network Error - Unable to Retrieve Data", Snackbar.LENGTH_LONG).show()
            }
        }
        viewModel.dataStateEventFinished()
    }

    private fun setVisibilityHelper(option: Int) {
        binding.tickerNameTextView.visibility = option
        binding.tickerCurrentPrice.visibility = option
        binding.candleStickChart.visibility = option
        binding.openText.visibility = option
        binding.openValue.visibility = option
        binding.highText.visibility = option
        binding.highValue.visibility = option
        binding.lowText.visibility = option
        binding.lowValue.visibility = option
        binding.prevCloseText.visibility = option
        binding.prevCloseValue.visibility = option
        binding.movementText.visibility = option
        binding.tickerNews.visibility = option
        binding.followButton.visibility = option
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
        binding.candleStickChart.xAxis.setDrawLabels(false)
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