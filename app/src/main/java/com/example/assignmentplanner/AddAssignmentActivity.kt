package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddAssignmentActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_assignment)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Header Actions
        findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            finish()
        }

        val etTitle = findViewById<EditText>(R.id.et_title)

        findViewById<TextView>(R.id.btn_save).setOnClickListener {
            if (etTitle?.text.toString().trim().isEmpty()) {
                etTitle?.error = "Required"
                return@setOnClickListener
            }
            Toast.makeText(this, "Assignment saved successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Bottom CTA Buttons
        findViewById<Button>(R.id.btn_create_assignment).setOnClickListener {
            if (etTitle?.text.toString().trim().isEmpty()) {
                etTitle?.error = "Required"
                return@setOnClickListener
            }
            Toast.makeText(this, "Assignment created!", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.btn_save_draft).setOnClickListener {
            Toast.makeText(this, "Saved as draft", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Subtask Action
        findViewById<Button>(R.id.btn_add_subtask).setOnClickListener {
            val subTaskInput = findViewById<EditText>(R.id.et_new_subtask)
            if (subTaskInput?.text.toString().isNotEmpty()) {
                Toast.makeText(this, "Subtask added", Toast.LENGTH_SHORT).show()
                subTaskInput?.text?.clear()
            }
        }
    }
}
