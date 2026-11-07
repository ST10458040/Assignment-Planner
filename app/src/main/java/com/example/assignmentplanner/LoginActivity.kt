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
import android.util.Log
import java.security.MessageDigest

/**
 * LoginActivity manages the authentication flow for the user.
 * It provides both classic Email/Password login and Google Single Sign-On (SSO).
 */
class LoginActivity : BaseActivity() {

    /**
     * Called when the activity is first created.
     * Sets up UI listeners and auth behaviors.
     */
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
            
            com.google.firebase.auth.FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                        
                        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
                        if (email == "mlondi.sithole@uct.ac.za") {
                            prefs.edit().putString("user_first_name", "Mlondi").putString("user_last_name", "Sithole").putString("user_email", email).apply()
                        } else {
                            prefs.edit().putString("user_email", email).apply()
                        }
                        
                        val isOnboarded = prefs.getBoolean("is_onboarded", false)
                        if (isOnboarded) {
                            startActivity(Intent(this, DashboardActivity::class.java))
                        } else {
                            startActivity(Intent(this, WelcomeSetupActivity::class.java))
                        }
                        finish()
                    } else {
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        findViewById<Button>(R.id.btn_google_login).setOnClickListener {
            Log.i("LoginActivity", "Initiating Google Sign-In intent")
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso)
            startActivityForResult(googleSignInClient.signInIntent, 9001)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: com.google.android.gms.common.api.ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Authenticates with Firebase using the Google ID token obtained from SSO.
     * Maps Google profile properties to SharedPreferences.
     *
     * @param idToken The token provided by GoogleSignInAccount.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d("LoginActivity", "Exchanging Google Token for Firebase Credential")
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i("LoginActivity", "Google SSO Firebase Authentication Successful")
                    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
                    val fullName = user?.displayName ?: "Mlondi Shezi"
                    val names = fullName.split(" ")
                    prefs.edit()
                        .putString("user_first_name", names.firstOrNull() ?: "Mlondi")
                        .putString("user_last_name", names.drop(1).joinToString(" ").takeIf { it.isNotEmpty() } ?: "Shezi")
                        .putString("user_email", user?.email ?: "ST10458040@rcconnect.edu.za")
                        .apply()
                        
                    // Route to onboarding or main app depending on flag
                    val isOnboarded = prefs.getBoolean("is_onboarded", false)
                    if (isOnboarded) {
                        startActivity(Intent(this, DashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, WelcomeSetupActivity::class.java))
                    }
                    finish()
                } else {
                    Log.e("LoginActivity", "Google SSO Firebase Authentication Failed", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    private fun mockHashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
