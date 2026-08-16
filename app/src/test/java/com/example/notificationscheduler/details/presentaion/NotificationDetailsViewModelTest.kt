package com.example.notificationscheduler.details.presentaion

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.notificationscheduler.core.model.Notification
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NotificationDetailsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val workManager: WorkManager = mockk()
    private lateinit var viewModel: NotificationDetailsViewModel

    @Before
    fun setup() {
        viewModel = NotificationDetailsViewModel(workManager)
    }

    @Test
    fun `scheduleNotification enqueues unique work with correct parameters`() {
        // Given
        val notification = Notification(id = 1, title = "Test Notification", timeInSeconds = 60)
        val tag = "notification_${notification.id}"
        every {
            workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, any<OneTimeWorkRequest>())
        } returns mockk()

        // When
        viewModel.scheduleNotification(notification)

        // Then
        verify {
            workManager.enqueueUniqueWork(
                tag,
                ExistingWorkPolicy.REPLACE,
                match<OneTimeWorkRequest> {
                    it.tags.contains(tag) && it.tags.contains(NotificationDetailsViewModel.NOTIFICATION_WORK_TAG)
                }
            )
        }
    }

    @Test
    fun `cancelNotification calls workManager cancelUniqueWork`() {
        // Given
        val notificationId = 1
        val tag = "notification_$notificationId"
        every { workManager.cancelUniqueWork(tag) } returns mockk()

        // When
        viewModel.cancelNotification(notificationId)

        // Then
        verify { workManager.cancelUniqueWork(tag) }
    }

    @Test
    fun `getWorkStatus returns the flow from workManager`() {
        // Given
        val notificationId = 1
        val tag = "notification_$notificationId"
        val flow = flowOf(emptyList<androidx.work.WorkInfo>())
        every { workManager.getWorkInfosForUniqueWorkFlow(tag) } returns flow

        // When
        val result = viewModel.getWorkStatus(notificationId)

        // Then
        assertEquals(flow, result)
        verify { workManager.getWorkInfosForUniqueWorkFlow(tag) }
    }
}
