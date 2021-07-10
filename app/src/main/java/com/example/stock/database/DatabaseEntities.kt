package com.example.stock.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stock.domain.Ticker
import com.example.stock.domain.TickerQuote

@Entity
data class DBFollowedTickerQuote constructor(
    @PrimaryKey
    val symbol: String,
    var value: Double,
    var high: Double,
    var low: Double,
    var prevClose: Double,
    var open: Double
)

@Entity
data class DatabaseTicker constructor(
    @PrimaryKey
    val symbol: String
)

@JvmName("asDomainModelDatabaseTicker")
fun List<DatabaseTicker>.asDomainModel(): List<Ticker> {
    return map {
        Ticker(
            symbol = it.symbol
        )
    }
}

fun List<DBFollowedTickerQuote>.asDomainModel(): List<TickerQuote> {
    return map {
        TickerQuote(
            name = it.symbol,
            value = it.value,
            high = it.high,
            low = it.low,
            prevClose = it.prevClose,
            open = it.open
        )
    }
}