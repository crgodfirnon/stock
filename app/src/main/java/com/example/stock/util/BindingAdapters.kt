package com.example.stock.ui

import android.graphics.Color
import android.graphics.Color.red
import android.text.format.DateUtils
import android.view.View
import android.widget.*
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.stock.R
import com.example.stock.R.color
import com.example.stock.domain.Article
import com.example.stock.domain.Ticker
import com.example.stock.domain.TickerQuote
import com.example.stock.viewmodels.StockMainViewModel
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.CandleData
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*


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
fun bindAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView, data: List<Ticker>?){
    val adapter = autoCompleteTextView.adapter as ArrayAdapter<String>
    adapter.clear()
    data?.let{
        adapter.addAll(*data.map{it.symbol}.toTypedArray())
    }
}

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Article>?){
    val adapter = recyclerView.adapter as StockSymbolFragment.NewsArticleAdapter
    adapter.submitList(data)
}

@JvmName("bindRecyclerView1")
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<TickerQuote>?) {
    val adapter = recyclerView.adapter as StockMainFragment.TickerQuoteAdapter
    adapter.submitList(data)
}

@BindingAdapter("data")
fun bindCandleStickChart(candleStickChart: CandleStickChart, data: CandleData?){
    candleStickChart.data = data
    candleStickChart.invalidate()
}

@BindingAdapter("stockQuote")
fun bindMovementString(textView: TextView, quote: TickerQuote?){
    quote?.let{
        val diff = quote.value - quote.prevClose
        val percent = kotlin.math.abs(diff) / quote.prevClose * 100

        if (diff < 0) {
            textView.setTextColor(Color.rgb(0xFF, 0x00, 0x00))
            textView.text = String.format("%.2f (%.2f%%) today", diff, percent)
        }
        else {
            textView.setTextColor(Color.rgb(0x1B, 0x66, 0x3E))
            textView.text = String.format("+%.2f (%.2f%%) today", diff, percent)
        }
    }
}

@BindingAdapter("onRefresh")
fun bindRefresh(swipeRefreshLayout: SwipeRefreshLayout, callBack: () -> Unit){
    swipeRefreshLayout.setOnRefreshListener(callBack)
}

@BindingAdapter("following")
fun updateFollowingImage(imageButton: ImageButton, following: Boolean?){
    if (following == true){
        imageButton.setImageResource(R.drawable.ic_baseline_remove_circle_outline_24)
    }
    else {
        imageButton.setImageResource(R.drawable.ic_baseline_add_circle_outline_24)
    }
}
