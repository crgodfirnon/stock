package com.example.stock.network

import com.example.stock.domain.Article
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