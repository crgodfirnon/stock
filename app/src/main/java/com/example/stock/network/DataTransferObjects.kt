package com.example.stock.network

import com.example.stock.domain.Article
import com.example.stock.domain.Ticker
import com.squareup.moshi.Json
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
data class NetworkQuote (
    val c: Double,
    val h: Double,
    val l: Double,
    val o: Double,
    val pc: Double,
    val t: Int
        )

fun NetworkQuote.asDomainModel(symbol: String) : Ticker {
    return Ticker(symbol, c)
}