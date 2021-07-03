package com.example.stock.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stock.R
import com.example.stock.databinding.FragmentStockMainBinding
import com.example.stock.databinding.NewsItemBinding
import com.example.stock.domain.Article
import com.example.stock.viewmodels.StockMainViewModel


class StockMainFragment : Fragment() {

    private val viewModel: StockMainViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewmodel after onViewCreated()"
        }
        ViewModelProvider(this, StockMainViewModel.Factory(activity.application)).get(StockMainViewModel::class.java)
    }

    private var viewModelAdapter: NewsArticleAdapter? = null

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

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModelAdapter = NewsArticleAdapter(ArticleClick {
            viewModel.onArticleClicked(it)
        })
        viewModelAdapter?.submitList( viewModel.articles.value )

        binding.newsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }

        viewModel.navigateToArticleEvent.observe( viewLifecycleOwner, Observer { article ->
            article?.let {
                var intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                startActivity(intent)
                viewModel.navigateToArticleFinished()
            }
        })

        return binding.root
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