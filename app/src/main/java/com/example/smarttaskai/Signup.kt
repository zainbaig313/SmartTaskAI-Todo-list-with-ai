package com.example.smarttaskai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class Signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)


        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.newUserEmail)
        val passwordEditText = findViewById<EditText>(R.id.newPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmNewPassword)
        val signupButton = findViewById<Button>(R.id.signUpBtn)


        signupButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Verification email sent to ${user.email}. Please verify and then log in.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    auth.signOut()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }

        }

    }
}
