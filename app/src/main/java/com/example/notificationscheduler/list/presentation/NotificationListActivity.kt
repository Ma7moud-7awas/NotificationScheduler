package com.example.notificationscheduler.list.presentation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationscheduler.R
import com.example.notificationscheduler.core.presentation.BaseActivity
import com.example.notificationscheduler.core.presentation.model.UiState
import com.example.notificationscheduler.details.presentaion.NotificationDetailsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationListActivity : BaseActivity() {

    private val viewModel: NotificationListViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter
    private lateinit var pbLoading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_list)
        setWindowInsets(R.id.main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        findGlobalViews()
        setupRecyclerView()
        observeUiState()
    }

    override fun onStart() {
        super.onStart()
        viewModel.refreshNotifications()
    }

    fun findGlobalViews() {
        pbLoading = findViewById(R.id.loading)
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(listOf()) { notification ->
            val intent = Intent(this, NotificationDetailsActivity::class.java)
                .putExtra("notification", notification)

            startActivity(intent)
        }

        val rvNotifications: RecyclerView = findViewById(R.id.rv_notifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            toggleLoading(true)
                        }

                        is UiState.Success -> {
                            toggleLoading(false)
                            adapter.updateData(state.data)
                        }

                        is UiState.Error -> {
                            toggleLoading(false)
                            displayError(state)
                        }
                    }
                }
            }
        }
    }

    fun toggleLoading(display: Boolean) {
        pbLoading.visibility = if (display) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cancel_all -> {
                viewModel.cancelAllNotifications()
                displayMessage(getString(R.string.all_notifications_cancelled))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
