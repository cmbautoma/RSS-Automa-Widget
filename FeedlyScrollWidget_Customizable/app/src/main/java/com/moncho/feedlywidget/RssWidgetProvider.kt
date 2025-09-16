package com.moncho.feedlywidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.moncho.feedlywidget.R

class RssWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_rss)

            // Set list adapter
            val svcIntent = Intent(context, RssWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.list, svcIntent)
            views.setEmptyView(R.id.list, R.id.empty)

            // Click template to open links
            val clickIntent = Intent(Intent.ACTION_VIEW)
            val clickPI = PendingIntent.getActivity(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setPendingIntentTemplate(R.id.list, clickPI)

            // Refresh button
            val refreshIntent = Intent(context, SyncNowReceiver::class.java)
            val refreshPI = PendingIntent.getBroadcast(
                context, appWidgetId, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_refresh, refreshPI)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list)
        }
    }

    companion object {
        fun notifyAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, RssWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(thisWidget)
            if (ids != null && ids.isNotEmpty()) {
                appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.list)
            }
        }
    }
}
