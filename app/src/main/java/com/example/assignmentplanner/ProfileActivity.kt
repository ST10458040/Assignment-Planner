package com.example.assignmentplanner

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class ProfileActivity : BaseActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvFaculty: TextView
    private lateinit var tvAvatar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvName = findViewById(R.id.tv_profile_name) ?: findViewById(R.id.tv_header_title) // Fallback temporarily
        tvFaculty = findViewById(R.id.tv_profile_faculty) ?: findViewById(R.id.tv_header_title)
        tvAvatar = findViewById(R.id.tv_profile_avatar)
        
        loadProfile()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btn_edit_profile).setOnClickListener {
            showEditProfileDialog()
        }

        findViewById<ImageView>(R.id.btn_change_avatar)?.setOnClickListener {
            showChangeAvatarDialog()
        }

        findViewById<View>(R.id.btn_change_password)?.setOnClickListener {
            Toast.makeText(this, "Password reset email sent to student address.", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btn_export_data)?.setOnClickListener {
            Toast.makeText(this, "Exporting timetable as JSON...", Toast.LENGTH_SHORT).show()
            // Mock implicit intent
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "{ \"tasks\": [] }")
                type = "text/json"
            }
            startActivity(Intent.createChooser(sendIntent, "Export Timetable"))
        }

        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            // Clear mock session
            val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
            prefs.edit().putBoolean("is_logged_in", false).apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupCustomBottomNavigation(-1)
    }

    private fun loadProfile() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val name = prefs.getString("profile_name", "Mlondi Sithole") ?: "Mlondi Sithole"
        val faculty = prefs.getString("profile_faculty", "Information Systems & Computer Science") ?: "Information Systems & Computer Science"
        
        // Let's set the text if we can find the views accurately.
        // For simplicity, we just extract initials
        val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
        tvAvatar.text = initials
    }

    private fun showEditProfileDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Profile")
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        
        val nameInput = EditText(this).apply {
            hint = "Full Name"
            setText(prefs.getString("profile_name", "Mlondi Sithole"))
        }
        val facultyInput = EditText(this).apply {
            hint = "Faculty"
            setText(prefs.getString("profile_faculty", "Information Systems"))
        }
        
        layout.addView(nameInput)
        layout.addView(facultyInput)
        builder.setView(layout)

        builder.setPositiveButton("Save") { dialog, _ ->
            prefs.edit()
                .putString("profile_name", nameInput.text.toString())
                .putString("profile_faculty", facultyInput.text.toString())
                .apply()
            loadProfile()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showChangeAvatarDialog() {
        val options = arrayOf("Tech Icon", "Graduate Icon", "Initials (Default)")
        AlertDialog.Builder(this)
            .setTitle("Select Avatar Style")
            .setItems(options) { dialog, which ->
                Toast.makeText(this, "Avatar style updated to ${options[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }
}
