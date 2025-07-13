package com.example.smarttaskai

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class ForgetPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forget_password)



        class ForgetPassword : AppCompatActivity() {
            private lateinit var auth: FirebaseAuth
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                setContentView(R.layout.activity_forget_password)

                auth = FirebaseAuth.getInstance()

                val forgotEmail = findViewById<EditText>(R.id.forgotEmailEditText)
                val resetPasswordButton = findViewById<Button>(R.id.resetPasswordButton)

                resetPasswordButton.setOnClickListener {
                    val email = forgotEmail.text.toString().trim()

                    if (email.isEmpty()) {
                        Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                                finish() // Optional: Close this screen after success
                            } else {
                                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }

                }
            }
        }
    }
}