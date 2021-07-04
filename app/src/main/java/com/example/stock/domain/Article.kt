package com.example.stock.domain

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

val inputFormat : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").apply {
    timeZone = Calendar.getInstance().timeZone
}

data class Article(
                val source: String,
                val time: String,
                val title: String,
                val description: String,
                val url: String,
                val imageUrl: String?)
{
    fun getArticleStamp() : String {
        val date: Date = inputFormat.parse(time)
        val niceDateStr: String = DateUtils.getRelativeTimeSpanString(
            date.time,
            Calendar.getInstance().timeInMillis,
            DateUtils.MINUTE_IN_MILLIS
        ).toString()

        return "$source - $niceDateStr"

    }
}