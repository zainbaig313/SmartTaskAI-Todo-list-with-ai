package com.example.smarttaskai

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val tasks: List<TodoTask>,
    private val onStartClick: (TodoTask) -> Unit,
    private val onCompleteClick: (TodoTask) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.taskCardView)
        val titleText: TextView = itemView.findViewById(R.id.taskTitleText)
        val durationText: TextView = itemView.findViewById(R.id.taskDurationText)
        val priorityText: TextView = itemView.findViewById(R.id.taskPriorityText)
        val statusText: TextView = itemView.findViewById(R.id.taskStatusText)
        val startButton: Button = itemView.findViewById(R.id.startButton)
        val completeButton: Button = itemView.findViewById(R.id.completeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Set task details
        holder.titleText.text = task.title
        holder.durationText.text = "⏱️ ${task.duration}"

        // Set priority with color coding
        holder.priorityText.text = when (task.priority) {
            "High" -> "🔴 High Progress"
            "Low" -> "🟢 Low Progress"
            else -> "🟡 Medium Progress"
        }

        // Set status and configure buttons based on current status
        when (task.status) {
            "Not Started" -> {
                holder.statusText.text = "📋 Not Started"
                holder.statusText.setTextColor(Color.GRAY)

                holder.startButton.visibility = View.VISIBLE
                holder.startButton.isEnabled = true
                holder.startButton.text = "▶️ Start"
                holder.startButton.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_light)
                )

                holder.completeButton.visibility = View.VISIBLE
                holder.completeButton.isEnabled = false
                holder.completeButton.text = "✅ Complete"
                holder.completeButton.alpha = 0.5f

                holder.cardView.setCardBackgroundColor(Color.WHITE)
            }

            "In Progress" -> {
                holder.statusText.text = "⚡ In Progress"
                holder.statusText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark)
                )

                holder.startButton.visibility = View.VISIBLE
                holder.startButton.isEnabled = false
                holder.startButton.text = "▶️ Started"
                holder.startButton.alpha = 0.5f

                holder.completeButton.visibility = View.VISIBLE
                holder.completeButton.isEnabled = true
                holder.completeButton.text = "✅ Complete"
                holder.completeButton.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_light)
                )
                holder.completeButton.alpha = 1.0f

                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_light)
                )
            }

            "Completed" -> {
                holder.statusText.text = "🎉 Completed"
                holder.statusText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
                )

                holder.startButton.visibility = View.GONE
                holder.completeButton.visibility = View.VISIBLE
                holder.completeButton.isEnabled = false
                holder.completeButton.text = "✅ Done"
                holder.completeButton.alpha = 0.7f

                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_light)
                )

                // Add strikethrough effect to completed tasks
                holder.titleText.paintFlags = holder.titleText.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        // Set click listeners
        holder.startButton.setOnClickListener {
            if (task.status == "Not Started") {
                onStartClick(task)
            }
        }

        holder.completeButton.setOnClickListener {
            if (task.status == "In Progress") {
                onCompleteClick(task)
            }
        }

        // Add subtle animation for better UX
        holder.cardView.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(200)
            .start()
    }

    override fun getItemCount(): Int = tasks.size
}