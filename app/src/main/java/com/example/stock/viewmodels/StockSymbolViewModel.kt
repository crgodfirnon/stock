package com.example.stock.viewmodels

import android.app.Application
import android.util.Log
import android.util.Log.ERROR
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stock.domain.Article
import com.example.stock.domain.TickerQuote
import com.example.stock.network.*
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException
import java.util.*

class StockSymbolViewModel(app: Application, val tickerName: String) : AndroidViewModel(app) {


    private val _currentQuote : MutableLiveData<TickerQuote> = MutableLiveData()
    val currentQuote: LiveData<TickerQuote>
        get() = _currentQuote

    private val _candleStickData: MutableLiveData<CandleData> = MutableLiveData()
    val candleStickData: LiveData<CandleData>
        get() = _candleStickData

    private val _tickerArticles: MutableLiveData<List<Article>> = MutableLiveData()
    val tickerArticles: LiveData<List<Article>>
        get() = _tickerArticles

    private val _navigateToArticle: MutableLiveData<Article> = MutableLiveData()
    val navigateToArticle : LiveData<Article>
        get() = _navigateToArticle

    init {
        viewModelScope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        withContext(Dispatchers.IO){
            try{
                // current quote data
                val price = Network.tickers.getQuote(tickerName)
                _currentQuote.postValue(price.body()?.asDomainModel(tickerName))

                // candle stick for the last 30 days
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -30)
                val fromString = (cal.timeInMillis/1000).toString()

                val candleData = Network.tickers.getCandleStickData(
                    tickerName, fromString, (System.currentTimeMillis()/1000).toString())
                _candleStickData.postValue(CandleData(candleData.body()?.asDomainModel()))

                // news about the current ticker
                val articleResponse = Network.tickers.getCompanyNews(tickerName, "2021-07-01", "2021-07-07")
                val companyArticles = companyArticleAdapter.fromJson(articleResponse.body()?.string())
                val domainArticles = CompanyArticleResponseContainer(companyArticles!!).asDomainModel()
                _tickerArticles.postValue(domainArticles.toList())

            }
            catch(e: Exception)
            {
            }
        }
    }

    fun onArticleClicked(article: Article) {
        _navigateToArticle.value = article
    }

    fun navigateToArticleComplete(){
        _navigateToArticle.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    class Factory(val app: Application, val ticker: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StockSymbolViewModel::class.java)) {
                return StockSymbolViewModel(app, ticker) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}