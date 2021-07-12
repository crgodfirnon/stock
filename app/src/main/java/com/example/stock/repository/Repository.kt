package com.example.stock.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.stock.database.DBFollowedTickerQuote
import com.example.stock.database.TickersDatabase
import com.example.stock.database.asDomainModel
import com.example.stock.domain.Article
import com.example.stock.domain.Ticker
import com.example.stock.domain.TickerQuote
import com.example.stock.domain.asDbFollowTickerQuote
import com.example.stock.network.*
import com.github.mikephil.charting.data.CandleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class TickerRepository(private val database: TickersDatabase) {

    sealed class OperationResult {
        object Success: OperationResult()
        object Fail: OperationResult()

        data class ToggleFollowResult(val isFollowing: Boolean) : OperationResult()
        data class GetQuoteResult(val quote: TickerQuote) : OperationResult()
        data class GetCandleStickData(val data: CandleData) : OperationResult()
        data class GetTickerNewsResult(val articles: List<Article>) : OperationResult()
    }

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

    suspend fun getCandleStickData(ticker: String) : OperationResult {
        return withContext(Dispatchers.IO){
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -30)
            val fromString = (cal.timeInMillis/1000).toString()

            try{
                val candleData = Network.tickers.getCandleStickData(
                    ticker, fromString, (System.currentTimeMillis()/1000).toString()
                )
                if (candleData.isSuccessful){
                    val domainData = candleData.body()?.asDomainModel()
                    if (domainData == null)
                        return@withContext OperationResult.Fail
                    else
                        return@withContext OperationResult.GetCandleStickData(CandleData(domainData))
                }
                else{
                    return@withContext OperationResult.Fail
                }
            }
            catch(e: Exception){
                return@withContext OperationResult.Fail
            }
        }
    }

    suspend fun getTickerNews(ticker: String): OperationResult {
        return withContext(Dispatchers.IO){
            try{
                val articleResponse = Network.tickers.getCompanyNews(ticker,"2021-07-01", "2021-07-07")
                if (articleResponse.isSuccessful){
                    val companyArticles = NETWORK_COMPANY_ARTICLE_ADAPTER.fromJson(articleResponse.body()?.string())
                    if (companyArticles != null) {
                        return@withContext OperationResult.GetTickerNewsResult(companyArticles.asDomainModel())
                    }
                    else{
                        return@withContext OperationResult.GetTickerNewsResult(emptyList())
                    }
                }
                else{
                    return@withContext OperationResult.Fail
                }
            }
            catch(e: Exception){
                return@withContext OperationResult.Fail
            }
        }
    }

    suspend fun toggleFollow(ticker: String): OperationResult.ToggleFollowResult{
        return withContext(Dispatchers.IO){
            val dbTickerQuote = database.followedTickersDao.getTicker(ticker)
            // we are currently following this ticker, so just unfollow and return
            dbTickerQuote?.let {
                database.followedTickersDao.deleteTickerQuote(dbTickerQuote)
                return@withContext OperationResult.ToggleFollowResult(false)
            }

            try {
                // try to get the current quote
                val currQuote = Network.tickers.getQuote(ticker)
                if (currQuote.isSuccessful){
                    currQuote.body()?.asDatabaseModel(ticker)?.let {
                        database.followedTickersDao.insertAll(
                            it
                        )
                    }
                }
                else {
                    database.followedTickersDao.insertAll(DBFollowedTickerQuote(ticker, 0.0, 0.0, 0.0, 0.0, 0.0))
                }
                return@withContext OperationResult.ToggleFollowResult(true)
            }
            catch(e: Exception){
                database.followedTickersDao.insertAll(DBFollowedTickerQuote(ticker, 0.0, 0.0, 0.0, 0.0, 0.0))
                return@withContext OperationResult.ToggleFollowResult(true)
            }
        }
    }

    suspend fun getTickerQuote(ticker: String) : OperationResult {
        return withContext(Dispatchers.IO){
            try{
                val quote = Network.tickers.getQuote(ticker)
                if(quote.isSuccessful) {
                    val domainQuote = quote.body()?.asDomainModel(ticker)
                    return@withContext OperationResult.GetQuoteResult(domainQuote!!)
                }
                else{
                    return@withContext OperationResult.Fail
                }
            }
            catch(e: Exception){
                return@withContext OperationResult.Fail
            }
        }
    }

    suspend fun refreshFollowedTickerQuotes() : OperationResult {
        return withContext(Dispatchers.IO){
            //get latest followed tickers
            val followedTickers = database.followedTickersDao.getTickerData()
            val freshTickers = mutableListOf<DBFollowedTickerQuote>()

            followedTickers.forEach { dbTicker ->
                if (database.tickersDao.getTicker(dbTicker.symbol) == null){
                    database.followedTickersDao.deleteTickerQuote(dbTicker)
                }
                else {
                    try{
                        val currQuote = Network.tickers.getQuote(dbTicker.symbol)
                        if (currQuote.isSuccessful){
                            val domainCurrQuote = currQuote.body()?.asDomainModel(dbTicker.symbol)
                            if (domainCurrQuote?.equals(dbTicker) == false){
                                freshTickers.add(domainCurrQuote.asDbFollowTickerQuote())
                            }
                        }
                        else{
                            return@withContext OperationResult.Fail
                        }
                    }
                    catch(e: Exception){
                        return@withContext OperationResult.Fail
                    }
                }
            }
            database.followedTickersDao.insertAll(*freshTickers.toTypedArray())
            return@withContext OperationResult.Success
        }
    }

    suspend fun refreshAvailableTickers() : OperationResult {
        return withContext(Dispatchers.IO){
            try {
                val tickerResponse = Network.tickers.getTickers()
                if (tickerResponse.isSuccessful){
                    val tickers = NETWORK_TICKER_ADAPTER.fromJson(tickerResponse.body()?.string())
                    tickers?.asDatabaseModel()?.let { database.tickersDao.insertAll(*it) }
                    return@withContext OperationResult.Success
                }
                else {
                    return@withContext OperationResult.Fail
                }
            }
            catch(e: Exception){
                return@withContext OperationResult.Fail
            }
        }
    }
}