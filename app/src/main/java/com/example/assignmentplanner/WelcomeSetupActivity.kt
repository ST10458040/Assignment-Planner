package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class WelcomeSetupActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome_setup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnAddModule = findViewById<Button>(R.id.btn_add_module)
        val llModulesContainer = findViewById<LinearLayout>(R.id.ll_modules_container)
        val inflater = LayoutInflater.from(this)

        val priorities = arrayOf("Low", "Medium", "High")
        val priorityAdapter =
            android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, priorities)
        val actvPriority = findViewById<android.widget.AutoCompleteTextView>(R.id.actv_priority)
        actvPriority?.setAdapter(priorityAdapter)

        btnAddModule.setOnClickListener {
            val moduleRow = inflater.inflate(R.layout.item_module_input, llModulesContainer, false)
            llModulesContainer.addView(moduleRow)
        }

        findViewById<Button>(R.id.btn_save_launch).setOnClickListener {
            val courseName = findViewById<EditText>(R.id.et_course_name)?.text.toString().trim()
            val academicYear = findViewById<EditText>(R.id.et_academic_year)?.text.toString().trim()
            val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
            prefs.edit()
                .putString("user_course_name", courseName)
                .putString("user_academic_year", academicYear)
                .apply()

            val moduleCodes = mutableListOf<String>()
            val dao = com.example.assignmentplanner.data.AppDatabase.getDatabase(this).plannerDao()
            val repository = com.example.assignmentplanner.data.PlannerRepository(dao)
            val viewModel = androidx.lifecycle.ViewModelProvider(this, com.example.assignmentplanner.viewmodel.PlannerViewModelFactory(repository))[com.example.assignmentplanner.viewmodel.PlannerViewModel::class.java]

            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                for (i in 0 until llModulesContainer.childCount) {
                    val row = llModulesContainer.getChildAt(i)
                    val code = row.findViewById<EditText>(R.id.et_module_code)?.text.toString().trim()
                    val name = row.findViewById<EditText>(R.id.et_module_name)?.text.toString().trim()
                    if (code.isNotEmpty() && name.isNotEmpty()) {
                        repository.insertModule(com.example.assignmentplanner.data.ModuleEntity(code = code, name = name))
                        moduleCodes.add(code)
                    }
                }

                val assignmentTitle = findViewById<EditText>(R.id.et_assignment_title)?.text.toString().trim()
                val assignedModule = findViewById<android.widget.AutoCompleteTextView>(R.id.actv_assigned_module)?.text.toString().trim()
                val dueDateTime = findViewById<EditText>(R.id.et_due_date_time)?.text.toString().trim()
                val priority = actvPriority?.text.toString().trim().takeIf { it.isNotEmpty() } ?: "Medium"

                if (assignmentTitle.isNotEmpty()) {
                    val safeModuleCode = if (moduleCodes.contains(assignedModule)) assignedModule else (moduleCodes.firstOrNull() ?: "")
                    val epoch = if (dueDateTime.isNotBlank()) {
                        try {
                            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(dueDateTime)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            try {
                                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).parse(dueDateTime)?.time ?: System.currentTimeMillis()
                            } catch (e2: Exception) { System.currentTimeMillis() }
                        }
                    } else System.currentTimeMillis()

                    val assignment = com.example.assignmentplanner.data.AssignmentEntity(
                        title = assignmentTitle,
                        moduleId = 0L,
                        moduleCode = safeModuleCode,
                        dueDate = dueDateTime,
                        dueTime = "",
                        priority = priority,
                        status = "ACTIVE",
                        dueDateEpoch = epoch
                    )
                    viewModel.saveAssignment(assignment, emptyList()) {}
                }

                launch(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(this@WelcomeSetupActivity, "Academic baseline saved!", android.widget.Toast.LENGTH_SHORT).show()
                    prefs.edit().putBoolean("is_onboarded", true).apply()
                    startActivity(Intent(this@WelcomeSetupActivity, DashboardActivity::class.java))
                    finish()
                }
            }
        }

        findViewById<TextView>(R.id.tv_skip).setOnClickListener {
            val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
            prefs.edit().putBoolean("is_onboarded", true).apply()

            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}
