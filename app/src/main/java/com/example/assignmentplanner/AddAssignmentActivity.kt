package com.example.assignmentplanner

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmentplanner.data.AppDatabase
import com.example.assignmentplanner.data.AssignmentEntity
import com.example.assignmentplanner.data.ModuleEntity
import com.example.assignmentplanner.data.NotificationEntity
import com.example.assignmentplanner.data.PlannerRepository
import com.example.assignmentplanner.data.SubtaskEntity
import com.example.assignmentplanner.receiver.AlarmReceiver
import com.example.assignmentplanner.viewmodel.PlannerViewModel
import com.example.assignmentplanner.viewmodel.PlannerViewModelFactory
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddAssignmentActivity : BaseActivity() {

    private lateinit var viewModel: PlannerViewModel
    private lateinit var repository: PlannerRepository
    private val subtasks = mutableListOf<SubtaskEntity>()
    private lateinit var subtaskAdapter: SubtaskAdapter
    private var assignmentId: Long = 0L
    private var modulesList = listOf<ModuleEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_assignment)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = PlannerRepository(AppDatabase.getDatabase(this).plannerDao())
        viewModel = ViewModelProvider(this, PlannerViewModelFactory(repository))[PlannerViewModel::class.java]

        createNotificationChannel()
        requestNotificationPermission()

        assignmentId = intent.getLongExtra("assignmentId", 0L)
        val moduleCodeFromIntent = intent.getStringExtra("moduleCode")

        val actvModule = findViewById<AutoCompleteTextView>(R.id.actv_module)
        val etTitle = findViewById<EditText>(R.id.et_title)
        val etDesc = findViewById<EditText>(R.id.et_description)
        val etDate = findViewById<EditText>(R.id.et_date)
        val etTime = findViewById<EditText>(R.id.et_time)
        val rgPriority = findViewById<RadioGroup>(R.id.rg_priority)

        // Handle PREFILLED_DATE / EXTRA_SELECTED_DATE_EPOCH from Calendar
        val prefilledDateMillis = intent.getLongExtra("EXTRA_SELECTED_DATE_EPOCH", intent.getLongExtra("PREFILLED_DATE", -1L))
        if (prefilledDateMillis > 0L) {
            val c = Calendar.getInstance()
            c.timeInMillis = prefilledDateMillis
            val day = c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
            val month = (c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            val year = c.get(Calendar.YEAR)
            val dateStr = "$day/$month/$year"
            etDate.setText(dateStr)
        }

        // Date and Time Pickers
        etDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val dateStr = "${dayOfMonth.toString().padStart(2, '0')}/${(month + 1).toString().padStart(2, '0')}/$year"
                etDate.setText(dateStr)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        etTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val timeStr = "${hourOfDay.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                etTime.setText(timeStr)
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        // Assessment Format Selection
        var selectedFormat = "Task"
        val cgTypes = findViewById<com.google.android.material.chip.ChipGroup>(R.id.cg_types)
        cgTypes.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = findViewById<com.google.android.material.chip.Chip>(checkedIds.first())
                selectedFormat = chip.text.toString()
            }
        }

        // Priority Selection
        var selectedPriority = "Medium"
        val rbLow = findViewById<RadioButton>(R.id.rb_low)
        val rbMedium = findViewById<RadioButton>(R.id.rb_medium)
        val rbHigh = findViewById<RadioButton>(R.id.rb_high)

        val unselectedColor = getColor(R.color.surface_container)
        val selectedLowColor = getColor(R.color.tertiary_green)
        val selectedMediumColor = getColor(R.color.accent_violet)
        val selectedHighColor = getColor(R.color.destructive_red)

        rgPriority.setOnCheckedChangeListener { _, checkedId ->
            rbLow.setBackgroundColor(unselectedColor)
            rbMedium.setBackgroundColor(unselectedColor)
            rbHigh.setBackgroundColor(unselectedColor)

            when (checkedId) {
                R.id.rb_low -> {
                    selectedPriority = "Low"
                    rbLow.setBackgroundColor(selectedLowColor)
                }
                R.id.rb_medium -> {
                    selectedPriority = "Medium"
                    rbMedium.setBackgroundColor(selectedMediumColor)
                }
                R.id.rb_high -> {
                    selectedPriority = "High"
                    rbHigh.setBackgroundColor(selectedHighColor)
                }
            }
        }

        // Initial setup for medium priority
        rgPriority.check(R.id.rb_medium)

        // Notification Timing Spinner setup
        val actvNotificationTime = findViewById<AutoCompleteTextView>(R.id.actv_notification_time)
        val times = arrayOf("At time of event", "1 hour before", "1 day before", "1 week before")
        val timesAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, times)
        actvNotificationTime?.setAdapter(timesAdapter)

        // Setup Modules Dropdown
        lifecycleScope.launch {
            viewModel.allModules.collectLatest { modules ->
                modulesList = modules
                val adapter = ArrayAdapter(this@AddAssignmentActivity, android.R.layout.simple_dropdown_item_1line, modules.map { it.code })
                actvModule.setAdapter(adapter)
                if (moduleCodeFromIntent != null && assignmentId == 0L) {
                    actvModule.setText(moduleCodeFromIntent, false)
                }
            }
        }

        // Setup Subtasks Adapter
        subtaskAdapter = SubtaskAdapter { subtask, isChecked ->
            val index = subtasks.indexOfFirst { it.title == subtask.title && it.id == subtask.id }
            if (index != -1) {
                subtasks[index] = subtask.copy(isCompleted = isChecked)
                subtaskAdapter.submitList(subtasks.toList())
            }
        }
        val rvSubtasks = findViewById<RecyclerView>(R.id.rv_subtasks)
        rvSubtasks.layoutManager = LinearLayoutManager(this)
        rvSubtasks.adapter = subtaskAdapter
        subtaskAdapter.submitList(subtasks.toList())

        // Add Subtask
        findViewById<View>(R.id.btn_add_subtask).setOnClickListener {
            val etNewSubtask = findViewById<EditText>(R.id.et_new_subtask)
            val title = etNewSubtask.text.toString().trim()
            if (title.isNotEmpty()) {
                subtasks.add(SubtaskEntity(assignmentId = assignmentId, title = title))
                subtaskAdapter.submitList(subtasks.toList())
                etNewSubtask.text.clear()
            }
        }

        // Gamification Streak Badge
        val streakBadge = findViewById<View>(R.id.streak_badge)
        if (AppPreferences.isGamificationEnabled(this)) {
            streakBadge?.visibility = View.VISIBLE
            val actualStreak = 5
            val tvStreak = (streakBadge as? android.view.ViewGroup)?.getChildAt(0) as? TextView
            tvStreak?.text = "🔥 ${actualStreak}d"
        } else {
            streakBadge?.visibility = View.GONE
        }

        // Autofill if Edit Mode
        if (assignmentId > 0) {
            findViewById<TextView>(R.id.tv_add_title).text = "Edit Assignment"
            lifecycleScope.launch {
                val assignment = viewModel.getAssignmentById(assignmentId)
                if (assignment != null) {
                    etTitle.setText(assignment.title)
                    actvModule.setText(assignment.moduleCode, false)
                    etDesc.setText(assignment.description)
                    etDate.setText(assignment.dueDate)
                    etTime.setText(assignment.dueTime)
                    when (assignment.priority) {
                        "Low" -> rgPriority.check(R.id.rb_low)
                        "Medium" -> rgPriority.check(R.id.rb_medium)
                        "High" -> rgPriority.check(R.id.rb_high)
                    }
                }

                val currentSubtasks = viewModel.getSubtasksForAssignment(assignmentId).firstOrNull()
                if (currentSubtasks != null) {
                    subtasks.clear()
                    subtasks.addAll(currentSubtasks)
                    subtaskAdapter.submitList(subtasks.toList())
                }
            }
        }

        // Actions
        findViewById<TextView>(R.id.btn_cancel).setOnClickListener { finish() }

        val saveAction = { status: String ->
            val title = etTitle.text.toString().trim()
            val moduleCode = actvModule.text.toString().trim()
            val dateStr = etDate.text.toString().trim()
            val timeStr = etTime.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Required"
            } else if (dateStr.isEmpty()) {
                etDate.error = "Required"
            } else {
                val selectedPriority = when (rgPriority.checkedRadioButtonId) {
                    R.id.rb_low -> "Low"
                    R.id.rb_medium -> "Medium"
                    R.id.rb_high -> "High"
                    else -> "Medium"
                }

                val notificationsEnabled = findViewById<MaterialSwitch>(R.id.switch_notifications)?.isChecked ?: false
                val notificationTime = findViewById<AutoCompleteTextView>(R.id.actv_notification_time)?.text?.toString() ?: "1 day before"

                val epoch = parseDateTimeToEpoch(dateStr, timeStr)
                val finalEpoch = if (epoch > 0L) epoch else System.currentTimeMillis()

                val selectedModule = modulesList.find { it.code.equals(moduleCode, ignoreCase = true) }
                val resolvedModuleId = selectedModule?.id ?: 0L

                val newAssignment = AssignmentEntity(
                    id = assignmentId,
                    title = title,
                    moduleId = resolvedModuleId,
                    moduleCode = moduleCode,
                    dueDate = dateStr,
                    dueTime = timeStr,
                    priority = selectedPriority,
                    status = status,
                    description = etDesc.text.toString(),
                    isNotificationEnabled = notificationsEnabled,
                    dueDateEpoch = finalEpoch
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    val savedId = repository.insertAssignment(newAssignment)
                    if (subtasks.isNotEmpty()) {
                        subtasks.forEach {
                            repository.insertSubtask(it.copy(assignmentId = savedId))
                        }
                    }

                    val notif = NotificationEntity(
                        title = "New Assignment Added",
                        message = "$title has been scheduled for $dateStr.",
                        timestampEpoch = System.currentTimeMillis(),
                        isRead = false,
                        type = "ASSIGNMENT",
                        relatedAssignmentId = savedId
                    )
                    repository.insertNotification(notif)

                    withContext(Dispatchers.Main) {
                        AppPreferences.setHasUnreadNotifications(this@AddAssignmentActivity, true)
                        sendAssignmentNotification(savedId, title, dateStr, timeStr)

                        if (notificationsEnabled && status != "DRAFT") {
                            scheduleAlarm(savedId, title, dateStr, notificationTime)
                        }

                        Toast.makeText(this@AddAssignmentActivity, "Assignment saved successfully", Toast.LENGTH_SHORT).show()
                        finish() // Closes AddAssignmentActivity and triggers onResume() in DashboardActivity
                    }
                }
            }
        }

        findViewById<TextView>(R.id.btn_save).setOnClickListener { saveAction("ACTIVE") }
        findViewById<Button>(R.id.btn_create_assignment).setOnClickListener { saveAction("ACTIVE") }
        findViewById<Button>(R.id.btn_save_draft).setOnClickListener { saveAction("DRAFT") }
    }

    private fun parseDateTimeToEpoch(dateStr: String, timeStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()
        val cleanDate = dateStr.trim()
        val cleanTime = if (timeStr.isNotBlank()) timeStr.trim() else "23:59:59"
        val formats = arrayOf("dd/MM/yyyy", "yyyy-MM-dd", "d/M/yyyy", "dd-MM-yyyy")
        for (dateFormat in formats) {
            try {
                val sdf = SimpleDateFormat("$dateFormat HH:mm:ss", Locale.getDefault())
                val parsed = sdf.parse("$cleanDate $cleanTime")
                if (parsed != null && parsed.time > 0L) return parsed.time
            } catch (e: Exception) { }
            try {
                val sdf = SimpleDateFormat("$dateFormat HH:mm", Locale.getDefault())
                val parsed = sdf.parse("$cleanDate $cleanTime")
                if (parsed != null && parsed.time > 0L) return parsed.time
            } catch (e: Exception) { }
            try {
                val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
                val parsed = sdf.parse(cleanDate)
                if (parsed != null) {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = parsed.time
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                    }
                    if (cal.timeInMillis > 0L) return cal.timeInMillis
                }
            } catch (e: Exception) { }
        }
        return System.currentTimeMillis()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Deadline Reminders"
            val descriptionText = "Notifications for assignment deadlines and drafts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("deadline_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendAssignmentNotification(id: Long, title: String, date: String, time: String) {
        val intent = Intent(this, AssignmentDetailsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ASSIGNMENT_ID", id)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "deadline_reminders")
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Assignment Created")
            .setContentText("$title scheduled for $date at $time")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(id.toInt(), builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    private fun scheduleAlarm(id: Long, title: String, date: String, notificationTime: String) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        try {
            val triggerTime = System.currentTimeMillis() + 10000
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            Toast.makeText(this, "Notification scheduled!", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            // Requires exact alarm permission on newer Android versions
        }
    }
}
