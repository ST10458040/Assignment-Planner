package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmentplanner.data.AppDatabase
import com.example.assignmentplanner.data.NotificationEntity
import com.example.assignmentplanner.data.PlannerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsActivity : AppCompatActivity() {

    private lateinit var repository: PlannerRepository
    private lateinit var rvNotifications: RecyclerView
    private lateinit var adapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)

        AppPreferences.setHasUnreadNotifications(this, false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        repository = PlannerRepository(AppDatabase.getDatabase(this).plannerDao())
        rvNotifications = findViewById(R.id.rv_notifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)
        adapter = NotificationsAdapter()
        rvNotifications.adapter = adapter

        findViewById<TextView>(R.id.tv_mark_read).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                repository.markAllNotificationsAsRead()
                loadNotifications()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppPreferences.setHasUnreadNotifications(this, false)
        loadNotifications()
    }

    private fun loadNotifications() {
        val tvEmpty = findViewById<TextView>(R.id.tv_empty_notifications)
        lifecycleScope.launch(Dispatchers.IO) {
            val notifications = repository.getAllNotifications()
            repository.markAllNotificationsAsRead()
            withContext(Dispatchers.Main) {
                AppPreferences.setHasUnreadNotifications(this@NotificationsActivity, false)
                if (notifications.isEmpty()) {
                    tvEmpty?.visibility = View.VISIBLE
                    rvNotifications.visibility = View.GONE
                } else {
                    tvEmpty?.visibility = View.GONE
                    rvNotifications.visibility = View.VISIBLE
                    adapter.submitList(notifications)
                }
            }
        }
    }

    inner class NotificationsAdapter : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {
        private var items = listOf<NotificationEntity>()

        fun submitList(list: List<NotificationEntity>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
            return NotificationViewHolder(view)
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            val notif = items[position]
            holder.tvTitle.text = notif.title
            holder.tvMessage.text = notif.message
            val timeString = DateUtils.getRelativeTimeSpanString(notif.timestampEpoch, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
            holder.tvTime.text = timeString

            if (!notif.isRead) {
                holder.cardView.strokeWidth = 2
                holder.cardView.strokeColor = android.graphics.Color.parseColor("#EF4444")
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#EF4444"))
            } else {
                holder.cardView.strokeWidth = 0
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#06B6D4")) // accent_cyan
            }

            holder.itemView.setOnClickListener {
                if (notif.relatedAssignmentId > 0) {
                    val intent = Intent(this@NotificationsActivity, AssignmentDetailsActivity::class.java).apply {
                        putExtra("ASSIGNMENT_ID", notif.relatedAssignmentId)
                    }
                    startActivity(intent)
                }
            }
        }

        override fun getItemCount() = items.size

        inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
            val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
            val tvTime: TextView = itemView.findViewById(R.id.tv_time)
            val cardView: com.google.android.material.card.MaterialCardView = itemView.findViewById(R.id.card_notification)
            val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        }
    }
}
