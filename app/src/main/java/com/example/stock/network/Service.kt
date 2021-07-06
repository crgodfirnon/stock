package com.example.stock.network

import com.example.stock.domain.Article
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private object ApiKeys {
    const val ARTICLE_API_KEY = "3961d4dd8d4b49fc85a6cfc088c1e9bb"
}

private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()


interface ArticleService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") countryCode: String = "us",
        @Query("pageSize") pageSize: Int = 20)
            : retrofit2.Response<ArticleResponseContainer>

    @GET("everything")
    suspend fun getArticles(
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("pageSize") pageSize: Int = 20,
        @Query("page") page: Int = 1,
        @Query("sortBy") sortBy: String = "publishedAt"
    )
        : retrofit2.Response<ArticleResponseContainer>
}

private class ApiKeyInterceptor(val api_key: String) : Interceptor{
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var original = chain.request()
        val url = original.url.newBuilder().addQueryParameter("apiKey", api_key).build()
        original = original.newBuilder().url(url).build()
        return chain.proceed(original)
    }

}

private val articleOkHttpClient = OkHttpClient()
    .newBuilder()
    .addInterceptor(ApiKeyInterceptor(ApiKeys.ARTICLE_API_KEY))
    .build()

object Network {
    private val articleRetrofit = Retrofit.Builder()
        .client(articleOkHttpClient)
        .baseUrl("https://newsapi.org/v2/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val articles: ArticleService = articleRetrofit.create(ArticleService::class.java)
}