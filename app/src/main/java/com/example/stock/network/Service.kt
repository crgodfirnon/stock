package com.example.stock.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.reflect.Type

private object ApiKeys {
    const val TICKER_API_KEY = "c3iet4qad3ib8lb84mcg"
}

private val type: Type = Types.newParameterizedType(List::class.java, NetworkCompanyArticle::class.java)
private val tickerContainerType: Type = Types.newParameterizedType(List::class.java, NetworkTicker::class.java)

private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

public val NETWORK_COMPANY_ARTICLE_ADAPTER: JsonAdapter<List<NetworkCompanyArticle>> = moshi.adapter(type)
public val NETWORK_TICKER_ADAPTER: JsonAdapter<List<NetworkTicker>> = moshi.adapter(tickerContainerType)

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

    @GET("stock/symbol")
    suspend fun getTickers(
        @Query("exchange") exchange: String = "us"
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

private val tickerOkHttpClient = OkHttpClient()
    .newBuilder()
    .addInterceptor(ApiKeyInterceptor(ApiKeys.TICKER_API_KEY, "token"))
    .build()

object Network {

    private val tickerRetroFit = Retrofit.Builder()
        .client(tickerOkHttpClient)
        .baseUrl("https://finnhub.io/api/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val tickers: TickerService = tickerRetroFit.create(TickerService::class.java)
}