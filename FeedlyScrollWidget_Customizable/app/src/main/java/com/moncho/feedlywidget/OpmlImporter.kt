package com.moncho.feedlywidget

import android.content.Context
import android.net.Uri
import com.moncho.feedlywidget.data.AppDatabase
import com.moncho.feedlywidget.data.FeedEntity
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

object OpmlImporter {
    suspend fun importFromUri(context: Context, uri: Uri): Int {
        val resolver = context.contentResolver
        val input = resolver.openInputStream(uri) ?: return 0
        input.use { stream ->
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
            doc.documentElement.normalize()
            val outlines = doc.getElementsByTagName("outline")
            val feeds = mutableListOf<FeedEntity>()
            for (i in 0 until outlines.length) {
                val node = outlines.item(i)
                if (node is Element) {
                    val xmlUrl = node.getAttribute("xmlUrl")
                    val title = node.getAttribute("title").ifBlank { node.getAttribute("text") }
                    if (xmlUrl.isNotBlank()) {
                        feeds.add(FeedEntity(url = xmlUrl, title = title.ifBlank { null }))
                    }
                }
            }
            val db = AppDatabase.get(context)
            db.feedDao().clearFeeds()
            db.feedDao().insertFeeds(feeds)
            return feeds.size
        }
    }
}
