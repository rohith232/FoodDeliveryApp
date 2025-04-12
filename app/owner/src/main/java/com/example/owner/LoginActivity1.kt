package com.example.owner


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.owner.databinding.ActivityLogin1Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity1 : AppCompatActivity() {

    private lateinit var binding: ActivityLogin1Binding
    private val auth: FirebaseAuth by lazy { Firebase.auth }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is already logged in
        if (auth.currentUser != null && auth.currentUser?.isEmailVerified == true) {
            redirectToMain()
            return
        }

        setupLoginButton()

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity1::class.java))
        }
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            checkEmailVerification()
                        } else {
                            Toast.makeText(
                                this,
                                "Login failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun checkEmailVerification() {
        if (auth.currentUser?.isEmailVerified == true) {
            redirectToMain()
        } else {
            Toast.makeText(
                this,
                "Please verify your email first",
                Toast.LENGTH_LONG
            ).show()
            auth.signOut()
        }
    }

    private fun redirectToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}