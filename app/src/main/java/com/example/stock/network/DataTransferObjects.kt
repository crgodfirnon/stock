package com.example.stock.network

import android.graphics.Color
import android.graphics.Paint
import com.example.stock.domain.Article
import com.example.stock.domain.TickerQuote
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArticleResponseContainer(
    val status: String,
    val totalResults: Int,
    val articles: List<NetworkArticle>
)

@JsonClass(generateAdapter = true)
data class NetworkArticle(
    val source: ArticleSource,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

@JsonClass(generateAdapter = true)
data class ArticleSource(
    val id: String?,
    val name: String?
)

fun ArticleResponseContainer.asDomainModel() : Array<Article> {
    return articles.map {
        Article(
            source = it.source.name ?: "",
            time = it.publishedAt ?: "",
            description = it.description ?: "",
            title = it.title ?: "",
            url = it.url ?: "",
            imageUrl = it.urlToImage)
    }.toTypedArray()
}

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
data class CompanyArticleResponseContainer(
    val articles: List<CompanyArticle>
)

@JsonClass(generateAdapter = true)
data class CompanyArticle(
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

fun CompanyArticleResponseContainer.asDomainModel() : Array<Article> {
    return articles.map{
        Article(
            source = it.source,
            description = it.summary,
            title = it.headline,
            url = it.url,
            imageUrl = it.image,
            time = it.datetime.toString()
        )
    }.toTypedArray()
}

fun NetworkTickerQuote.asDomainModel(symbol: String) : TickerQuote {
    return TickerQuote(symbol, c, h, l, pc, o)
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