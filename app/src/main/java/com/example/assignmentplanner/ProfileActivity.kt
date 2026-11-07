package com.example.assignmentplanner

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.assignmentplanner.data.AppDatabase
import com.example.assignmentplanner.data.PlannerRepository
import com.example.assignmentplanner.data.UserEntity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileActivity : BaseActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvFaculty: TextView
    private lateinit var tvAvatar: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var plannerRepository: PlannerRepository

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            uploadImageToFirebase(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        plannerRepository = PlannerRepository(AppDatabase.getDatabase(this).plannerDao())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvScreenTitle = findViewById<TextView>(R.id.tvProfileScreenTitle)
        tvScreenTitle?.text = "Iphrofayili / Profile"

        tvName = findViewById(R.id.tv_profile_name) ?: findViewById(R.id.tvProfileScreenTitle)
        tvFaculty = findViewById(R.id.tv_profile_faculty) ?: findViewById(R.id.tvProfileScreenTitle)
        tvAvatar = findViewById(R.id.tvProfileCardInitials)
        ivAvatar = findViewById(R.id.iv_profile_avatar)
        
        loadProfile()
        loadEnrolledModules()

        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btn_edit_profile)?.setOnClickListener {
            showEditAcademicRecordDialog()
        }

        findViewById<ImageView>(R.id.btn_edit_academic_record)?.setOnClickListener {
            showEditAcademicRecordDialog()
        }

        findViewById<ImageView>(R.id.btn_change_avatar)?.setOnClickListener {
            showChangeAvatarDialog()
        }

        findViewById<View>(R.id.btn_change_password)?.setOnClickListener {
            Toast.makeText(this, "Password reset email sent to student address.", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btn_export_data)?.setOnClickListener {
            Toast.makeText(this, "Exporting timetable as JSON...", Toast.LENGTH_SHORT).show()
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "{ \"tasks\": [] }")
                type = "application/json"
            }
            startActivity(Intent.createChooser(sendIntent, "Export Timetable"))
        }

        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
            prefs.edit().clear().apply()
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupCustomBottomNavigation(-1)
    }

    override fun onResume() {
        super.onResume()
        val tvScreenTitle = findViewById<TextView>(R.id.tvProfileScreenTitle)
        tvScreenTitle?.text = "Iphrofayili / Profile"

        val isGamificationEnabled = AppPreferences.isGamificationEnabled(this)
        findViewById<View>(R.id.layoutGamification)?.visibility = if (isGamificationEnabled) View.VISIBLE else View.GONE
        loadProfile()
        loadEnrolledModules()
    }

    private fun loadEnrolledModules() {
        lifecycleScope.launch(Dispatchers.IO) {
            val modules = plannerRepository.allModules.firstOrNull() ?: AppDatabase.getDatabase(this@ProfileActivity).plannerDao().getAllModulesList()
            withContext(Dispatchers.Main) {
                val container = findViewById<ChipGroup>(R.id.chipGroupEnrolledModules)
                container?.removeAllViews()

                if (modules.isEmpty()) {
                    val emptyTv = TextView(this@ProfileActivity).apply {
                        text = "No modules enrolled yet"
                        setTextColor(Color.parseColor("#94A3B8"))
                        textSize = 13f
                    }
                    container?.addView(emptyTv)
                } else {
                    modules.forEach { module ->
                        val chip = Chip(this@ProfileActivity).apply {
                            text = if (module.name.isNotBlank()) "${module.code} • ${module.name}" else module.code
                            setChipBackgroundColorResource(android.R.color.transparent)
                            chipStrokeWidth = 2f
                            chipStrokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#3B82F6"))
                            setTextColor(Color.parseColor("#FFFFFF"))
                            textSize = 12f
                            isClickable = false
                            isCheckable = false
                        }
                        container?.addView(chip)
                    }
                }
            }
        }
    }

    private fun loadProfile() {
        val fullName = AppPreferences.getUserFullName(this)
        val studentNumber = AppPreferences.getStudentNumber(this)
        val faculty = AppPreferences.getFaculty(this)
        val degreeYear = AppPreferences.getDegreeYear(this)

        val nameView = findViewById<TextView>(R.id.tv_profile_name)
        val emailView = findViewById<TextView>(R.id.tv_profile_email)
        val instView = findViewById<TextView>(R.id.tv_profile_institution)
        val avatarView = findViewById<TextView>(R.id.tvProfileCardInitials)

        val studentNameView = findViewById<TextView>(R.id.tv_student_name)
        val studentNumView = findViewById<TextView>(R.id.tv_student_number)
        val facultyView = findViewById<TextView>(R.id.tv_profile_faculty)
        val degreeYearView = findViewById<TextView>(R.id.tv_degree_year)

        nameView?.text = fullName
        emailView?.text = studentNumber
        instView?.text = faculty

        studentNameView?.text = fullName
        studentNumView?.text = studentNumber
        facultyView?.text = faculty
        degreeYearView?.text = degreeYear

        avatarView?.text = AppPreferences.getUserInitials(this)

        // Load profile picture if URL exists
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val db = AppDatabase.getDatabase(this@ProfileActivity)
                    val userProfile = withContext(Dispatchers.IO) {
                        db.plannerDao().getUserProfile(uid)
                    }
                    if (userProfile != null && userProfile.profilePictureUrl.isNotEmpty()) {
                        ivAvatar.visibility = View.VISIBLE
                        tvAvatar.visibility = View.GONE
                        Glide.with(this@ProfileActivity)
                            .load(userProfile.profilePictureUrl)
                            .circleCrop()
                            .into(ivAvatar)
                    } else {
                        val doc = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
                        val url = doc.getString("profilePictureUrl")
                        if (url != null && url.isNotEmpty()) {
                            ivAvatar.visibility = View.VISIBLE
                            tvAvatar.visibility = View.GONE
                            Glide.with(this@ProfileActivity)
                                .load(url)
                                .circleCrop()
                                .into(ivAvatar)
                            
                            val nameParts = fullName.split(" ", limit = 2)
                            val fName = nameParts.getOrNull(0) ?: ""
                            val lName = nameParts.getOrNull(1) ?: ""
                            withContext(Dispatchers.IO) {
                                db.plannerDao().insertUser(UserEntity(uid, fName, lName, studentNumber, url))
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showEditAcademicRecordDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
        }

        val etFullName = EditText(this).apply {
            hint = "Full Name"
            setText(AppPreferences.getUserFullName(this@ProfileActivity))
        }
        val etStudentNumber = EditText(this).apply {
            hint = "Student Number"
            setText(AppPreferences.getStudentNumber(this@ProfileActivity))
        }
        val etFaculty = EditText(this).apply {
            hint = "Faculty / Institution"
            setText(AppPreferences.getFaculty(this@ProfileActivity))
        }
        val etDegreeYear = EditText(this).apply {
            hint = "Degree / Year / NQF Level"
            setText(AppPreferences.getDegreeYear(this@ProfileActivity))
        }

        layout.addView(etFullName)
        layout.addView(etStudentNumber)
        layout.addView(etFaculty)
        layout.addView(etDegreeYear)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Academic Record")
            .setView(layout)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = etFullName.text.toString().trim()
            val number = etStudentNumber.text.toString().trim()
            val faculty = etFaculty.text.toString().trim()
            val degree = etDegreeYear.text.toString().trim()

            if (name.isBlank()) {
                etFullName.error = "Full Name required"
                return@setOnClickListener
            }

            AppPreferences.setUserFullName(this, name)
            AppPreferences.setStudentNumber(this, if (number.isNotBlank()) number else "ST10458040")
            AppPreferences.setFaculty(this, if (faculty.isNotBlank()) faculty else "Faculty of Information Technology")
            AppPreferences.setDegreeYear(this, if (degree.isNotBlank()) degree else "Bachelor of IT • Year 3 (NQF 7)")

            loadProfile()
            loadEnrolledModules()
            Toast.makeText(this, "Academic record updated", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    private fun showChangeAvatarDialog() {
        val options = arrayOf("Pick from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Change Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> pickImageLauncher.launch("image/*")
                    1 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

        val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/$uid.jpg")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()
                
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(uid).update("profilePictureUrl", downloadUrl).await()
                
                val fullName = AppPreferences.getUserFullName(this@ProfileActivity)
                val studentNumber = AppPreferences.getStudentNumber(this@ProfileActivity)
                val parts = fullName.split(" ", limit = 2)
                val fName = parts.getOrNull(0) ?: ""
                val lName = parts.getOrNull(1) ?: ""
                
                val db = AppDatabase.getDatabase(this@ProfileActivity)
                db.plannerDao().insertUser(UserEntity(uid, fName, lName, studentNumber, downloadUrl))
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Profile picture updated", Toast.LENGTH_SHORT).show()
                    loadProfile()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    try {
                        val downloadUrl = storageRef.downloadUrl.await().toString()
                        val firestore = FirebaseFirestore.getInstance()
                        val data = hashMapOf(
                            "profilePictureUrl" to downloadUrl
                        )
                        firestore.collection("users").document(uid).set(data).await()
                        
                        val fullName = AppPreferences.getUserFullName(this@ProfileActivity)
                        val studentNumber = AppPreferences.getStudentNumber(this@ProfileActivity)
                        val parts = fullName.split(" ", limit = 2)
                        val fName = parts.getOrNull(0) ?: ""
                        val lName = parts.getOrNull(1) ?: ""
                        
                        val db = AppDatabase.getDatabase(this@ProfileActivity)
                        db.plannerDao().insertUser(UserEntity(uid, fName, lName, studentNumber, downloadUrl))
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfileActivity, "Profile picture updated", Toast.LENGTH_SHORT).show()
                            loadProfile()
                        }
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                        Toast.makeText(this@ProfileActivity, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
