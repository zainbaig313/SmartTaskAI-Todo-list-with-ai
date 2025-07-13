package com.example.smarttaskai

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.example.smarttaskai.R


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account.idToken!!)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // 🔄 Auto-login check
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.providerData.any { it.providerId == "google.com" } || currentUser.isEmailVerified) {
                // Go to Home directly
                startActivity(Intent(this, Home::class.java))
                finish()
                return
            }
        }

        // Google Sign-In setup
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignInBtn = findViewById<Button>(R.id.googleSignInBtn)
        val userEmail = findViewById<EditText>(R.id.userEmail)
        val userPassword = findViewById<TextView>(R.id.userPassword)
        val signUp = findViewById<TextView>(R.id.signUpPage)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val forget = findViewById<TextView>(R.id.forgetPassword)

        googleSignInBtn.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        forget.setOnClickListener {
            val email = userEmail.text.toString()
            startActivity(Intent(this, ForgetPassword::class.java).apply {
                putExtra("Email", email)
            })
        }

        signUp.setOnClickListener {
            startActivity(Intent(this, Signup::class.java))
        }

        loginBtn.setOnClickListener {
            val email = userEmail.text.toString().trim()
            val password = userPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            startActivity(Intent(this, Home::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_LONG).show()
                            auth.signOut()
                        }
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, Home::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Firebase auth failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
