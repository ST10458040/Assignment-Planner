package com.example.assignmentplanner.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.assignmentplanner.data.AppDatabase
import com.example.assignmentplanner.data.PlannerRepository

/**
 * SyncWorker handles background synchronization of the offline RoomDB with Firebase Firestore.
 * It is invoked by WorkManager whenever the device regains network connectivity.
 */
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    /**
     * Executes the background sync logic. Pushes local unsynced data, then pulls cloud data.
     * @return Result.success() on completion, or Result.retry() if a failure occurs.
     */
    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Starting sync work...")

        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = PlannerRepository(database.plannerDao())

            // 1. Push unsynced local RoomDB changes to Firestore
            repository.saveToFirestore()

            // 2. Pull new cloud changes to local RoomDB
            repository.syncFromFirestore()

            Log.d("SyncWorker", "Sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error during sync: ${e.message}", e)
            // Return retry if we want to retry on failure (e.g., network issue)
            Result.retry()
        }
    }
}
