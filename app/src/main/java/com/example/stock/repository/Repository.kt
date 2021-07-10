package com.example.stock.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.stock.database.DBFollowedTickerQuote
import com.example.stock.database.DatabaseTicker
import com.example.stock.database.TickersDatabase
import com.example.stock.database.asDomainModel
import com.example.stock.domain.Ticker
import com.example.stock.domain.TickerQuote
import com.example.stock.domain.asDbFollowTickerQuote
import com.example.stock.network.NETWORK_TICKER_ADAPTER
import com.example.stock.network.Network
import com.example.stock.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TickerRepository(private val database: TickersDatabase) {

    val availableTickers: LiveData<List<Ticker>> = Transformations.map(database.tickersDao.getTickers()) {
        it.asDomainModel()
    }

    val followedTickerQuotes: LiveData<List<TickerQuote>> = Transformations.map(database.followedTickersDao.getTickers()) {
       it.asDomainModel()
    }

    suspend fun isFollowing(tickerName: String): Boolean{
        return withContext(Dispatchers.IO){
            return@withContext database.followedTickersDao.getTicker(tickerName) != null
        }
    }

    suspend fun toggleFollow(ticker: TickerQuote): Boolean{
        return withContext(Dispatchers.IO){
            val dbTickerQuote = ticker.asDbFollowTickerQuote()

            if (database.followedTickersDao.getTicker(ticker.name) != null) {
                database.followedTickersDao.deleteTickerQuote(dbTickerQuote)
                return@withContext false
            }
            else{
                database.followedTickersDao.insertAll(dbTickerQuote)
                return@withContext true
            }
        }
    }

    suspend fun refreshFollowedTickerQuotes() {
        withContext(Dispatchers.IO){
            //get latest followed tickers
            val followedTickers = database.followedTickersDao.getTickerData()

            followedTickers.forEach { dbTicker ->
                if (database.tickersDao.getTicker(dbTicker.symbol) == null){
                    database.followedTickersDao.deleteTickerQuote(dbTicker)
                }
                else {
                    val currQuote = Network.tickers.getQuote(dbTicker.symbol).body()
                    if (currQuote?.asDatabaseModel(dbTicker.symbol)?.equals(dbTicker) == false){
                        currQuote.asDatabaseModel(dbTicker.symbol).let {
                            database.followedTickersDao.updateTickerQuote(
                                it
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun refreshAvailableTickers() {
        withContext(Dispatchers.IO){
            try {
                val tickerResponse = Network.tickers.getTickers()
                if (tickerResponse.isSuccessful){
                    val tickers = NETWORK_TICKER_ADAPTER.fromJson(tickerResponse.body()?.string())
                    tickers?.asDatabaseModel()?.let { database.tickersDao.insertAll(*it) }
                }
            }
            catch(e: Exception){

            }
        }
    }
}