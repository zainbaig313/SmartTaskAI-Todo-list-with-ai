# 🤖 SmartTask AI - Daily Task Scheduler App

SmartTask AI is an intelligent daily task manager built with Android (Kotlin) that uses AI to auto-schedule, prioritize, and track your tasks based on your available time. Whether it's studying, appointments, or errands—SmartTask AI helps you stay focused and organized.

---

## 📱 Features

✅ **AI-powered Smart Scheduler**  
Just enter your available time and your task list — the app uses OpenRouter AI to smartly prioritize, break down, and distribute tasks within your time frame.

✅ **To-Do Fragment**  
Displays all scheduled tasks with status indicators. Each task has:
- ▶️ **Start button** → changes task status to "In Progress"
- ✅ **Complete button** → changes status to "Completed" and line-throughs the task

✅ **Progress Fragment**  
Tracks your daily progress with:
- 📊 Progress bars (linear & circular)
- 📈 Motivational messages
- 📅 Task stats: total, completed, in-progress
- 👋 Logout option

✅ **Realtime Firestore Sync**  
Tasks are stored in Firebase Firestore based on user and date.

✅ **Daily Refresh**  
Each day shows only that day's tasks—ensuring focus.

---

## 🧠 How It Works

1. **User inputs time window + task list**
2. **OpenRouter API** (via GPT-like model) generates optimized schedule
3. Tasks are saved to **Firebase Firestore**
4. To-do list & progress auto-update based on real-time status

---

## 🛠️ Tech Stack

| Layer            | Tech Used                         |
|------------------|-----------------------------------|
| Language         | Kotlin                            |
| Backend          | Firebase Authentication + Firestore |
| AI Integration   | OpenRouter (Mistral model via API)|
| UI Components    | Material Components + RecyclerView|
| Dependency Mgmt  | Gradle                            |
| API/Networking   | Retrofit2, OkHttp, JSON parsing   |
| Architecture     | MVVM-lite + Lifecycle-aware coroutines |

---

## 🔗 Screenshots

> 📽️ Screen recording of the app in action will be posted on [LinkedIn](https://linkedin.com/in/your-username)  

---

## 👨‍💻 Author

**Zain Baig**  
📍 Android | AI Enthusiast | Final Year CS Student  
💼 *Currently open to internship opportunities!*  
📫 [LinkedIn](https://www.linkedin.com/in/zain-baig313/)) 

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
