Izabelo - Assignment Planner 

Izabelo is a premium, offline-first assignment and module tracker designed for university students to organize deadlines, manage subtasks, and track their academic streak seamlessly. 

 

Features 

• Offline-First Room DB: All assignments, subtasks, and profiles are cached locally on device utilizing Android Room allowing complete functionality during network outages. 

• REST API Synchronization: Background triggers to sync data asynchronously to an external REST interface (Ktor) when connected to Eduroam or Wi-Fi. 

• Gamification Mechanics: Academic momentum is tracked through "Study Streaks" and "XP Velocity" metrics to reward consistent submission history. 

• Premium Glassmorphism UX: Engineered with a fully responsive Dark Neon/Glassmorphism theme emphasizing clear visual hierarchies for urgency (e.g., URGENT red tags, MEDIUM cyan tags). 

• Subtask Milestones: Break large projects down into manageable checklist items that dynamically increment progress bars. 

 

Tech Stack 

•Architecture: Model-View-ViewModel (MVVM) 

• Language: Kotlin 

• Database: Android Room (SQLite v4) 

• Networking: Retrofit2 & Gson / HttpURLConnection 

• UI Framework: Native Android XML with Material3 components. 

 

Setup Instructions 

1.Clone the repository to your local machine. 

2.Open the project in Android Studio. 

3.Sync Gradle to fetch dependencies (Room, Retrofit, Coroutines). 

4.Run app:assembleDebug or deploy straight to your emulator. 

Usage of AI Tools 

During the development and prototyping of this application, AI assistants (such as Google Gemini/Android Studio Bot) were utilized to accelerate boilerplate generation and thematic design conversion. Specifically, AI was prompted to: 

1.Translate provided HTML/Tailwind mockup files into native ConstraintLayout and MaterialCardView XML definitions. 

2.Establish baseline Kotlin Activity classes and intent wireups for all 12+ navigation destinations. 

3.Generate initial JUnit validation algorithms (AuthValidatorTest.kt) for input string verification. (AI tooling was strictly used as an accelerant and pair-programmer. All architecture and logic paths were verified manually.) 

 

Automation & Workflows 

This repository is configured with a GitHub Action (build.yml) that automatically executes ./gradlew testDebugUnitTest and ./gradlew assembleDebug on every push to the main branch to ensure regressions are blocked. 
