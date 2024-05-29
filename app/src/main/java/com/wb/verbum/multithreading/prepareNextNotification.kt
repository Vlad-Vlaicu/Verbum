package com.wb.verbum.multithreading

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.wb.verbum.model.User
import com.wb.verbum.notifications.NotificationWorker
import com.wb.verbum.notifications.UsagePredictor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@OptIn(DelicateCoroutinesApi::class)
suspend fun prepareNextNotification(context: Context, user: User) {
    withContext(Dispatchers.IO) {
        GlobalScope.launch {
            val usagePredictor = UsagePredictor()
            val nextUsageTime = usagePredictor.predictNextUsage(user)
            val currentTime = System.currentTimeMillis() / 1000

            val triggerTime = if (nextUsageTime != null && nextUsageTime > currentTime) {
                nextUsageTime * 1000
            } else {
                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
            }

            // Calculate delay in milliseconds
            val delay = triggerTime - System.currentTimeMillis()

            // Schedule the notification with WorkManager
            val workManager = WorkManager.getInstance(context)
            val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniqueWork(
                "daily_notification",
                ExistingWorkPolicy.REPLACE,
                notificationWork
            )
        }
    }
}