package com.example.smarttaskai

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class Schedule : Fragment() {
    private lateinit var timeFromEditText: EditText
    private lateinit var timeToEditText: EditText
    private lateinit var tasksEditText: EditText
    private lateinit var generateButton: Button
    private lateinit var progressBar: ProgressBar

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val taskGenerator = TaskGenerator()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)

        initializeViews(view)
        setupClickListeners()

        return view
    }

    private fun initializeViews(view: View) {
        timeFromEditText = view.findViewById(R.id.timeFromEditText)
        timeToEditText = view.findViewById(R.id.timeToEditText)
        tasksEditText = view.findViewById(R.id.tasksEditText)
        generateButton = view.findViewById(R.id.generateButton)
        progressBar = view.findViewById(R.id.progressBar)

        // Set hints for better UX
        timeFromEditText.hint = "e.g., 10:00 AM"
        timeToEditText.hint = "e.g., 3:00 PM"
        tasksEditText.hint = "e.g., clean room, wash dishes, go to hospital, study for exam"
    }

    private fun setupClickListeners() {
        generateButton.setOnClickListener {
            val timeFrom = timeFromEditText.text.toString().trim()
            val timeTo = timeToEditText.text.toString().trim()
            val tasks = tasksEditText.text.toString().trim()

            if (validateInput(timeFrom, timeTo, tasks)) {
                val availableTime = "$timeFrom - $timeTo"
                generateTaskSchedule(availableTime, tasks)
            }
        }
    }

    private fun validateInput(timeFrom: String, timeTo: String, tasks: String): Boolean {
        when {
            timeFrom.isEmpty() -> {
                Toast.makeText(requireContext(), "Please enter start time", Toast.LENGTH_SHORT).show()
                return false
            }
            timeTo.isEmpty() -> {
                Toast.makeText(requireContext(), "Please enter end time", Toast.LENGTH_SHORT).show()
                return false
            }
            tasks.isEmpty() -> {
                Toast.makeText(requireContext(), "Please enter your tasks", Toast.LENGTH_SHORT).show()
                return false
            }
            else -> return true
        }
    }

    private fun generateTaskSchedule(availableTime: String, tasks: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                Log.d("ScheduleFragment", "Generating task schedule...")
                Log.d("ScheduleFragment", "Available time: $availableTime")
                Log.d("ScheduleFragment", "Tasks: $tasks")

                val result = taskGenerator.generateTaskSchedule(availableTime, tasks)

                if (result.isSuccess) {
                    val taskSchedule = result.getOrNull()
                    if (taskSchedule != null) {
                        Log.d("ScheduleFragment", "Task schedule generated successfully with ${taskSchedule.tasks.size} tasks")

                        // Save to Firestore
                        saveTasksToFirestore(taskSchedule.tasks, availableTime)

                    } else {
                        Log.e("ScheduleFragment", "Task schedule was null")
                        Toast.makeText(requireContext(), "Failed to generate schedule", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("ScheduleFragment", "Task generation failed: ${error?.message}")
                    Toast.makeText(requireContext(), "Failed to generate schedule: ${error?.message}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("ScheduleFragment", "Exception during task generation: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun saveTasksToFirestore(tasks: List<TaskItem>, availableTime: String) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "Please log in to save tasks", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDate = getCurrentDate()
        val timestamp = System.currentTimeMillis()

        // Create a batch write for all tasks
        val batch = db.batch()

        // Save schedule info
        val scheduleRef = db.collection("schedules").document(userId).collection("dates").document(currentDate)
        val scheduleData = hashMapOf(
            "userId" to userId,
            "date" to currentDate,
            "availableTime" to availableTime,
            "totalTasks" to tasks.size,
            "timestamp" to timestamp
        )
        batch.set(scheduleRef, scheduleData)

        // Save individual tasks
        tasks.forEachIndexed { index, task ->
            val taskRef = db.collection("todos").document(userId).collection("dates").document(currentDate)
                .collection("tasks").document()

            val todoTask = TodoTask(
                id = taskRef.id,
                title = task.title,
                duration = task.duration,
                priority = task.priority,
                status = "Not Started",
                userId = userId,
                date = currentDate,
                timestamp = timestamp + index // Ensure ordering
            )

            batch.set(taskRef, todoTask)
        }

        // Commit the batch
        batch.commit()
            .addOnSuccessListener {
                Log.d("ScheduleFragment", "Tasks saved successfully")
                Toast.makeText(requireContext(), "✅ Schedule generated! ${tasks.size} tasks created", Toast.LENGTH_SHORT).show()

                // Clear form
                clearForm()

                // Navigate to TodoFragment
                navigateToTodoFragment()
            }
            .addOnFailureListener { exception ->
                Log.e("ScheduleFragment", "Failed to save tasks: ${exception.message}", exception)
                Toast.makeText(requireContext(), "Failed to save tasks: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        generateButton.isEnabled = !show
        generateButton.text = if (show) "🤖 Generating..." else "✨ Generate Schedule"

        if (show) {
            generateButton.alpha = 0.6f
        } else {
            generateButton.alpha = 1.0f
        }
    }

    private fun clearForm() {
        timeFromEditText.setText("")
        timeToEditText.setText("")
        tasksEditText.setText("")
    }

    private fun navigateToTodoFragment() {
        try {
            // Get the parent activity and navigate using bottom navigation
            val activity = requireActivity()
            if (activity is Home) {
                // Find the bottom navigation view and programmatically select the todo tab
                val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                bottomNav?.selectedItemId = R.id.navigation_todo
            }
        } catch (e: Exception) {
            Log.e("ScheduleFragment", "Navigation failed: ${e.message}", e)
            Toast.makeText(requireContext(), "Please check your To-Do list manually", Toast.LENGTH_SHORT).show()
        }
    }
}