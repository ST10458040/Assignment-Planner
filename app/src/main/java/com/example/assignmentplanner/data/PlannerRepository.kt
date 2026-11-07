package com.example.assignmentplanner.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import android.util.Log

/**
 * PlannerRepository acts as the single source of truth for data access.
 * It manages operations between local Room DB and remote Firebase Cloud Firestore,
 * ensuring offline-first architecture principles are maintained.
 *
 * @param plannerDao The Data Access Object for local Room operations.
 */
class PlannerRepository(private val plannerDao: PlannerDao) {

    private val TAG = "PlannerRepository"

    val allModules: Flow<List<ModuleEntity>> = plannerDao.getAllModules()
    val allAssignments: Flow<List<AssignmentEntity>> = plannerDao.getAllAssignments()

    suspend fun insertModule(module: ModuleEntity) = plannerDao.insertModule(module)
    /**
     * Inserts a new assignment into the local Room database.
     * Starts as unsynced by default.
     *
     * @param assignment The AssignmentEntity object containing task details.
     * @return The auto-generated Long ID of the new entry.
     */
    suspend fun insertAssignment(assignment: AssignmentEntity): Long {
        Log.d(TAG, "Inserting new assignment: ${assignment.title}")
        return plannerDao.insertAssignment(assignment)
    }

    suspend fun updateAssignment(assignment: AssignmentEntity) {
        plannerDao.updateAssignment(assignment)
    }

    suspend fun deleteAssignment(assignment: AssignmentEntity) {
        plannerDao.deleteAssignment(assignment)
    }

    suspend fun getAssignmentById(id: Long): AssignmentEntity? {
        return plannerDao.getAssignmentById(id)
    }
    
    fun getAssignmentByIdFlow(id: Long): Flow<AssignmentEntity?> {
        return plannerDao.getAssignmentByIdFlow(id)
    }

    fun getAssignmentsForModule(moduleCode: String): Flow<List<AssignmentEntity>> {
        return plannerDao.getAssignmentsForModule(moduleCode)
    }

    suspend fun getTodaysAssignments(startOfToday: Long, endOfToday: Long): List<AssignmentEntity> {
        return plannerDao.getTodaysAssignments(startOfToday, endOfToday)
    }

    suspend fun getUpcomingAssignments(endOfToday: Long): List<AssignmentEntity> {
        return plannerDao.getUpcomingAssignments(endOfToday)
    }

    suspend fun insertSubtask(subtask: SubtaskEntity) {
        plannerDao.insertSubtask(subtask)
    }

    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>) {
        plannerDao.insertSubtasks(subtasks)
    }

    suspend fun updateSubtask(subtask: SubtaskEntity) {
        plannerDao.updateSubtask(subtask)
    }
    
    suspend fun deleteSubtask(subtask: SubtaskEntity) {
        plannerDao.deleteSubtask(subtask)
    }

    fun getSubtasksForAssignment(assignmentId: Long): Flow<List<SubtaskEntity>> {
        return plannerDao.getSubtasksForAssignment(assignmentId)
    }

    suspend fun getPendingSubtasksForAssignment(assignmentId: Long): List<SubtaskEntity> {
        return plannerDao.getPendingSubtasksForAssignment(assignmentId)
    }

    suspend fun getPendingSyncRecordsCount(): Int {
        return plannerDao.getPendingSyncRecordsCount()
    }

    suspend fun insertNotification(notification: NotificationEntity): Long {
        return plannerDao.insertNotification(notification)
    }

    suspend fun getAllNotifications(): List<NotificationEntity> {
        return plannerDao.getAllNotifications()
    }

    suspend fun getNotifications(): List<NotificationEntity> {
        return plannerDao.getAllNotifications()
    }

    suspend fun markAllNotificationsAsRead(): Int {
        return plannerDao.markAllNotificationsAsRead()
    }

    // Firestore Integration
    private val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private val userId: String?
        get() = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Pushes all unsynced local changes (Modules, Assignments, Subtasks)
     * to Firebase Cloud Firestore for the authenticated user.
     * This ensures the remote database matches the local offline changes.
     */
    suspend fun saveToFirestore() {
        val uid = userId ?: return
        Log.i(TAG, "Initiating push sync to Firestore for user: $uid")
        
        try {
            // Save Modules (Only Unsynced)
            val modules = plannerDao.getUnsyncedModules()
            Log.d(TAG, "Found ${modules.size} unsynced modules. Pushing to cloud...")
            for (module in modules) {
                firestore.collection("users").document(uid).collection("modules")
                    .document(module.id.toString()).set(module)
                    .await()
                module.isSynced = true
                plannerDao.updateModule(module)
            }

            // Save Assignments (Only Unsynced)
            val assignments = plannerDao.getUnsyncedAssignments()
            Log.d(TAG, "Found ${assignments.size} unsynced assignments. Pushing to cloud...")
            for (assignment in assignments) {
                firestore.collection("users").document(uid).collection("assignments")
                    .document(assignment.id.toString()).set(assignment)
                    .await()
                assignment.isSynced = true
                plannerDao.updateAssignment(assignment)
            }
            
            // Save Subtasks (Only Unsynced)
            val subtasks = plannerDao.getUnsyncedSubtasks()
            Log.d(TAG, "Found ${subtasks.size} unsynced subtasks. Pushing to cloud...")
            for (subtask in subtasks) {
                 firestore.collection("users").document(uid).collection("subtasks")
                     .document(subtask.id.toString()).set(subtask)
                     .await()
                 subtask.isSynced = true
                 plannerDao.updateSubtask(subtask)
            }
            Log.i(TAG, "Push sync to Firestore completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push local data to Firestore", e)
            e.printStackTrace()
        }
    }

    /**
     * Pulls the latest changes from Firebase Cloud Firestore and injects them
     * into the local Room DB, making sure offline data remains current.
     */
    suspend fun syncFromFirestore() {
        val uid = userId ?: return
        Log.i(TAG, "Initiating pull sync from Firestore for user: $uid")
        
        try {
            // Sync Modules
            val modulesSnapshot = firestore.collection("users").document(uid).collection("modules").get().await()
            for (doc in modulesSnapshot.documents) {
                val module = doc.toObject(ModuleEntity::class.java)
                if (module != null) {
                    module.isSynced = true
                    // Need a way to update or insert module
                    plannerDao.insertModule(module)
                }
            }
            
            // Sync Assignments
            val assignmentsSnapshot = firestore.collection("users").document(uid).collection("assignments").get().await()
            for (doc in assignmentsSnapshot.documents) {
                val assignment = doc.toObject(AssignmentEntity::class.java)
                if (assignment != null) {
                    assignment.isSynced = true
                    plannerDao.insertAssignment(assignment)
                }
            }
            
            // Sync Subtasks
            val subtasksSnapshot = firestore.collection("users").document(uid).collection("subtasks").get().await()
            for (doc in subtasksSnapshot.documents) {
                val subtask = doc.toObject(SubtaskEntity::class.java)
                if (subtask != null) {
                    subtask.isSynced = true
                    plannerDao.insertSubtask(subtask)
                }
            }
            Log.i(TAG, "Pull sync from Firestore completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull remote data from Firestore", e)
            e.printStackTrace()
        }
    }
}
