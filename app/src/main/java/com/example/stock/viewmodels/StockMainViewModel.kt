package com.example.stock.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.stock.domain.Article
import com.example.stock.domain.Ticker
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

    fun navigateToArticleFinished(){
        _navigateToArticleEvent.value = null
    }

    private fun getDummyArticles(): List<Article> {
        val article = Article(
            "NBC News",
            "2021-07-02T23:52:00Z",
            "North Miami Beach building deemed unsafe, evacuations ordered - NBC News",
            "Evacuations were ordered after a North Miami Beach condominium complex, the Crestview Towers, was deemed unsafe.",
            "https://www.nbcnews.com/news/us-news/north-miami-beach-building-deemed-unsafe-evacuations-ordered-n1273032",
            "https://media-cldnry.s-nbcnews.com/image/upload/t_nbcnews-fp-1200-630,f_auto,q_auto:best/newscms/2021_26/3488533/210702-crestview-towers-snip-wtvj-nbc6-ac-736p.jpg"
        )
        val article2 = Article(
            "BBC News",
            "2021-07-02T22:07:27Z",
            "Ethiopia's Tigray crisis: Rebel resurgence raises questions for Abiy Ahmed - BBC News",
            "The capture of the Tigrayan capital Mekelle puts huge pressure on Prime Minister Abiy Ahmed.",
            "https://www.bbc.com/news/world-africa-57693784",
            "https://ichef.bbci.co.uk/news/1024/branded_news/F9F4/production/_119188936_tigray.jpg"
        )

        return listOf(article2, article, article2, article)
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