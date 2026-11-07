package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignmentplanner.data.AppDatabase
import com.example.assignmentplanner.data.AssignmentEntity
import com.example.assignmentplanner.data.ModuleEntity
import com.example.assignmentplanner.data.PlannerRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModulesActivity : BaseActivity() {

    private lateinit var plannerRepository: PlannerRepository
    private var allModulesList = listOf<ModuleEntity>()
    private var allAssignmentsList = listOf<AssignmentEntity>()
    private lateinit var rvModules: RecyclerView
    private lateinit var etSearchModules: EditText
    private lateinit var modulesAdapter: ModulesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_modules)

        plannerRepository = PlannerRepository(AppDatabase.getDatabase(this).plannerDao())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ivBack = findViewById<ImageView>(R.id.btn_back)
        ivBack?.setOnClickListener {
            finish()
        }

        val ivProfile = findViewById<View>(R.id.fl_profile_avatar) ?: findViewById<View>(R.id.tv_profile_initials)
        ivProfile?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        val btnAddModule = findViewById<MaterialButton>(R.id.btn_add_module)
        btnAddModule?.setOnClickListener {
            showAddModuleDialog()
        }

        rvModules = findViewById(R.id.rv_module_list)
        rvModules.layoutManager = LinearLayoutManager(this)
        modulesAdapter = ModulesAdapter()
        rvModules.adapter = modulesAdapter

        etSearchModules = findViewById(R.id.moduleSearchInput)
        etSearchModules.addTextChangedListener { text ->
            val query = text.toString().lowercase()
            val filtered = allModulesList.filter {
                it.code.lowercase().contains(query) || it.name.lowercase().contains(query) || it.lecturer.lowercase().contains(query)
            }
            modulesAdapter.submitList(filtered)
        }

        if (intent.getBooleanExtra("OPEN_ADD_MODULE_DIALOG", false) || intent.getBooleanExtra("EXTRA_OPEN_ADD_MODULE", false) || intent.getBooleanExtra("OPEN_ADD_DIALOG", false)) {
            showAddModuleDialog()
        }

        setupCustomBottomNavigation(-1)
        loadModules()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent?.getBooleanExtra("EXTRA_OPEN_ADD_MODULE", false) == true || intent?.getBooleanExtra("OPEN_ADD_MODULE_DIALOG", false) == true) {
            showAddModuleDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        val tvInitials = findViewById<TextView>(R.id.tv_profile_initials)
        updateTopBarInitials(tvInitials)
        loadModules()
    }

    private fun loadModules() {
        lifecycleScope.launch {
            plannerRepository.allModules.collectLatest { modules ->
                allModulesList = modules
                val query = etSearchModules.text.toString().lowercase()
                val filtered = allModulesList.filter {
                    it.code.lowercase().contains(query) || it.name.lowercase().contains(query) || it.lecturer.lowercase().contains(query)
                }
                modulesAdapter.submitList(filtered)
                findViewById<TextView>(R.id.tvActiveModulesCount)?.text = modules.size.toString()
            }
        }

        lifecycleScope.launch {
            plannerRepository.allAssignments.collectLatest { assignments ->
                allAssignmentsList = assignments
                val dueCount = assignments.count { it.status != "COMPLETED" }
                findViewById<TextView>(R.id.tvDueTasksCount)?.text = dueCount.toString()
                modulesAdapter.notifyDataSetChanged()
            }
        }

        val userLevel = AppPreferences.getPrefs(this).getString("user_nqf", "NQF 7 • Year 3")
        findViewById<TextView>(R.id.tvYearLevel)?.text = userLevel
    }

    private fun showAddModuleDialog() {
        val dialogView = layoutInflater.inflate(R.layout.item_module_input, null)

        val etModuleCode = dialogView.findViewById<EditText>(R.id.et_module_code)
        val etModuleName = dialogView.findViewById<EditText>(R.id.et_module_name)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Module")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val code = etModuleCode?.text?.toString()?.trim().orEmpty()
            val name = etModuleName?.text?.toString()?.trim().orEmpty()

            if (code.isEmpty()) {
                etModuleCode?.error = "Enter module code (e.g. DATA6222)"
                return@setOnClickListener
            }
            if (name.isEmpty()) {
                etModuleName?.error = "Enter module name"
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val newModule = ModuleEntity(
                    code = code,
                    name = name
                )
                plannerRepository.insertModule(newModule)

                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    loadModules()
                }
            }
        }
    }

    inner class ModulesAdapter : RecyclerView.Adapter<ModulesAdapter.ModuleViewHolder>() {
        private var items = listOf<ModuleEntity>()

        fun submitList(list: List<ModuleEntity>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_module, parent, false)
            return ModuleViewHolder(view)
        }

        override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
            val module = items[position]
            holder.tvCodeBadge.text = module.code
            holder.tvTitle.text = module.name
            holder.tvSubtitle.text = "${module.lecturer} • NQF Level ${module.nqfLevel}"

            val moduleAssignments = allAssignmentsList.filter {
                (it.moduleId > 0L && it.moduleId == module.id) || it.moduleCode.equals(module.code, ignoreCase = true)
            }
            val activeCount = moduleAssignments.count { it.status != "COMPLETED" }
            val totalCount = moduleAssignments.size
            val completedCount = moduleAssignments.count { it.status == "COMPLETED" }
            val progress = if (totalCount > 0) (completedCount * 100) / totalCount else 0

            holder.tvActiveCount.text = "$activeCount Active Tasks"
            holder.pbProgress.progress = progress

            holder.cardModule.setOnClickListener {
                val intent = Intent(this@ModulesActivity, ModuleDetailsActivity::class.java).apply {
                    putExtra("MODULE_ID", module.id)
                    putExtra("moduleCode", module.code)
                }
                startActivity(intent)
            }
        }

        override fun getItemCount() = items.size

        inner class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cardModule: MaterialCardView = itemView.findViewById(R.id.card_module)
            val tvCodeBadge: TextView = itemView.findViewById(R.id.tv_module_code_badge)
            val tvActiveCount: TextView = itemView.findViewById(R.id.tv_active_task_count)
            val tvTitle: TextView = itemView.findViewById(R.id.tv_module_title)
            val tvSubtitle: TextView = itemView.findViewById(R.id.tv_module_subtitle)
            val pbProgress: ProgressBar = itemView.findViewById(R.id.pb_module_progress)
        }
    }
}
