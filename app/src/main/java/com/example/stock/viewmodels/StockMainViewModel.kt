package com.example.stock.viewmodels

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import com.example.stock.domain.Article
import com.example.stock.network.Network
import com.example.stock.network.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class StockMainViewModel(application: Application) : AndroidViewModel(application) {

    enum class ArticleApiStatus { LOADING, ERROR, DONE }

    private val _articleApiStatus = MutableLiveData<ArticleApiStatus>()
    val articleApiStatus: LiveData<ArticleApiStatus>
        get() = _articleApiStatus

    private val _articles: MutableLiveData<List<Article>> = MutableLiveData()
    val articles: LiveData<List<Article>>
        get() = _articles

    private val _navigateToArticleEvent : MutableLiveData<Article?> = MutableLiveData()
    val navigateToArticleEvent : LiveData<Article?>
        get() = _navigateToArticleEvent

    private val _searchButtonEvent: MutableLiveData<Boolean> = MutableLiveData()
    val searchButtonEvent: LiveData<Boolean>
        get() = _searchButtonEvent

    private val _tickerNotFoundEvent: MutableLiveData<String> = MutableLiveData()
    val tickerNotFoundEvent: LiveData<String>
        get() = _tickerNotFoundEvent

    private val _navigateToTickerEvent: MutableLiveData<String> = MutableLiveData()
    val navigateToTickerEvent: LiveData<String>
        get() = _navigateToTickerEvent

    init {

        viewModelScope.launch {
            //refreshArticles()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    fun searchButtonEventComplete() {
        _searchButtonEvent.value = false
    }

    fun tickerNotFoundEventComplete() {
        _tickerNotFoundEvent.value = null
    }

    private suspend fun refreshArticles() {
        withContext(Dispatchers.IO) {
            try {
                _articleApiStatus.postValue(ArticleApiStatus.LOADING)
                val articlesResponse = Network.articles.getArticles(query = "stock market")
                _articleApiStatus.postValue(ArticleApiStatus.DONE)
                _articles.postValue(articlesResponse.body()?.asDomainModel()?.toList())
            }
            catch(e: Exception){
                _articleApiStatus.postValue(ArticleApiStatus.ERROR)
                _articles.postValue(emptyList())
            }
        }
    }

    fun onArticleClicked(article: Article){
        _navigateToArticleEvent.value = article
    }

    fun navigateToArticleComplete(){
        _navigateToArticleEvent.value = null
    }

    fun navigateToTickerComplete(){
        _navigateToTickerEvent.value = null
    }

    fun searchTicker(ticker: String) {
        // check that this ticker exists
        //_tickerNotFoundEvent.value = ticker

        _navigateToTickerEvent.value = ticker
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StockMainViewModel::class.java)) {
                return StockMainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}