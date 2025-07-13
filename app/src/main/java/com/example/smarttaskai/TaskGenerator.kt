package com.example.smarttaskai

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TaskGenerator {

    private val apiKey = "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"
    private val apiService: OpenRouterApiService

    init {
        // Create logging interceptor for debugging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Create OkHttpClient with timeout and logging
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(OpenRouterApiService::class.java)
    }

    suspend fun generateTaskSchedule(availableTime: String, taskList: String): Result<TaskScheduleResponse> {
        return try {
            // Create the comprehensive prompt for task scheduling
            val prompt = """
                You are a smart task scheduler. Given the available time and a list of tasks, create a prioritized and organized schedule.
                
                Available Time: $availableTime
                Tasks: $taskList
                
                Please analyze these tasks and:
                1. Prioritize them based on importance and urgency
                2. Estimate realistic time durations for each task
                3. Consider the available time window
                4. Organize them in a logical order
                
                Respond ONLY with a JSON object in this exact format:
                {
                  "tasks": [
                    {
                      "title": "Task name (be specific and clear)",
                      "duration": "X mins" or "X hours",
                      "priority": "High" or "Medium" or "Low"
                    }
                  ]
                }
                
                Rules:
                - Keep task titles clear and actionable
                - Use realistic time estimates (15 mins, 30 mins, 1 hour, etc.)
                - Prioritize urgent/important tasks as "High"
                - Consider the total available time when estimating durations
                - Maintain the same number of tasks or break down large tasks if needed
                
                Respond ONLY with the JSON object, no other text.
            """.trimIndent()

            val request = OpenRouterRequest(
                model = "mistralai/mistral-small-3.2-24b-instruct",
                messages = listOf(
                    Message(
                        role = "user",
                        content = prompt
                    )
                ),
                max_tokens = 1024,
                temperature = 0.3
            )

            Log.d("TaskGenerator", "Sending task generation request to OpenRouter...")
            Log.d("TaskGenerator", "Available time: $availableTime")
            Log.d("TaskGenerator", "Tasks: $taskList")

            val response = apiService.analyzeEmotion(
                authorization = "Bearer $apiKey",
                referer = "https://smarttaskai.app",
                title = "SmartTaskAI task scheduler",
                request = request
            )

            if (response.isSuccessful) {
                val responseBody = response.body()

                if (responseBody?.error != null) {
                    Log.e("TaskGenerator", "API Error: ${responseBody.error.message}")
                    return Result.failure(Exception("API Error: ${responseBody.error.message}"))
                }

                val tasksText = responseBody?.choices?.firstOrNull()?.message?.content?.trim()
                Log.d("TaskGenerator", "Tasks response from API: $tasksText")

                if (tasksText != null) {
                    val taskSchedule = parseTasksResponse(tasksText)
                    if (taskSchedule != null) {
                        Log.d("TaskGenerator", "Successfully generated ${taskSchedule.tasks.size} tasks")
                        Result.success(taskSchedule)
                    } else {
                        Log.e("TaskGenerator", "Failed to parse tasks response")
                        Result.failure(Exception("Failed to parse tasks response"))
                    }
                } else {
                    Log.e("TaskGenerator", "No tasks content received")
                    Result.failure(Exception("No tasks content received"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TaskGenerator", "HTTP Error: ${response.code()} - ${response.message()}")
                Log.e("TaskGenerator", "Error body: $errorBody")
                Result.failure(Exception("HTTP Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("TaskGenerator", "Exception during task generation: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun parseTasksResponse(jsonString: String): TaskScheduleResponse? {
        return try {

            val cleanedJson = jsonString
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonObject = JSONObject(cleanedJson)
            val tasksArray = jsonObject.getJSONArray("tasks")

            val taskList = mutableListOf<TaskItem>()

            for (i in 0 until tasksArray.length()) {
                val taskObj = tasksArray.getJSONObject(i)

                val title = taskObj.optString("title", "Untitled Task")
                val duration = taskObj.optString("duration", "30 mins")
                val priority = taskObj.optString("priority", "Medium")

                // Validate priority
                val validPriorities = listOf("High", "Medium", "Low")
                val normalizedPriority = if (validPriorities.contains(priority)) {
                    priority
                } else {
                    "Medium" // Default fallback
                }

                taskList.add(
                    TaskItem(
                        title = title,
                        duration = duration,
                        priority = normalizedPriority
                    )
                )
            }

            TaskScheduleResponse(tasks = taskList)
        } catch (e: Exception) {
            Log.e("TaskGenerator", "JSON parsing error: ${e.message}", e)
            null
        }
    }

    // Keep the old method for backward compatibility
    suspend fun analyzeJournal(text: String): Result<JournalAnalysis> {
        return try {
            val prompt = """
                Analyze the following journal entry and provide a comprehensive response in JSON format with exactly these fields:
                {
                  "summary": "Brief 1-2 sentence summary of the journal entry",
                  "emotionalTone": "One of: Happy, Sad, Angry, Anxious, Excited, Neutral",
                  "motivationalQuote": "An appropriate motivational quote with attribution",
                  "relaxationActivity": "A specific relaxation or wellness activity suggestion"
                }                
                Journal Entry: $text               
                Respond ONLY with the JSON object, no other text.
            """.trimIndent()

            val request = OpenRouterRequest(
                model = "deepseek/deepseek-r1-0528:free",
                messages = listOf(
                    Message(
                        role = "user",
                        content = prompt
                    )
                ),
                max_tokens = 1024,
                temperature = 0.3
            )

            val response = apiService.analyzeEmotion(
                authorization = "Bearer $apiKey",
                referer = "https://smarttaskai.app",
                title = "SmartTaskAi journal analyzer",
                request = request
            )

            if (response.isSuccessful) {
                val responseBody = response.body()

                if (responseBody?.error != null) {
                    return Result.failure(Exception("API Error: ${responseBody.error.message}"))
                }

                val analysisText = responseBody?.choices?.firstOrNull()?.message?.content?.trim()

                if (analysisText != null) {
                    val analysis = parseAnalysisResponse(analysisText)
                    if (analysis != null) {
                        Result.success(analysis)
                    } else {
                        Result.failure(Exception("Failed to parse analysis response"))
                    }
                } else {
                    Result.failure(Exception("No analysis content received"))
                }
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAnalysisResponse(jsonString: String): JournalAnalysis? {
        return try {
            val jsonObject = JSONObject(jsonString)

            val summary = jsonObject.optString("summary", "Journal entry recorded")
            val emotionalTone = jsonObject.optString("emotionalTone", "Neutral")
            val motivationalQuote = jsonObject.optString("motivationalQuote", "Every day is a fresh start.")
            val relaxationActivity = jsonObject.optString("relaxationActivity", "Take 5 minutes to practice deep breathing")

            val validEmotions = listOf("Happy", "Sad", "Angry", "Anxious", "Excited", "Neutral")
            val normalizedEmotion = if (validEmotions.contains(emotionalTone)) {
                emotionalTone
            } else {
                "Neutral"
            }

            JournalAnalysis(
                summary = summary,
                emotionalTone = normalizedEmotion,
                motivationalQuote = motivationalQuote,
                relaxationActivity = relaxationActivity
            )
        } catch (e: Exception) {
            Log.e("TaskGenerator", "JSON parsing error: ${e.message}", e)
            null
        }
    }
}