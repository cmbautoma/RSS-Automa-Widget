package com.moncho.feedlywidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.moncho.feedlywidget.data.ArticleEntity
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class RssWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = Factory(this, intent)
}

class Factory(private val context: RssWidgetService, intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    private val items = mutableListOf<ArticleEntity>()
    private val appWidgetId: Int =
        intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

    override fun onCreate() {}

    override fun onDataSetChanged() {
        runBlocking {
            val repo = RssRepository(context)
            items.clear()
            val limit = Prefs.itemLimit(context, appWidgetId).coerceIn(1, 100)
            items.addAll(repo.latest(limit))
        }
    }

    override fun onDestroy() { items.clear() }
    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val article = items[position]
        val compact = Prefs.compact(context, appWidgetId)

        val layoutId = if (compact) R.layout.widget_rss_item_compact else R.layout.widget_rss_item
        val rv = RemoteViews(context.packageName, layoutId)

        val textColor = Prefs.textColor(context, appWidgetId)
        val text2Color = Prefs.secondaryTextColor(context, appWidgetId)
        val titleSize = Prefs.titleSizeSp(context, appWidgetId)
        val metaSize = Prefs.metaSizeSp(context, appWidgetId)

        rv.setTextViewText(R.id.item_title, article.title)
        rv.setTextColor(R.id.item_title, textColor)
        rv.setTextViewTextSize(R.id.item_title, android.util.TypedValue.COMPLEX_UNIT_SP, titleSize)

        if (!compact) {
            val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
            val meta = ((article.feedTitle ?: "")) + " • " + sdf.format(Date(article.pubDateMillis))
            rv.setTextViewText(R.id.item_meta, meta)
            rv.setTextColor(R.id.item_meta, text2Color)
            rv.setTextViewTextSize(R.id.item_meta, android.util.TypedValue.COMPLEX_UNIT_SP, metaSize)
        }

        val fillInIntent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(article.link)
        }
        rv.setOnClickFillInIntent(R.id.item_title, fillInIntent)
        if (!compact) rv.setOnClickFillInIntent(R.id.item_meta, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 2
    override fun getItemId(position: Int): Long = items[position].link.hashCode().toLong()
    override fun hasStableIds(): Boolean = true
}
