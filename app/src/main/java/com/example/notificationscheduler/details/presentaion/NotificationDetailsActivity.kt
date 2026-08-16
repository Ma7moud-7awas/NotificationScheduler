package com.example.notificationscheduler.details.presentaion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.notificationscheduler.R
import com.example.notificationscheduler.core.data.model.Notification
import com.example.notificationscheduler.core.presentation.BaseActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationDetailsActivity : BaseActivity() {

    private val viewModel: NotificationDetailsViewModel by viewModels()
    private var notification: Notification? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            notification?.let {
                showScheduleConfirmationDialog(it)
            }
        } else {
            displayMessage(getString(R.string.notification_permission_denied))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_details)
        setWindowInsets(R.id.main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getPassedData()

        notification?.let { n ->
            val tvTitle: TextView = findViewById(R.id.tv_detail_title)
            tvTitle.text = n.title

            val btnSchedule: Button = findViewById(R.id.btn_schedule)
            btnSchedule.setOnClickListener {
                checkAndRequestPermissionAndSchedule(n)
            }

            observeNotificationState(n, btnSchedule)
        }
    }

    private fun getPassedData() {
        notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("notification", Notification::class.java)
        } else {
            intent.getSerializableExtra("notification") as? Notification
        }
    }

    private fun observeNotificationState(
        notification: Notification,
        btnSchedule: Button
    ): Job = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.getNotificationById(notification.id).collectLatest { n ->
                val isScheduled = n?.isScheduled ?: false

                btnSchedule.isEnabled = !isScheduled
            }
        }
    }

    private fun checkAndRequestPermissionAndSchedule(n: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!viewModel.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog()
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showScheduleConfirmationDialog(n)
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            showScheduleConfirmationDialog(n)
        }
    }

    private fun showExactAlarmPermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.exact_alarm_permission_needed))
            .setMessage(getString(R.string.schedule_exact_time_body))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showScheduleConfirmationDialog(n: Notification) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.confirm_scheduling_title))
            .setMessage(getString(R.string.confirm_scheduling_message, n.timeInSeconds))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                viewModel.scheduleNotification(n)
                displayMessage(getString(R.string.notification_scheduled))
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.action_cancel -> {
                notification?.let {
                    viewModel.cancelNotification(it.id)
                    displayMessage(getString(R.string.notification_cancelled))
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
