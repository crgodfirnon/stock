package com.example.stock.ui

import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.stock.R
import com.example.stock.domain.Article
import com.example.stock.viewmodels.StockMainViewModel
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("articleApiStatus")
fun bindArticleApiStatus(statusImageView: ImageView, status: StockMainViewModel.ArticleApiStatus?){
    when(status){
        StockMainViewModel.ArticleApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
        }
        StockMainViewModel.ArticleApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
        }
        StockMainViewModel.ArticleApiStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
    }
}


@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_broken_image)
            .into(imgView)
    }
}

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Article>?){
    val adapter = recyclerView.adapter as StockMainFragment.NewsArticleAdapter
    adapter.submitList(data)
}
