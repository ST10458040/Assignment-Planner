package com.example.assignmentplanner

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmentplanner.data.SubtaskEntity

class SubtaskAdapter(
    private val onSubtaskChecked: (SubtaskEntity, Boolean) -> Unit
) : ListAdapter<SubtaskEntity, SubtaskAdapter.SubtaskViewHolder>(SubtaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subtask, parent, false)
        return SubtaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubtaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cbCompleted: CheckBox = itemView.findViewById(R.id.cb_completed)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)

        fun bind(subtask: SubtaskEntity) {
            tvTitle.text = subtask.title
            cbCompleted.setOnCheckedChangeListener(null)
            cbCompleted.isChecked = subtask.isCompleted
            
            if (subtask.isCompleted) {
                tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvStatus.text = "Done"
                tvStatus.setTextColor(itemView.context.getColor(R.color.tertiary_green))
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvStatus.text = "In Progress"
                tvStatus.setTextColor(itemView.context.getColor(R.color.accent_cyan))
            }

            cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onSubtaskChecked(subtask, isChecked)
            }
        }
    }
}

class SubtaskDiffCallback : DiffUtil.ItemCallback<SubtaskEntity>() {
    override fun areItemsTheSame(oldItem: SubtaskEntity, newItem: SubtaskEntity): Boolean {
        // use title fallback if ID is 0 (new)
        if (oldItem.id == 0L && newItem.id == 0L) return oldItem.title == newItem.title
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SubtaskEntity, newItem: SubtaskEntity): Boolean {
        return oldItem == newItem
    }
}
