package com.example.smarttaskai

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*


class Todo : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var dateHeaderText: TextView
    private lateinit var taskAdapter: TaskAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val tasksList = mutableListOf<TodoTask>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_todo, container, false)

        initializeViews(view)
        setupRecyclerView()
        loadTasks()

        return view
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.tasksRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        dateHeaderText = view.findViewById(R.id.dateHeaderText)

        // Set current date
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        dateHeaderText.text = dateFormat.format(Date())
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(tasksList,
            onStartClick = { task -> updateTaskStatus(task, "In Progress") },
            onCompleteClick = { task -> updateTaskStatus(task, "Completed") }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = taskAdapter
    }

    private fun loadTasks() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            showEmptyState("Please log in to view your tasks")
            return
        }

        val currentDate = getCurrentDate()

        db.collection("todos")
            .document(userId)
            .collection("dates")
            .document(currentDate)
            .collection("tasks")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("TodoFragment", "Listen failed: ${e.message}", e)
                    showEmptyState("Error loading tasks")
                    return@addSnapshotListener
                }

                tasksList.clear()

                if (snapshots != null && !snapshots.isEmpty) {
                    for (document in snapshots.documents) {
                        try {
                            val task = document.toObject(TodoTask::class.java)
                            if (task != null) {
                                tasksList.add(task.copy(id = document.id))
                            }
                        } catch (ex: Exception) {
                            Log.e("TodoFragment", "Error parsing task: ${ex.message}")
                        }
                    }

                    Log.d("TodoFragment", "Loaded ${tasksList.size} tasks")
                    showTaskList()
                } else {
                    Log.d("TodoFragment", "No tasks found for today")
                    showEmptyState("No tasks for today.\nCreate a schedule to get started! 📝")
                }

                taskAdapter.notifyDataSetChanged()
            }
    }

    private fun updateTaskStatus(task: TodoTask, newStatus: String) {
        val userId = auth.currentUser?.uid ?: return
        val currentDate = getCurrentDate()

        db.collection("todos")
            .document(userId)
            .collection("dates")
            .document(currentDate)
            .collection("tasks")
            .document(task.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                Log.d("TodoFragment", "Task status updated to: $newStatus")

                val statusMessage = when (newStatus) {
                    "In Progress" -> "✅ Task started! You've got this! 💪"
                    "Completed" -> "🎉 Great job! Task completed! 🌟"
                    else -> "Task updated"
                }

                // Safely show Toast only if context is available
                context?.let { ctx ->
                    Toast.makeText(ctx, statusMessage, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TodoFragment", "Failed to update task status: ${exception.message}")

                context?.let { ctx ->
                    Toast.makeText(ctx, "❌ Failed to update task", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun showTaskList() {
        recyclerView.visibility = View.VISIBLE
        emptyStateText.visibility = View.GONE
    }

    private fun showEmptyState(message: String) {
        recyclerView.visibility = View.GONE
        emptyStateText.visibility = View.VISIBLE
        emptyStateText.text = message
    }

    override fun onResume() {
        super.onResume()
        // Refresh tasks when fragment becomes visible
        loadTasks()
    }
}