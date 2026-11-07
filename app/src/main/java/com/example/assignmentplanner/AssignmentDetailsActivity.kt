package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmentplanner.data.AppDatabase
import com.example.assignmentplanner.data.PlannerRepository
import com.example.assignmentplanner.data.SubtaskEntity
import com.example.assignmentplanner.viewmodel.PlannerViewModel
import com.example.assignmentplanner.viewmodel.PlannerViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AssignmentDetailsActivity : BaseActivity() {

    private lateinit var viewModel: PlannerViewModel
    private var assignmentId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assignment_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        assignmentId = intent.getLongExtra("ASSIGNMENT_ID", intent.getLongExtra("assignmentId", -1L))
        if (assignmentId <= 0L) {
            val stringId = intent.getStringExtra("ASSIGNMENT_ID") ?: intent.getStringExtra("assignmentId")
            assignmentId = stringId?.toLongOrNull() ?: -1L
        }

        if (assignmentId <= 0L) {
            Toast.makeText(this, "Invalid assignment ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val repository = PlannerRepository(AppDatabase.getDatabase(this).plannerDao())
        viewModel = ViewModelProvider(this, PlannerViewModelFactory(repository))[PlannerViewModel::class.java]

        val tvTitle = findViewById<TextView>(R.id.tv_detail_title)
        val tvModuleCode = findViewById<TextView>(R.id.tv_detail_module_code)
        val tvDesc = findViewById<TextView>(R.id.tv_detail_desc)
        val tvCountdown = findViewById<TextView>(R.id.tv_detail_countdown)
        val tvDueDate = findViewById<TextView>(R.id.tv_detail_due_date)
        val tvProgressPercent = findViewById<TextView>(R.id.tv_detail_progress_percent)
        val pbProgress = findViewById<ProgressBar>(R.id.pb_assignment_progress)
        
        lifecycleScope.launch {
            viewModel.getAssignmentByIdFlow(assignmentId).collectLatest { assignment ->
                if (assignment != null) {
                    tvTitle.text = assignment.title
                    tvModuleCode.text = assignment.moduleCode
                    tvDesc.text = assignment.description.ifEmpty { "No description provided." }
                    
                    tvDueDate.text = "Due ${assignment.dueDate} at ${assignment.dueTime}"
                    
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = sdf.parse(assignment.dueDate)
                        if (date != null) {
                            val diff = date.time - System.currentTimeMillis()
                            val days = (diff / (1000 * 60 * 60 * 24)).toInt()
                            if (days < 0) {
                                tvCountdown.text = "OVERDUE"
                            } else if (days == 0) {
                                tvCountdown.text = "DUE TODAY"
                            } else {
                                tvCountdown.text = "⏰ $days DAYS LEFT"
                            }
                        }
                    } catch (e: Exception) {
                        tvCountdown.text = ""
                    }
                }
            }
        }

        val subtaskAdapter = SubtaskAdapter { subtask, isChecked ->
            viewModel.updateSubtask(subtask.copy(isCompleted = isChecked))
        }

        val rvSubtasks = findViewById<RecyclerView>(R.id.rv_subtasks)
        rvSubtasks.layoutManager = LinearLayoutManager(this)
        rvSubtasks.adapter = subtaskAdapter

        lifecycleScope.launch {
            viewModel.getSubtasksForAssignment(assignmentId).collectLatest { subtasks ->
                subtaskAdapter.submitList(subtasks)
                val total = subtasks.size
                val completed = subtasks.count { it.isCompleted }
                val progress = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0
                pbProgress.progress = progress
                tvProgressPercent.text = "$progress%"
            }
        }

        findViewById<Button>(R.id.btn_add_subtask).setOnClickListener {
            val etNewSubtask = findViewById<EditText>(R.id.et_new_subtask)
            val title = etNewSubtask.text.toString().trim()
            if (title.isNotEmpty()) {
                viewModel.insertSubtask(SubtaskEntity(assignmentId = assignmentId, title = title))
                etNewSubtask.text.clear()
            }
        }

        findViewById<Button>(R.id.btn_edit).setOnClickListener {
            val intent = Intent(this, AddAssignmentActivity::class.java).apply {
                putExtra("ASSIGNMENT_ID", assignmentId)
                putExtra("assignmentId", assignmentId)
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_mark_complete).setOnClickListener {
            lifecycleScope.launch {
                val assignment = viewModel.getAssignmentById(assignmentId)
                if (assignment != null) {
                    viewModel.saveAssignment(assignment.copy(status = "COMPLETED"), emptyList()) {
                        Toast.makeText(this@AssignmentDetailsActivity, "Marked as complete", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        findViewById<Button>(R.id.btn_delete)?.setOnClickListener {
            lifecycleScope.launch {
                val assignment = viewModel.getAssignmentById(assignmentId)
                if (assignment != null) {
                    viewModel.deleteAssignment(assignment)
                    Toast.makeText(this@AssignmentDetailsActivity, "Deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
