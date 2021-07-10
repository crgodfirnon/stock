package com.example.stock.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FollowedTickersDao {
    @Query("select * from dbfollowedtickerquote")
    fun getTickers() : LiveData<List<DBFollowedTickerQuote>>

    @Query("select * from dbfollowedtickerquote")
    fun getTickerData() : List<DBFollowedTickerQuote>

    @Query("select * from dbfollowedtickerquote where symbol = :sym")
    fun getTicker(sym: String) : DBFollowedTickerQuote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg tickers: DBFollowedTickerQuote)

    @Update
    fun updateTickerQuote(ticker: DBFollowedTickerQuote)

    @Delete
    fun deleteTickerQuote(ticker: DBFollowedTickerQuote?)
}

@Dao
interface TickersDao {
    @Query("select * from databaseticker")
    fun getTickers() : LiveData<List<DatabaseTicker>>

    @Query("select * from databaseticker WHERE symbol = :sym")
    fun getTicker(sym: String) : DatabaseTicker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg tickers: DatabaseTicker)

    @Delete
    fun deleteTicker(ticker: DatabaseTicker)
}

@Database(entities = [DBFollowedTickerQuote::class, DatabaseTicker::class], version = 1)
abstract class TickersDatabase: RoomDatabase() {
    abstract val followedTickersDao : FollowedTickersDao
    abstract val tickersDao: TickersDao
}

private lateinit var INSTANCE: TickersDatabase

fun getDatabase(context: Context) : TickersDatabase {
    synchronized(TickersDatabase::class.java){
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                TickersDatabase::class.java,
                "tickers").build()
        }
    }
    return INSTANCE
}
