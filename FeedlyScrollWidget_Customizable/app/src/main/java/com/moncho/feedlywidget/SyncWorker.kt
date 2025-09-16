package com.moncho.feedlywidget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val repo = RssRepository(applicationContext)
        try {
            repo.refreshAll()
            // Notify widgets to refresh
            RssWidgetProvider.notifyAllWidgets(applicationContext)
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
