package com.example.stock.network

import android.graphics.Color
import android.graphics.Paint
import com.example.stock.database.DBFollowedTickerQuote
import com.example.stock.database.DatabaseTicker
import com.example.stock.domain.Article
import com.example.stock.domain.TickerQuote
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkTickerQuote (
    val c: Double,
    val h: Double,
    val l: Double,
    val o: Double,
    val pc: Double,
    val t: Int
        )

@JsonClass(generateAdapter = true)
data class NetworkCandleData(
    val c: List<Float>,
    val h: List<Float>,
    val l: List<Float>,
    val o: List<Float>,
    val s: String,
    val t: List<Float>,
    val v: List<Float>
)

@JsonClass(generateAdapter = true)
data class NetworkCompanyArticle(
    val category: String,
    val datetime: Long,
    val headline: String,
    val id: Double,
    val image: String?,
    val related: String,
    val source: String,
    val summary: String,
    val url: String
)

@JsonClass(generateAdapter = true)
data class NetworkTicker(
    val currency: String,
    val description: String,
    val displaySymbol: String,
    val figi: String,
    val mic: String,
    val symbol: String,
    val type: String
)

fun List<NetworkCompanyArticle>.asDomainModel() : List<Article> {
    return map{
        Article(
            source = it.source,
            description = it.summary,
            title = it.headline,
            url = it.url,
            imageUrl = it.image,
            time = it.datetime.toString()
        )
    }
}

fun List<NetworkTicker>.asDatabaseModel(): Array<DatabaseTicker> {
    return map {
        DatabaseTicker(
            symbol = it.symbol
        )
    }.toTypedArray()
}

fun NetworkTickerQuote.asDomainModel(symbol: String) : TickerQuote {
    return TickerQuote(symbol, c, h, l, pc, o)
}

fun NetworkTickerQuote.asDatabaseModel(symbol: String): DBFollowedTickerQuote {
    return  DBFollowedTickerQuote(
        symbol = symbol,
        value = c,
        high = h,
        low = l,
        prevClose = pc,
        open = o
    )
}

fun NetworkCandleData.asDomainModel() : CandleDataSet {
    val values: ArrayList<CandleEntry> = ArrayList()
    for (i in c.indices) {
        values.add(CandleEntry((i+1).toFloat(), h[i], l[i], o[i], c[i]))
    }
    return CandleDataSet(values, "Data Set").apply {
        setDrawIcons(false)
        axisDependency = YAxis.AxisDependency.LEFT
        shadowColor = Color.DKGRAY
        shadowWidth = .7f
        decreasingColor = Color.RED
        decreasingPaintStyle = Paint.Style.FILL
        increasingColor = Color.rgb(122, 242, 84)
        increasingPaintStyle = Paint.Style.STROKE
        neutralColor = Color.BLUE
    }
}