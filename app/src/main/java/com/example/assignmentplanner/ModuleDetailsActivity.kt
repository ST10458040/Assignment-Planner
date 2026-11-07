package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmentplanner.data.AppDatabase
import com.example.assignmentplanner.data.ModuleEntity
import com.example.assignmentplanner.data.PlannerRepository
import com.example.assignmentplanner.viewmodel.PlannerViewModel
import com.example.assignmentplanner.viewmodel.PlannerViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModuleDetailsActivity : BaseActivity() {

    private lateinit var viewModel: PlannerViewModel
    private lateinit var repository: PlannerRepository
    private var currentModule: ModuleEntity? = null
    private var moduleCode: String = "DATA6222"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_module_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dao = AppDatabase.getDatabase(this).plannerDao()
        repository = PlannerRepository(dao)
        viewModel = ViewModelProvider(this, PlannerViewModelFactory(repository))[PlannerViewModel::class.java]

        val tvScreenTitle = findViewById<TextView>(R.id.tvScreenTitle)
        tvScreenTitle?.text = "Module Details"

        val tvAvatar = findViewById<TextView>(R.id.tvTopBarAvatarInitials)
        tvAvatar?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        moduleCode = intent.getStringExtra("moduleCode") ?: intent.getStringExtra("MODULE_CODE") ?: "DATA6222"

        // Load Module from Database
        lifecycleScope.launch(Dispatchers.IO) {
            var mod = dao.getModuleByCode(moduleCode)
            if (mod == null) {
                val defaultName = when (moduleCode) {
                    "WEDE6021" -> "Web Development"
                    "PROG6212" -> "Programming 2B"
                    else -> "Database Design & Implementation"
                }
                val defaultLecturer = when (moduleCode) {
                    "WEDE6021" -> "Prof. S. Khumalo"
                    "PROG6212" -> "Dr. J. Smith"
                    else -> "Dr. K. Naidoo"
                }
                val newMod = ModuleEntity(
                    code = moduleCode,
                    name = defaultName,
                    lecturer = defaultLecturer,
                    termSemester = "Year 3 • Semester 2",
                    nqfLevel = "6"
                )
                val id = dao.insertModule(newMod)
                mod = newMod.copy(id = id)
            }
            currentModule = mod

            withContext(Dispatchers.Main) {
                updateHeaderUI(mod)
            }
        }

        // Edit Module Button
        findViewById<ImageView>(R.id.btn_edit_module)?.setOnClickListener {
            showEditModuleDialog()
        }

        // Setup Assignments List and Progress Bar
        val rvAssignments = findViewById<RecyclerView>(R.id.rv_module_assignments)
        val adapter = AssignmentAdapter()
        rvAssignments.layoutManager = LinearLayoutManager(this)
        rvAssignments.adapter = adapter

        lifecycleScope.launch {
            viewModel.getAssignmentsForModule(moduleCode).collectLatest { assignments ->
                adapter.submitList(assignments)
                val total = assignments.size
                val completed = assignments.count { it.status == "COMPLETED" }
                val percentage = if (total > 0) (completed * 100) / total else 0

                findViewById<ProgressBar>(R.id.pb_module_progress)?.progress = percentage
                findViewById<TextView>(R.id.tv_progress_text)?.text = "$completed/$total Tasks ($percentage%)"
            }
        }

        findViewById<Button>(R.id.btn_add_assignment)?.setOnClickListener {
            val addIntent = Intent(this, AddAssignmentActivity::class.java).apply {
                putExtra("moduleCode", moduleCode)
            }
            startActivity(addIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        val tvScreenTitle = findViewById<TextView>(R.id.tvScreenTitle)
        tvScreenTitle?.text = "Module Details"

        val tvAvatar = findViewById<TextView>(R.id.tvTopBarAvatarInitials)
        if (tvAvatar != null) {
            tvAvatar.text = AppPreferences.getUserInitials(this)
        }
    }

    private fun updateHeaderUI(mod: ModuleEntity) {
        findViewById<TextView>(R.id.tv_module_code)?.text = mod.code
        findViewById<TextView>(R.id.tv_module_name)?.text = mod.name
        findViewById<TextView>(R.id.tv_lecturer)?.text = "Lecturer: ${mod.lecturer}"
        findViewById<TextView>(R.id.tv_term)?.text = "Term/Semester: ${mod.termSemester}"
        findViewById<TextView>(R.id.tv_nqf)?.text = "NQF Level: ${mod.nqfLevel}"
    }

    private fun showEditModuleDialog() {
        val mod = currentModule ?: return

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val etEditModuleName = EditText(this).apply {
            hint = "Module Name"
            setText(mod.name)
        }
        val etEditLecturer = EditText(this).apply {
            hint = "Lecturer Name"
            setText(mod.lecturer)
        }
        val etEditTerm = EditText(this).apply {
            hint = "Term / Semester"
            setText(mod.termSemester)
        }
        val etEditNqf = EditText(this).apply {
            hint = "NQF Level"
            setText(mod.nqfLevel)
        }

        layout.addView(etEditModuleName)
        layout.addView(etEditLecturer)
        layout.addView(etEditTerm)
        layout.addView(etEditNqf)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Module Details (${mod.code})")
            .setView(layout)
            .setPositiveButton("Save Changes", null)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val newName = etEditModuleName.text.toString().trim()
            val newLecturer = etEditLecturer.text.toString().trim()
            val newTerm = etEditTerm.text.toString().trim()
            val newNqf = etEditNqf.text.toString().trim()

            if (newName.isBlank()) {
                etEditModuleName.error = "Module name required"
                return@setOnClickListener
            }

            val updatedModule = mod.copy(
                name = newName,
                lecturer = newLecturer.ifBlank { "Dr. K. Naidoo" },
                termSemester = newTerm.ifBlank { "Year 3 • Semester 2" },
                nqfLevel = newNqf.ifBlank { "6" }
            )

            lifecycleScope.launch(Dispatchers.IO) {
                repository.insertModule(updatedModule)
                currentModule = updatedModule

                withContext(Dispatchers.Main) {
                    updateHeaderUI(updatedModule)
                    Toast.makeText(this@ModuleDetailsActivity, "Module updated successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }
}
