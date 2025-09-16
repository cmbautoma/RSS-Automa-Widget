package com.moncho.feedlywidget

import android.content.Context
import com.moncho.feedlywidget.data.AppDatabase
import com.moncho.feedlywidget.data.ArticleEntity
import com.moncho.feedlywidget.data.FeedEntity
import com.prof18.rssparser.RssParser
import com.prof18.rssparser.RssParserBuilder
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.*

class RssRepository(private val context: Context) {

    private val db = AppDatabase.get(context)
    private val parser: RssParser by lazy {
        val ok = OkHttpClient.Builder().build()
        RssParserBuilder(callFactory = ok).build()
    }

    suspend fun getFeeds(): List<FeedEntity> = db.feedDao().getAllFeeds()

    suspend fun refreshAll() {
        val feeds = getFeeds()
        if (feeds.isEmpty()) return
        val articles = mutableListOf<ArticleEntity>()
        for (feed in feeds) {
            try {
                val channel = parser.getRssChannel(feed.url)
                val feedTitle = channel.title
                for (item in channel.items) {
                    val link = item.link ?: continue
                    val title = item.title ?: continue
                    val dateMillis = parseDate(item.pubDate) ?: System.currentTimeMillis()
                    articles.add(
                        ArticleEntity(
                            link = link,
                            title = title,
                            feedTitle = feedTitle,
                            pubDateMillis = dateMillis
                        )
                    )
                }
            } catch (_: Exception) {
                // ignore single feed errors
            }
        }
        if (articles.isNotEmpty()) {
            db.articleDao().upsertAll(articles)
        }
    }

    suspend fun latest(limit: Int = 50): List<ArticleEntity> = db.articleDao().latest(limit)

    private fun parseDate(dateStr: String?): Long? {
        if (dateStr == null) return null
        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm Z",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ"
        )
        for (f in formats) {
            try {
                val sdf = SimpleDateFormat(f, Locale.US)
                return sdf.parse(dateStr)?.time
            } catch (_: Exception) {}
        }
        return null
    }
}
