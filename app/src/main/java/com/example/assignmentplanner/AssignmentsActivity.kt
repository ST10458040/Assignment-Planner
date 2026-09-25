package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View



class AssignmentsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assignments)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.fab_add)?.setOnClickListener {
            startActivity(Intent(this, AddAssignmentActivity::class.java))
        }

        findViewById<View>(R.id.card_due_today)?.setOnClickListener {
            startActivity(Intent(this, AssignmentDetailsActivity::class.java))
        }

        findViewById<View>(R.id.card_upcoming_1)?.setOnClickListener {
            startActivity(Intent(this, AssignmentDetailsActivity::class.java))
        }

        findViewById<View>(R.id.iv_profile_icon)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        setupCustomBottomNavigation(R.id.nav_assignments)
    }

    override fun onResume() {
        super.onResume()
        setupCustomBottomNavigation(R.id.nav_assignments)
    }
}
