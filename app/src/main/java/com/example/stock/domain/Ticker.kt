package com.example.stock.domain

import com.example.stock.database.DBFollowedTickerQuote

data class Ticker(
    val symbol: String
)

data class TickerQuote(
    val name: String,
    var value: Double,
    var high: Double,
    var low: Double,
    var prevClose: Double,
    var open: Double){

    fun getValueString() = String.format("$%.2f",value)
}

fun TickerQuote.asDbFollowTickerQuote() :  DBFollowedTickerQuote {
    return DBFollowedTickerQuote(
        symbol = name,
        high = high,
        low = low,
        open = open,
        prevClose = prevClose,
        value = value
    )
}