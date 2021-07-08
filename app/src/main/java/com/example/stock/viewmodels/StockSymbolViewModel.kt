package com.example.stock.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stock.domain.TickerQuote
import com.example.stock.network.Network
import com.example.stock.network.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class StockSymbolViewModel(app: Application, val tickerName: String) : AndroidViewModel(app) {

    private val _currentPrice : MutableLiveData<Double> = MutableLiveData()
    val currentPrice: LiveData<Double>
        get() = _currentPrice

    val currentPriceString: LiveData<String> = Transformations.map(currentPrice) {
            it?.let{
                "$$it"
            }
        }

    init {
        _currentPrice.value = 0.0

        viewModelScope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        withContext(Dispatchers.IO){
            try{
                val price = Network.tickers.getQuote(tickerName)
                _currentPrice.postValue(price.body()?.asDomainModel(tickerName)?.value)
            }
            catch(e: Exception)
            {

            }
        }
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