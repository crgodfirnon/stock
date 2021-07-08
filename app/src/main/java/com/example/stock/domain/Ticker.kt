package com.example.stock.domain

data class TickerQuote(val name: String, var value: Double){
    fun getValueString() = "$$value"
}