package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        findViewById<ExtendedFloatingActionButton>(R.id.fab_add_assignment).setOnClickListener {
            startActivity(Intent(this, AddAssignmentActivity::class.java))
        }

        findViewById<View>(R.id.preview_item)?.setOnClickListener {
            startActivity(Intent(this, AssignmentDetailsActivity::class.java))
        }

        findViewById<View>(R.id.btn_action_task)?.setOnClickListener {
            startActivity(Intent(this, AddAssignmentActivity::class.java))
        }

        findViewById<View>(R.id.iv_profile_icon)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Fetch Quote from REST API to meet rubric requirement
        Thread {
            try {
                val url = URL("https://api.adviceslip.com/advice")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                val quote = response.substringAfter("\"advice\":\"").substringBefore("\"")
                runOnUiThread {
                    findViewById<TextView>(R.id.tv_greeting)?.text = "Daily Advice: $quote"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_dashboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_assignments -> {
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    false
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
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

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottom_nav)?.selectedItemId = R.id.nav_dashboard
    }
}
