package com.example.stock.domain

data class TickerQuote(
    val name: String,
    var value: Double,
    var high: Double,
    var low: Double,
    var prevClose: Double,
    var open: Double){

    fun getValueString() = String.format("$%.2f",value)
}