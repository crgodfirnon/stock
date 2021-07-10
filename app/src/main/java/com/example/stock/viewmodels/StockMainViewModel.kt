package com.example.stock.viewmodels

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stock.database.TickersDatabase
import com.example.stock.database.getDatabase
import com.example.stock.domain.Article
import com.example.stock.domain.Ticker
import com.example.stock.network.Network
import com.example.stock.network.asDomainModel
import com.example.stock.repository.TickerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class StockMainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val tickerRepository = TickerRepository(database)

    val availableTickers = tickerRepository.availableTickers
    val followedTickerQuotes = tickerRepository.followedTickerQuotes

    init {
        viewModelScope.launch {
            tickerRepository.refreshFollowedTickerQuotes()
        }
    }

    private val _tickerNotFoundEvent: MutableLiveData<String> = MutableLiveData()
    val tickerNotFoundEvent: LiveData<String>
        get() = _tickerNotFoundEvent

    private val _navigateToTickerEvent: MutableLiveData<String> = MutableLiveData()
    val navigateToTickerEvent: LiveData<String>
        get() = _navigateToTickerEvent

    private val _refreshCompleteEvent: MutableLiveData<Boolean> = MutableLiveData()
    val refreshCompleteEvent: LiveData<Boolean>
        get() = _refreshCompleteEvent

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    fun tickerNotFoundEventComplete() {
        _tickerNotFoundEvent.value = null
    }


    fun navigateToTickerComplete(){
        _navigateToTickerEvent.value = null
    }

    fun searchTicker(ticker: String) {
        if (availableTickers.value?.find{ it.symbol == ticker} != null){
            _navigateToTickerEvent.value = ticker
        }
        else {
            _tickerNotFoundEvent.value = ticker
        }
    }

    fun refreshFollowedTickers() {
        viewModelScope.launch {
            tickerRepository.refreshFollowedTickerQuotes()
            _refreshCompleteEvent.postValue(true)
        }
    }

    fun refreshEventFinished(){
        _refreshCompleteEvent.value = false
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