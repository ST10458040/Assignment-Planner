package com.example.assignmentplanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface PlannerDao {
    // MODULES
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: ModuleEntity): Long

    @Update
    suspend fun updateModule(module: ModuleEntity): Int

    @Query("SELECT * FROM modules")
    fun getAllModules(): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM modules")
    suspend fun getAllModulesList(): List<ModuleEntity>

    @Query("SELECT * FROM modules WHERE isSynced = 0")
    suspend fun getUnsyncedModules(): List<ModuleEntity>

    @Query("SELECT * FROM modules WHERE code = :code LIMIT 1")
    suspend fun getModuleByCode(code: String): ModuleEntity?

    // ASSIGNMENTS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity): Long

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity): Int

    @Delete
    suspend fun deleteAssignment(assignment: AssignmentEntity): Int

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getAssignmentById(id: Long): AssignmentEntity?

    @Query("SELECT * FROM assignments WHERE id = :id")
    fun getAssignmentByIdFlow(id: Long): Flow<AssignmentEntity?>

    @Query("SELECT * FROM assignments")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments")
    suspend fun getAllAssignmentsList(): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE isSynced = 0")
    suspend fun getUnsyncedAssignments(): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE moduleCode = :moduleCode")
    fun getAssignmentsForModule(moduleCode: String): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE dueDateEpoch BETWEEN :startOfToday AND :endOfToday AND status != 'COMPLETED' ORDER BY dueDateEpoch ASC")
    suspend fun getTodaysAssignments(startOfToday: Long, endOfToday: Long): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE dueDateEpoch > :endOfToday AND status != 'COMPLETED' ORDER BY dueDateEpoch ASC")
    suspend fun getUpcomingAssignments(endOfToday: Long): List<AssignmentEntity>

    // SUBTASKS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>): List<Long>

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity): Int

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity): Int

    @Query("SELECT * FROM subtasks WHERE assignmentId = :assignmentId")
    fun getSubtasksForAssignment(assignmentId: Long): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE assignmentId = :assignmentId AND isCompleted = 0 ORDER BY id ASC")
    suspend fun getPendingSubtasksForAssignment(assignmentId: Long): List<SubtaskEntity>
    
    @Query("SELECT * FROM subtasks")
    suspend fun getAllSubtasksList(): List<SubtaskEntity>

    @Query("SELECT * FROM subtasks WHERE isSynced = 0")
    suspend fun getUnsyncedSubtasks(): List<SubtaskEntity>
    
    // USER PROFILE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM user_profile WHERE uid = :uid LIMIT 1")
    suspend fun getUserProfile(uid: String): UserEntity?

    @Query("SELECT (SELECT COUNT(*) FROM modules WHERE isSynced = 0) + (SELECT COUNT(*) FROM assignments WHERE isSynced = 0) + (SELECT COUNT(*) FROM subtasks WHERE isSynced = 0)")
    suspend fun getPendingSyncRecordsCount(): Int
    
    // NOTIFICATIONS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long
    
    @Query("SELECT * FROM notifications ORDER BY timestampEpoch DESC")
    suspend fun getAllNotifications(): List<NotificationEntity>
    
    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllNotificationsAsRead(): Int
}
