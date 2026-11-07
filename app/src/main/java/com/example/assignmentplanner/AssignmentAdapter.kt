package com.example.assignmentplanner

import android.content.Intent
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmentplanner.data.AssignmentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssignmentAdapter : ListAdapter<AssignmentEntity, AssignmentAdapter.AssignmentViewHolder>(AssignmentDiffCallback()) {

    private var lastClickTime = 0L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_assignment, parent, false)
        return AssignmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvModulePriority: TextView = itemView.findViewById(R.id.tv_module_priority)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)

        fun bind(assignment: AssignmentEntity) {
            tvTitle.text = assignment.title
            
            // Calculate days left
            var daysLeftText = ""
            try {
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dueDate = format.parse(assignment.dueDate)
                if (dueDate != null) {
                    val diff = dueDate.time - System.currentTimeMillis()
                    val days = diff / (1000 * 60 * 60 * 24)
                    daysLeftText = if (days < 0) "Overdue" else if (days == 0L) "Due Today" else "$days days left"
                }
            } catch (e: Exception) {
                daysLeftText = assignment.dueDate
            }

            tvModulePriority.text = "${assignment.moduleCode} • ${assignment.priority} • $daysLeftText"
            
            val priorityColor = when (assignment.priority.uppercase()) {
                "HIGH" -> itemView.context.getColor(R.color.destructive_red)
                "MEDIUM" -> itemView.context.getColor(R.color.accent_violet)
                else -> itemView.context.getColor(R.color.accent_cyan)
            }
            tvModulePriority.setTextColor(priorityColor)

            itemView.setOnClickListener {
                val currentTime = SystemClock.elapsedRealtime()
                if (currentTime - lastClickTime < 800) return@setOnClickListener
                lastClickTime = currentTime

                val intent = Intent(itemView.context, AssignmentDetailsActivity::class.java).apply {
                    putExtra("ASSIGNMENT_ID", assignment.id)
                    putExtra("assignmentId", assignment.id)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}

class AssignmentDiffCallback : DiffUtil.ItemCallback<AssignmentEntity>() {
    override fun areItemsTheSame(oldItem: AssignmentEntity, newItem: AssignmentEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AssignmentEntity, newItem: AssignmentEntity): Boolean {
        return oldItem == newItem
    }
}
