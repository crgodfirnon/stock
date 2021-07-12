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
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class StockMainViewModel(application: Application) : AndroidViewModel(application) {

    enum class DataState{
        Loading,
        Done,
        Error
    }

    private val database = getDatabase(application)
    private val tickerRepository = TickerRepository(database)

    val availableTickers = tickerRepository.availableTickers
    val followedTickerQuotes = tickerRepository.followedTickerQuotes

    private val _dataState : MutableLiveData<DataState> = MutableLiveData()
    val dataState: LiveData<DataState>
        get() = _dataState

    init {
        viewModelScope.launch {
            if (availableTickers.value == null || availableTickers.value!!.isEmpty()){
                if (tickerRepository.refreshAvailableTickers() == TickerRepository.OperationResult.Fail){
                    _dataState.postValue(DataState.Error)
                    return@launch
                }
            }

            _dataState.postValue(DataState.Loading)
            when(tickerRepository.refreshFollowedTickerQuotes()){
                is TickerRepository.OperationResult.Success->
                    _dataState.postValue(DataState.Done)
                else ->
                    _dataState.postValue(DataState.Error)
            }
        }
    }

    sealed class TickerInfo {
        object RefreshComplete: TickerInfo()
        data class TickerSearch(val ticker: String) : TickerInfo()
        data class TickerNotFound(val ticker: String): TickerInfo()
    }

    private val _tickerEvent: MutableLiveData<TickerInfo> = MutableLiveData()
    val tickerEvent: LiveData<TickerInfo>
        get() = _tickerEvent

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    fun tickerNotFoundEventComplete() {
        _tickerEvent.value = null
    }

    fun navigateToTickerComplete(){
        _tickerEvent.value = null
    }

    fun dataStateEventHandled() {
        _dataState.value = null
    }

    fun searchTicker(ticker: String) {
        if (availableTickers.value == null || availableTickers.value!!.isEmpty()){
            viewModelScope.launch {
                if (tickerRepository.refreshAvailableTickers() == TickerRepository.OperationResult.Fail){
                    _dataState.value = DataState.Error
                }
            }
        }

        if (availableTickers.value == null || availableTickers.value!!.isEmpty()){
            return
        }

        if (availableTickers.value?.find{ it.symbol == ticker} != null){
            _tickerEvent.value = TickerInfo.TickerSearch(ticker)
        }
        else {
            _tickerEvent.value = TickerInfo.TickerNotFound(ticker)
        }
    }

    fun refreshFollowedTickers() {
        viewModelScope.launch {
            _dataState.postValue(DataState.Loading)
            when(tickerRepository.refreshFollowedTickerQuotes()){
                is TickerRepository.OperationResult.Success->
                    _dataState.postValue(DataState.Done)
                else->
                    _dataState.postValue(DataState.Error)
            }
            _tickerEvent.postValue(TickerInfo.RefreshComplete)
        }
    }

    fun refreshEventFinished(){
        _tickerEvent.value = null
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