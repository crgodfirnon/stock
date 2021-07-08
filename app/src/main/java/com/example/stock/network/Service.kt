package com.example.stock.network

import com.example.stock.domain.Article
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.reflect.Type

private object ApiKeys {
    const val ARTICLE_API_KEY = "3961d4dd8d4b49fc85a6cfc088c1e9bb"
    const val TICKER_API_KEY = "c3iet4qad3ib8lb84mcg"
}

private val type: Type = Types.newParameterizedType(List::class.java, CompanyArticle::class.java)

private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

public val companyArticleAdapter: JsonAdapter<List<CompanyArticle>> = moshi.adapter(type)

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

interface TickerService {
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String)
        : retrofit2.Response<NetworkTickerQuote>

    @GET("stock/candle")
    suspend fun getCandleStickData(
        @Query("symbol") symbol: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("resolution") resolution: String = "D"
    )
     : retrofit2.Response<NetworkCandleData>

    @GET("company-news")
    suspend fun getCompanyNews(
        @Query("symbol") symbol: String,
        @Query("from") from: String,
        @Query("to") to: String
    )
    : retrofit2.Response<ResponseBody>
}

private class ApiKeyInterceptor(val api_key: String, val queryName: String) : Interceptor{
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var original = chain.request()
        val url = original.url.newBuilder().addQueryParameter(queryName, api_key).build()
        original = original.newBuilder().url(url).build()
        return chain.proceed(original)
    }

}

private val articleOkHttpClient = OkHttpClient()
    .newBuilder()
    .addInterceptor(ApiKeyInterceptor(ApiKeys.ARTICLE_API_KEY, "apiKey"))
    .build()

private val tickerOkHttpClient = OkHttpClient()
    .newBuilder()
    .addInterceptor(ApiKeyInterceptor(ApiKeys.TICKER_API_KEY, "token"))
    .build()

object Network {
    private val articleRetrofit = Retrofit.Builder()
        .client(articleOkHttpClient)
        .baseUrl("https://newsapi.org/v2/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val tickerRetroFit = Retrofit.Builder()
        .client(tickerOkHttpClient)
        .baseUrl("https://finnhub.io/api/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val articles: ArticleService = articleRetrofit.create(ArticleService::class.java)
    val tickers: TickerService = tickerRetroFit.create(TickerService::class.java)
}