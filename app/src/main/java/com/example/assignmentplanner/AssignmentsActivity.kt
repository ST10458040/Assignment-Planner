package com.example.assignmentplanner

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.SearchView
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class AssignmentsActivity : BaseActivity() {
    private lateinit var viewModel: PlannerViewModel
    private lateinit var adapter: AssignmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assignments)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupTopBarProfile(findViewById(R.id.tv_profile_initials) ?: findViewById(R.id.tv_header_title))
        
        val dao = AppDatabase.getDatabase(this).plannerDao()
        val repository = PlannerRepository(dao)
        viewModel = ViewModelProvider(this, PlannerViewModelFactory(repository))[PlannerViewModel::class.java]

        adapter = AssignmentAdapter()
        val rvAssignments = findViewById<RecyclerView>(R.id.rv_assignments)
        rvAssignments.layoutManager = LinearLayoutManager(this)
        rvAssignments.adapter = adapter

        lifecycleScope.launch {
            viewModel.filteredAssignments.collectLatest { assignments ->
                adapter.submitList(assignments)
            }
        }

        val searchView = findViewById<SearchView>(R.id.sv_assignments)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.updateSearchQuery(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchQuery(newText ?: "")
                return true
            }
        })

        val chipGroup = findViewById<ChipGroup>(R.id.cg_filters)
        val chipAll = findViewById<Chip>(R.id.chip_all)
        val chipPending = findViewById<Chip>(R.id.chip_pending)
        val chipOverdue = findViewById<Chip>(R.id.chip_overdue)
        
        val llStreak = findViewById<View>(R.id.ll_streak)
        val tvStreakText = findViewById<TextView>(R.id.tv_streak_text)

        lifecycleScope.launch {
            repository.allAssignments.collectLatest { assignments ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val today = Calendar.getInstance().time
                val c48 = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 48) }.time
                
                var allCount = assignments.size
                var pendingCount = 0
                var dueSoonCount = 0
                var completedCount = 0
                val streakDates = mutableSetOf<String>()

                for (a in assignments) {
                    if (a.status == "COMPLETED") {
                        completedCount++
                        streakDates.add(a.dueDate)
                    } else {
                        pendingCount++
                        try {
                            val date = sdf.parse(a.dueDate)
                            if (date != null && !date.before(today) && !date.after(c48)) {
                                dueSoonCount++
                            }
                        } catch (e: Exception) {}
                    }
                }

                chipAll.text = "All ($allCount)"
                chipPending.text = "Pending ($pendingCount)"
                chipOverdue.text = "Due Soon ($dueSoonCount)"

                var streak = 0
                val checkCal = Calendar.getInstance()
                while (true) {
                    val checkDateStr = sdf.format(checkCal.time)
                    if (streakDates.contains(checkDateStr)) {
                        streak++
                        checkCal.add(Calendar.DATE, -1)
                    } else {
                        if (streak == 0 && sdf.format(Calendar.getInstance().time) == checkDateStr) {
                            checkCal.add(Calendar.DATE, -1)
                            val checkYesterdayStr = sdf.format(checkCal.time)
                            if (streakDates.contains(checkYesterdayStr)) {
                                streak++
                                checkCal.add(Calendar.DATE, -1)
                                continue
                            }
                        }
                        break
                    }
                }

                if (streak > 0) {
                    llStreak.visibility = View.VISIBLE
                    tvStreakText?.text = "🔥 $streak Izinsuku / Days Streak"
                } else {
                    llStreak.visibility = View.GONE
                }
            }
        }

        fun updateChipColors(checkedId: Int) {
            val chips = listOf(chipAll, chipPending, chipOverdue)
            chips.forEach { chip ->
                if (chip.id == checkedId) {
                    chip.setChipBackgroundColorResource(R.color.accent_violet)
                    chip.setTextColor(Color.WHITE)
                } else {
                    chip.setChipBackgroundColorResource(R.color.surface_container_high)
                    chip.setTextColor(getColor(R.color.text_on_surface_variant))
                }
            }
        }
        
        updateChipColors(R.id.chip_all)

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds.first()
                updateChipColors(checkedId)
                val filter = when (checkedId) {
                    R.id.chip_pending -> "Pending"
                    R.id.chip_overdue -> "Due Soon"
                    else -> "All"
                }
                viewModel.updateFilter(filter)
            }
        }

        findViewById<View>(R.id.fab_add)?.setOnClickListener {
            startActivity(android.content.Intent(this, AddAssignmentActivity::class.java))
        }

        findViewById<View>(R.id.fl_profile_avatar)?.setOnClickListener {
            startActivity(android.content.Intent(this, ProfileActivity::class.java))
        }

        setupCustomBottomNavigation(R.id.nav_assignments)
    }

    override fun onResume() {
        super.onResume()
        setupCustomBottomNavigation(R.id.nav_assignments)
        val tvInitials = findViewById<TextView>(R.id.tv_profile_initials)
        updateTopBarInitials(tvInitials)
    }
}
