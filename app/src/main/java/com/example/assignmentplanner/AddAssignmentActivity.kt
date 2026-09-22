package com.example.assignmentplanner

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddAssignmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_assignment)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Header Actions
        findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btn_save).setOnClickListener {
            // Save logic placeholder
            finish()
        }

        // Bottom CTA Buttons
        findViewById<Button>(R.id.btn_create_assignment).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_save_draft).setOnClickListener {
            finish()
        }

        // Subtask Action
        findViewById<Button>(R.id.btn_add_subtask).setOnClickListener {
            // Add subtask logic placeholder
        }
    }
}
