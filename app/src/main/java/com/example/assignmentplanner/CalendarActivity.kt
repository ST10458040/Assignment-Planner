package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmentplanner.data.AppDatabase
import com.example.assignmentplanner.data.PlannerRepository
import com.example.assignmentplanner.viewmodel.PlannerViewModel
import com.example.assignmentplanner.viewmodel.PlannerViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarActivity : BaseActivity() {
    private lateinit var viewModel: PlannerViewModel
    private lateinit var adapter: AssignmentAdapter
    private val selectedDateFlow = MutableStateFlow("")
    private var selectedDateMillis: Long = -1L

    private val displayedCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dao = AppDatabase.getDatabase(this).plannerDao()
        val repository = PlannerRepository(dao)
        viewModel = ViewModelProvider(this, PlannerViewModelFactory(repository))[PlannerViewModel::class.java]

        adapter = AssignmentAdapter()
        val rvCalendarAssignments = findViewById<RecyclerView>(R.id.rv_calendar_assignments)
        rvCalendarAssignments.layoutManager = LinearLayoutManager(this)
        rvCalendarAssignments.adapter = adapter

        val tvSelectedDate = findViewById<TextView>(R.id.tv_selected_date)
        val calendarView = findViewById<CalendarView>(R.id.cv_calendar)
        val btnAddEvent = findViewById<Button>(R.id.btn_add_event)
        
        val tvMonthYear = findViewById<TextView>(R.id.tv_month_year)
        val btnPrevMonth = findViewById<View>(R.id.btn_prev_month)
        val btnNextMonth = findViewById<View>(R.id.btn_next_month)
        val btnToday = findViewById<View>(R.id.btn_today)
        val cvClusterAlert = findViewById<View>(R.id.cv_cluster_alert)
        val tvClusterAlertText = findViewById<TextView>(R.id.tv_cluster_alert_text)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        
        fun updateMonthHeader() {
            tvMonthYear.text = monthYearFormat.format(displayedCalendar.time)
            calendarView.date = displayedCalendar.timeInMillis
        }

        updateMonthHeader()

        btnPrevMonth.setOnClickListener {
            displayedCalendar.add(Calendar.MONTH, -1)
            updateMonthHeader()
        }

        btnNextMonth.setOnClickListener {
            displayedCalendar.add(Calendar.MONTH, 1)
            updateMonthHeader()
        }

        btnToday.setOnClickListener {
            displayedCalendar.time = Date()
            updateMonthHeader()
        }

        val today = Date()
        selectedDateMillis = today.time
        val initialDateString = dateFormat.format(today)
        selectedDateFlow.value = initialDateString
        tvSelectedDate.text = displayFormat.format(today)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDate = calendar.time
            selectedDateMillis = selectedDate.time
            val dateStr = dateFormat.format(selectedDate)
            selectedDateFlow.value = dateStr
            tvSelectedDate.text = displayFormat.format(selectedDate)
        }

        setupTopBarProfile(findViewById(R.id.tv_profile_initials) ?: findViewById(R.id.tv_header_title))
        
        lifecycleScope.launch {
            // Note: Standard android.widget.CalendarView does not natively support rendering dot indicators.
            // In a real app with MaterialCalendarView, we would collect repository.allAssignments here 
            // and add decorators for dates that have pending assignments.
            combine(viewModel.filteredAssignments, selectedDateFlow) { assignments, date ->
                assignments.filter { it.dueDate == date }
            }.collectLatest { filtered ->
                adapter.submitList(filtered)
            }
        }

        lifecycleScope.launch {
            viewModel.filteredAssignments.collectLatest { assignments ->
                val pending = assignments.filter { it.status != "COMPLETED" }
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                
                var maxClusterCount = 0
                var clusterDate = ""
                
                for (a in pending) {
                    try {
                        val d1 = sdf.parse(a.dueDate) ?: continue
                        var clusterCount = 1
                        for (b in pending) {
                            if (a.id == b.id) continue
                            val d2 = sdf.parse(b.dueDate) ?: continue
                            val diff = Math.abs(d1.time - d2.time)
                            if (diff <= 48 * 60 * 60 * 1000L) {
                                clusterCount++
                            }
                        }
                        if (clusterCount >= 3 && clusterCount > maxClusterCount) {
                            maxClusterCount = clusterCount
                            clusterDate = SimpleDateFormat("d MMM", Locale.getDefault()).format(d1)
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
                
                if (maxClusterCount >= 3) {
                    cvClusterAlert.visibility = View.VISIBLE
                    tvClusterAlertText.text = "$maxClusterCount assignments due within 48 hours of $clusterDate. Plan your revision early."
                } else {
                    cvClusterAlert.visibility = View.GONE
                }
            }
        }

        btnAddEvent.setOnClickListener {
            val intent = Intent(this, AddAssignmentActivity::class.java).apply {
                putExtra("EXTRA_SELECTED_DATE_EPOCH", selectedDateMillis)
                putExtra("PREFILLED_DATE", selectedDateMillis)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.fl_profile_avatar)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        setupCustomBottomNavigation(R.id.nav_calendar)
    }


    override fun onResume() {
        super.onResume()
        setupCustomBottomNavigation(R.id.nav_calendar)
        val tvInitials = findViewById<TextView>(R.id.tv_profile_initials)
        updateTopBarInitials(tvInitials)
    }
}
