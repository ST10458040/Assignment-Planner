package com.example.assignmentplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modules")
data class ModuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String = "",
    val name: String = "",
    val lecturer: String = "Dr. K. Naidoo",
    val termSemester: String = "Year 3 • Semester 2",
    val nqfLevel: String = "6",
    var isSynced: Boolean = false
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val moduleId: Long = 0L,
    val moduleCode: String = "",
    val dueDate: String = "",
    val dueTime: String = "",
    val priority: String = "",
    val status: String = "", // ACTIVE, DRAFT, COMPLETED
    val isNotificationEnabled: Boolean = false,
    val description: String = "",
    var isSynced: Boolean = false,
    val dueDateEpoch: Long = 0L
)

@Entity(tableName = "subtasks")
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assignmentId: Long = 0L,
    val title: String = "",
    val isCompleted: Boolean = false,
    var isSynced: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val profilePictureUrl: String = ""
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val timestampEpoch: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "ASSIGNMENT", // "ASSIGNMENT", "MODULE", "REMINDER"
    val relatedAssignmentId: Long = 0L
)
