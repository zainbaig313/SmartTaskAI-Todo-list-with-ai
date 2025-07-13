package com.example.smarttaskai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class Progress : Fragment() {
    private lateinit var dateHeaderText: TextView
    private lateinit var circularProgressBar: ProgressBar
    private lateinit var linearProgressBar: ProgressBar
    private lateinit var progressPercentageText: TextView
    private lateinit var progressPercentText: TextView
    private lateinit var completedTasksText: TextView
    private lateinit var inProgressTasksText: TextView
    private lateinit var totalTasksText: TextView
    private lateinit var progressStatsText: TextView
    private lateinit var motivationalMessageText: TextView
    private lateinit var motivationalSubText: TextView
    private lateinit var logoutButton: Button
    private lateinit var emptyStateCard: CardView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)

        initializeViews(view)
        setupClickListeners()
        loadProgressData()

        return view
    }

    private fun initializeViews(view: View) {
        dateHeaderText = view.findViewById(R.id.dateHeaderText)
        circularProgressBar = view.findViewById(R.id.circularProgressBar)
        linearProgressBar = view.findViewById(R.id.linearProgressBar)
        progressPercentageText = view.findViewById(R.id.progressPercentageText)
        progressPercentText = view.findViewById(R.id.progressPercentText)
        completedTasksText = view.findViewById(R.id.completedTasksText)
        inProgressTasksText = view.findViewById(R.id.inProgressTasksText)
        totalTasksText = view.findViewById(R.id.totalTasksText)
        progressStatsText = view.findViewById(R.id.progressStatsText)
        motivationalMessageText = view.findViewById(R.id.motivationalMessageText)
        motivationalSubText = view.findViewById(R.id.motivationalSubText)
        logoutButton = view.findViewById(R.id.logoutButton)
        emptyStateCard = view.findViewById(R.id.emptyStateCard)

        // Set current date
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        dateHeaderText.text = dateFormat.format(Date())
    }

    private fun setupClickListeners() {
        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadProgressData() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            showEmptyState("Please log in to view your progress")
            return
        }

        val currentDate = getCurrentDate()

        db.collection("todos")
            .document(userId)
            .collection("dates")
            .document(currentDate)
            .collection("tasks")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("ProgressFragment", "Listen failed: ${e.message}", e)
                    showEmptyState("Error loading progress data")
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val tasks = mutableListOf<TodoTask>()

                    for (document in snapshots.documents) {
                        try {
                            val task = document.toObject(TodoTask::class.java)
                            if (task != null) {
                                tasks.add(task.copy(id = document.id))
                            }
                        } catch (ex: Exception) {
                            Log.e("ProgressFragment", "Error parsing task: ${ex.message}")
                        }
                    }

                    Log.d("ProgressFragment", "Loaded ${tasks.size} tasks for progress tracking")
                    updateProgressDisplay(tasks)
                } else {
                    Log.d("ProgressFragment", "No tasks found for progress tracking")
                    showEmptyState("No tasks yet!\nCreate a schedule to start tracking your progress 📝")
                }
            }
    }

    private fun updateProgressDisplay(tasks: List<TodoTask>) {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.status == "Completed" }
        val inProgressTasks = tasks.count { it.status == "In Progress" }
        val notStartedTasks = tasks.count { it.status == "Not Started" }

        val progressPercentage = if (totalTasks > 0) {
            (completedTasks * 100) / totalTasks
        } else {
            0
        }

        // Update UI elements
        totalTasksText.text = totalTasks.toString()
        completedTasksText.text = completedTasks.toString()
        inProgressTasksText.text = inProgressTasks.toString()

        // Update progress bars
        circularProgressBar.progress = progressPercentage
        linearProgressBar.progress = progressPercentage

        // Update percentage texts
        progressPercentageText.text = "$progressPercentage%"
        progressPercentText.text = "$progressPercentage%"

        // Update stats text
        progressStatsText.text = "$completedTasks of $totalTasks tasks completed"

        // Update motivational messages
        updateMotivationalMessage(completedTasks, totalTasks, progressPercentage)

        // Show progress data (hide empty state)
        emptyStateCard.visibility = View.GONE

        Log.d("ProgressFragment", "Progress updated: $completedTasks/$totalTasks ($progressPercentage%)")
    }

    private fun updateMotivationalMessage(completed: Int, total: Int, percentage: Int) {
        val remaining = total - completed

        val (message, subMessage) = when {
            percentage == 0 -> {
                "🚀 Ready to start your journey!" to "You've got $total tasks waiting. Let's begin!"
            }
            percentage == 100 -> {
                "🎉 Congratulations! All tasks completed!" to "You're absolutely crushing it today! 🌟"
            }
            percentage >= 75 -> {
                "🔥 You're on fire!" to "Almost there! Just $remaining more tasks to go."
            }
            percentage >= 50 -> {
                "💪 Great momentum!" to "You're halfway there! $remaining tasks remaining."
            }
            percentage >= 25 -> {
                "🎯 Good progress!" to "Keep up the great work! $remaining more to go."
            }
            else -> {
                "🌱 Every step counts!" to "You've started strong! $remaining tasks remaining."
            }
        }

        motivationalMessageText.text = message
        motivationalSubText.text = subMessage
    }

    private fun showEmptyState(message: String) {
        emptyStateCard.visibility = View.VISIBLE
        // You can update the empty state message if needed
        val emptyStateText = emptyStateCard.findViewById<TextView>(R.id.emptyStateText)
        emptyStateText?.text = message
    }

    private fun showLogoutConfirmation() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to log out?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes") { _, _ ->
            performLogout()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun performLogout() {
        try {
            auth.signOut()
            Log.d("ProgressFragment", "User logged out successfully")

            Toast.makeText(requireContext(), "👋 Logged out successfully!", Toast.LENGTH_SHORT).show()

            // Navigate to login activity
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        } catch (e: Exception) {
            Log.e("ProgressFragment", "Logout failed: ${e.message}", e)
            Toast.makeText(requireContext(), "Logout failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    override fun onResume() {
        super.onResume()
        // Refresh progress data when fragment becomes visible
        loadProgressData()
    }
}