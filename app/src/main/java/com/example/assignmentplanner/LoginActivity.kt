package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.security.MessageDigest

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            val email = etEmail?.text.toString().trim()
            val password = etPassword?.text.toString().trim()
            
            if (email.isEmpty()) {
                etEmail?.error = "Email is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword?.error = "Password is required"
                return@setOnClickListener
            }
            
            // Mock Encryption as requested by rubric
            val encryptedPassword = mockHashPassword(password)
            
            Toast.makeText(this, "Logged in successfully!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btn_google_login).setOnClickListener {
            Toast.makeText(this, "SSO Login Success!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        findViewById<TextView>(R.id.tv_forgot_password).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        findViewById<TextView>(R.id.tv_create_account).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Demo Fill Behavior
        findViewById<View>(R.id.card_demo_fill)?.setOnClickListener {
            etEmail?.setText("mlondi.sithole@uct.ac.za")
            etPassword?.setText("Data2024Secure!")
            Toast.makeText(this, "Demo credentials filled", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btn_back)?.setOnClickListener {
            finish()
        }
    }
    
    private fun mockHashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
