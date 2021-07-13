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

    enum class DataState {Loading, Done, Error}

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

    private val _dataState: MutableLiveData<DataState> = MutableLiveData()
    val dataState: LiveData<DataState>
        get() = _dataState

    private val _newsState: MutableLiveData<DataState> = MutableLiveData()
    val newsState: LiveData<DataState>
        get() = _newsState

    init {
        viewModelScope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        withContext(Dispatchers.IO){

            _dataState.postValue(DataState.Loading)

            // is the user following this ticker?
            _isFollowingTicker.postValue(
                tickerRepository.isFollowing(tickerName)
            )

            // current quote data
            when(val result = tickerRepository.getTickerQuote(tickerName)){
                is TickerRepository.OperationResult.GetQuoteResult->
                    _currentQuote.postValue(result.quote)
                is TickerRepository.OperationResult.Fail-> {
                    _dataState.postValue(DataState.Error)
                    return@withContext
                }
            }

            // candle stick for the last 30 days
            when(val stickDataResult = tickerRepository.getCandleStickData(tickerName)){
                is TickerRepository.OperationResult.GetCandleStickData->
                    _candleStickData.postValue(stickDataResult.data)
                is TickerRepository.OperationResult.Fail->{
                    _dataState.postValue(DataState.Error)
                    return@withContext
                }
            }

            // news about the current ticker
            _newsState.postValue(DataState.Loading)
            when(val articlesResult = tickerRepository.getTickerNews(tickerName)){
                is TickerRepository.OperationResult.GetTickerNewsResult->{
                    _tickerArticles.postValue(articlesResult.articles.take(15))
                    _newsState.postValue(DataState.Done)
                }
                is TickerRepository.OperationResult.Fail -> {
                    _newsState.postValue(DataState.Error)
                    return@withContext
                }
            }

            _dataState.postValue(DataState.Done)
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

    fun dataStateEventFinished() {
        _dataState.value = null
    }

    fun newsStateHandled() {
        _newsState.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    fun followTicker() {
        viewModelScope.launch {
            val following = tickerRepository.toggleFollow(tickerName)
            when(following){
                is TickerRepository.OperationResult.ToggleFollowResult-> {
                    _isFollowingTicker.postValue(following.isFollowing)
                    _followingEvent.postValue(following.isFollowing)
                }
            }
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