package com.example.assignmentplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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

class RegisterActivity : BaseActivity() {
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
        val actvFaculty = findViewById<AutoCompleteTextView>(R.id.actv_faculty)
        val tilCustomInstitution = findViewById<View>(R.id.til_custom_institution)
        val etCustomInstitution = findViewById<EditText>(R.id.et_custom_institution)

        val institutions = arrayOf(
            "University of Cape Town (UCT)",
            "University of the Witwatersrand (Wits)",
            "University of Pretoria (UP)",
            "University of Johannesburg (UJ)",
            "University of KwaZulu-Natal (UKZN)",
            "Stellenbosch University (SU)",
            "University of the Western Cape (UWC)",
            "Rhodes University (RU)",
            "North-West University (NWU)",
            "University of the Free State (UFS)",
            "University of South Africa (UNISA)",
            "Nelson Mandela University (NMU)",
            "University of Fort Hare (UFH)",
            "University of Limpopo (UL)",
            "University of Venda (UNIVEN)",
            "Walter Sisulu University (WSU)",
            "University of Zululand (UNIZULU)",
            "Sefako Makgatho Health Sciences University (SMU)",
            "Sol Plaatje University (SPU)",
            "University of Mpumalanga (UMP)",
            "Durban University of Technology (DUT)",
            "Cape Peninsula University of Technology (CPUT)",
            "Tshwane University of Technology (TUT)",
            "Central University of Technology (CUT)",
            "Mangosuthu University of Technology (MUT)",
            "Vaal University of Technology (VUT)",
            "The IIE (Varsity College / Rosebank College / Vega)",
            "Eduvos",
            "Stadio Higher Education",
            "Boston City Campus",
            "MANCOSA",
            "Regent Business School",
            "Other / Enter Custom Institution..."
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, institutions)
        actvFaculty?.setAdapter(adapter)

        actvFaculty?.setOnItemClickListener { _, _, position, _ ->
            if (institutions[position] == "Other / Enter Custom Institution...") {
                tilCustomInstitution?.visibility = View.VISIBLE
            } else {
                tilCustomInstitution?.visibility = View.GONE
                etCustomInstitution?.text = null
            }
        }

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
            
            var selectedInstitution = actvFaculty?.text.toString()
            if (selectedInstitution == "Other / Enter Custom Institution...") {
                val customInst = etCustomInstitution?.text.toString().trim()
                if (customInst.isEmpty()) {
                    etCustomInstitution?.error = "Custom institution is required"
                    return@setOnClickListener
                }
                selectedInstitution = customInst
            }
            
            com.google.firebase.auth.FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
                        val lName = findViewById<EditText>(R.id.et_last_name)?.text.toString().trim()
                        prefs.edit().putString("user_first_name", fName)
                             .putString("user_last_name", lName)
                             .putString("user_email", email)
                             .putString("user_institution", selectedInstitution)
                             .apply()
            
                        Toast.makeText(this, "Account created successfully! Welcome to $selectedInstitution", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, WelcomeSetupActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        findViewById<Button>(R.id.btn_google_signup).setOnClickListener {
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso)
            startActivityForResult(googleSignInClient.signInIntent, 9002)
        }

        findViewById<TextView>(R.id.tv_back_to_login).setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9002) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: com.google.android.gms.common.api.ApiException) {
                Toast.makeText(this, "Google sign up failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
                    val fullName = user?.displayName ?: "Mlondi Shezi"
                    val names = fullName.split(" ")
                    prefs.edit()
                        .putString("user_first_name", names.firstOrNull() ?: "Mlondi")
                        .putString("user_last_name", names.drop(1).joinToString(" ").takeIf { it.isNotEmpty() } ?: "Shezi")
                        .putString("user_email", user?.email ?: "ST10458040@rcconnect.edu.za")
                        // Google users can set institution in Welcome Setup
                        .putString("user_institution", "Not Specified")
                        .apply()
                        
                    Toast.makeText(this, "SSO Registration Success!", Toast.LENGTH_SHORT).show()
                    val isOnboarded = prefs.getBoolean("is_onboarded", false)
                    if (isOnboarded) {
                        startActivity(Intent(this, DashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, WelcomeSetupActivity::class.java))
                    }
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    private fun mockHashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
