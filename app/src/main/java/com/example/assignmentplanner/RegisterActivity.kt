package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.security.MessageDigest

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val etFirstName = findViewById<EditText>(R.id.et_first_name)
        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val etPassword = findViewById<EditText>(R.id.et_reg_password)

        findViewById<Button>(R.id.btn_register).setOnClickListener {
            val fName = etFirstName?.text.toString().trim()
            val email = etEmail?.text.toString().trim()
            val password = etPassword?.text.toString().trim()

            if (fName.isEmpty()) {
                etFirstName?.error = "First name is required"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                etEmail?.error = "Email is required"
                return@setOnClickListener
            }
            if (password.isEmpty() || password.length < 8) {
                etPassword?.error = "Password must be at least 8 characters"
                return@setOnClickListener
            }
            
            // Mock encrypt
            val encryptedPassword = mockHashPassword(password)
            println(encryptedPassword)

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btn_google_signup).setOnClickListener {
            Toast.makeText(this, "SSO Registration Success!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        findViewById<TextView>(R.id.tv_back_to_login).setOnClickListener {
            finish()
        }
    }
    
    private fun mockHashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
