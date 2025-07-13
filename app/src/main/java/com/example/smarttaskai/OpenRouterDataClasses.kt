package com.example.smarttaskai

// Request classes
data class OpenRouterRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int = 100,
    val temperature: Double = 0.3
)

data class Message(
    val role: String,
    val content: String
)

// Response classes
data class OpenRouterResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?,
    val error: ApiError?
)

data class Choice(
    val message: MessageResponse?,
    val finish_reason: String?
)

data class MessageResponse(
    val role: String?,
    val content: String?
)

data class Usage(
    val prompt_tokens: Int?,
    val completion_tokens: Int?,
    val total_tokens: Int?
)

data class ApiError(
    val message: String?,
    val type: String?,
    val code: String?
)

// New comprehensive journal analysis data class
data class JournalAnalysis(
    val summary: String,
    val emotionalTone: String,
    val motivationalQuote: String,
    val relaxationActivity: String
)

// Task-related data classes
data class TaskScheduleResponse(
    val tasks: List<TaskItem>
)

data class TaskItem(
    val title: String,
    val duration: String,
    val priority: String = "Medium", // High, Medium, Low
    val estimatedTime: String = duration // For backward compatibility
)

// Todo data class for Firestore
data class TodoTask(
    val id: String = "",
    val title: String = "",
    val duration: String = "",
    val priority: String = "Medium",
    val status: String = "Not Started", // Not Started, In Progress, Completed
    val userId: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

