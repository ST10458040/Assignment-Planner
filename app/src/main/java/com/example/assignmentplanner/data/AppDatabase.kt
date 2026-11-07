package com.example.assignmentplanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

/**
 * AppDatabase is the main entry point for the underlying Room database.
 * It manages the persistence of modules, assignments, subtasks, and user profiles locally.
 */
@Database(entities = [ModuleEntity::class, AssignmentEntity::class, SubtaskEntity::class, UserEntity::class, NotificationEntity::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plannerDao(): PlannerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns a singleton instance of the database, building it if it doesn't exist.
         * Ensures thread safety with a double-checked locking mechanism.
         *
         * @param context App context.
         * @return AppDatabase instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d("AppDatabase", "Building new Room database instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "planner_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.i("AppDatabase", "Room Database created, running pre-populate script")
                        // Prepopulate default academic modules for Demo/Rubric purposes
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).plannerDao()
                            dao.insertModule(ModuleEntity(code = "WEDE6021", name = "Web Development"))
                            dao.insertModule(ModuleEntity(code = "PROG6212", name = "Programming 2B"))
                            dao.insertModule(ModuleEntity(code = "DATA6222", name = "Database Design & Implementation"))
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
