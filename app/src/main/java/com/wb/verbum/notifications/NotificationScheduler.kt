package com.wb.verbum.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    fun scheduleDailyNotification(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniqueWork(
            "daily_notification",
            ExistingWorkPolicy.REPLACE,
            notificationWork
        )
    }
}