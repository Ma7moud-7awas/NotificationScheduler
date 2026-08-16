package com.example.notificationscheduler.details.presentaion

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.notificationscheduler.core.data.model.Notification
import com.example.notificationscheduler.details.alarm.NotificationAlarmScheduler
import com.example.notificationscheduler.list.domain.NotificationRepository
import com.example.notificationscheduler.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository: NotificationRepository = mockk()
    private val alarmScheduler: NotificationAlarmScheduler = mockk()
    private lateinit var viewModel: NotificationDetailsViewModel

    @Before
    fun setup() {
        viewModel = NotificationDetailsViewModel(repository, alarmScheduler)
    }

    @Test
    fun `scheduleNotification updates database and schedules alarm`() = runTest {
        // Given
        val notification = Notification(id = 1, title = "Test Notification", timeInSeconds = 60)
        coEvery { repository.updateScheduledState(notification.id, true) } returns Unit
        every { alarmScheduler.schedule(any()) } returns Unit

        // When
        viewModel.scheduleNotification(notification)

        // Then
        coVerify { repository.updateScheduledState(notification.id, true) }
        verify { alarmScheduler.schedule(match { it.id == notification.id && it.isScheduled }) }
    }

    @Test
    fun `cancelNotification updates database and cancels alarm`() = runTest {
        // Given
        val notificationId = 1
        coEvery { repository.updateScheduledState(notificationId, false) } returns Unit
        every { alarmScheduler.cancel(notificationId) } returns true

        // When
        viewModel.cancelNotification(notificationId)

        // Then
        coVerify { repository.updateScheduledState(notificationId, false) }
        verify { alarmScheduler.cancel(notificationId) }
    }

    @Test
    fun `getNotificationById returns the flow from repository`() {
        // Given
        val notificationId = 1
        val notification = Notification(id = notificationId, title = "Test")
        val flow = flowOf(notification)
        every { repository.getNotificationById(notificationId) } returns flow

        // When
        val result = viewModel.getNotificationById(notificationId)

        // Then
        assertEquals(flow, result)
        verify { repository.getNotificationById(notificationId) }
    }
}
