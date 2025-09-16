package com.moncho.feedlywidget

import android.app.Application
import androidx.work.*
import java.util.concurrent.TimeUnit

class FeedlyWidgetApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Schedule periodic sync every 2 hours (flexible)
        val request = PeriodicWorkRequestBuilder<SyncWorker>(2, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "rss-sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
