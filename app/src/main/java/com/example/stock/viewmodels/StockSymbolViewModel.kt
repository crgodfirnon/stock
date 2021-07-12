package com.example.stock.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.stock.database.getDatabase
import com.example.stock.domain.Article
import com.example.stock.domain.TickerQuote
import com.example.stock.network.*
import com.example.stock.repository.TickerRepository
import com.github.mikephil.charting.data.CandleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException
import java.util.*

class StockSymbolViewModel(app: Application, val tickerName: String) : AndroidViewModel(app) {

    private val database = getDatabase(app)
    private val tickerRepository = TickerRepository(database)

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

    private val _isFollowingTicker : MutableLiveData<Boolean> = MutableLiveData()
    val isFollowingTicker: LiveData<Boolean>
        get() = _isFollowingTicker

    private val _followingEvent: MutableLiveData<Boolean> = MutableLiveData()
    val followingEvent: LiveData<Boolean>
        get() = _followingEvent

    init {
        viewModelScope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        withContext(Dispatchers.IO){

            // current quote data
            when(val result = tickerRepository.getTickerQuote(tickerName)){
                is TickerRepository.OperationResult.GetQuoteResult->
                    _currentQuote.postValue(result.quote)
            }

            // is the user following this ticker?
            _isFollowingTicker.postValue(
                tickerRepository.isFollowing(tickerName)
            )

            // candle stick for the last 30 days
            when(val stickDataResult = tickerRepository.getCandleStickData(tickerName)){
                is TickerRepository.OperationResult.GetCandleStickData->
                    _candleStickData.postValue(stickDataResult.data)
            }

            // news about the current ticker
            when(val articlesResult = tickerRepository.getTickerNews(tickerName)){
                is TickerRepository.OperationResult.GetTickerNewsResult->
                    _tickerArticles.postValue(articlesResult.articles)
            }
        }
    }

    fun onArticleClicked(article: Article) {
        _navigateToArticle.value = article
    }

    fun navigateToArticleComplete(){
        _navigateToArticle.value = null
    }

    fun followingEventComplete() {
        _followingEvent.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    fun followTicker() {
        viewModelScope.launch {
            val following = tickerRepository.toggleFollow(tickerName).isFollowing
            _isFollowingTicker.postValue(following)
            _followingEvent.postValue(following)
        }
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