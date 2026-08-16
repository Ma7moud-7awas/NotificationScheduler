package com.example.notificationscheduler.details.presentaion

import androidx.lifecycle.ViewModel
import androidx.work.*
import com.example.notificationscheduler.core.data.model.Notification
import com.example.notificationscheduler.details.worker.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class NotificationDetailsViewModel @Inject constructor(
    private val workManager: WorkManager
) : ViewModel() {

    companion object {
        const val NOTIFICATION_WORK_TAG = "notification_work_tag"
    }

    fun getWorkStatus(notificationId: Int): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkFlow("notification_$notificationId")
    }

    fun scheduleNotification(notification: Notification) {
        val data = Data.Builder()
            .putString("title", notification.title)
            .putInt("id", notification.id)
            .build()
        
        val tag = "notification_${notification.id}"
        
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(notification.timeInSeconds, TimeUnit.SECONDS)
            .setInputData(data)
            .addTag(tag)
            .addTag(NOTIFICATION_WORK_TAG)
            .build()

        workManager.enqueueUniqueWork(
            tag,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelNotification(notificationId: Int) {
        workManager.cancelUniqueWork("notification_$notificationId")
    }
}
