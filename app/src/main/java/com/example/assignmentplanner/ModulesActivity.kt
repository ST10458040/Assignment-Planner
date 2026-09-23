package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class ModulesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_modules)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.iv_profile_icon)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btn_add_module).setOnClickListener {
            startActivity(Intent(this, ModuleDetailsActivity::class.java))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = -1 // Not a primary tab in the current menu
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    false
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    false
                }
                R.id.nav_assignments -> {
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    false
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }
}
