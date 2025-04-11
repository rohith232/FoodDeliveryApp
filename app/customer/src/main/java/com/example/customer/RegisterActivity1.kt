package com.example.customer

import Model.User
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.customer.databinding.ActivityRegister1Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity1 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister1Binding
    private val auth: FirebaseAuth by lazy { Firebase.auth }  // Lazy initialization
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val name = binding.nameEditText.text.toString().trim()
            val phone = binding.phoneEditText.text.toString().trim()

            if (validateInputs(email, password, name, phone)) {
                registerUser(email, password, name, phone)
            }
        }

        binding.signin.setOnClickListener {
            startActivity(Intent(this, LoginActivity1::class.java))
            finish()
        }
    }

    private fun validateInputs(email: String, password: String, name: String, phone: String): Boolean {
        var valid = true

        when {
            email.isEmpty() -> {
                binding.emailEditText.error = "Email required"
                binding.emailEditText.requestFocus()
                valid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailEditText.error = "Invalid email"
                binding.emailEditText.requestFocus()
                valid = false
            }
        }

        when {
            password.isEmpty() -> {
                binding.passwordEditText.error = "Password required"
                binding.passwordEditText.requestFocus()
                valid = false
            }
            password.length < 6 -> {
                binding.passwordEditText.error = "Password must be at least 6 characters"
                binding.passwordEditText.requestFocus()
                valid = false
            }
        }

        if (name.isEmpty()) {
            binding.nameEditText.error = "Name required"
            binding.nameEditText.requestFocus()
            valid = false
        }

        if (phone.isEmpty()) {
            binding.phoneEditText.error = "Phone required"
            binding.phoneEditText.requestFocus()
            valid = false
        }

        return valid
    }

    private fun registerUser(email: String, password: String, name: String, phone: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    handleSuccessfulRegistration(email, name, phone)
                } else {
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun handleSuccessfulRegistration(email: String, name: String, phone: String) {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { verificationTask ->
                if (verificationTask.isSuccessful) {
                    saveUserToDatabase(user.uid, email, name, phone)
                } else {
                    Toast.makeText(
                        this,
                        "Failed to send verification email: ${verificationTask.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(userId: String, email: String, name: String, phone: String) {
        val user = User(
            userId = userId,
            name = name,
            email = email,
            phone = phone
        )

        databaseReference.child(userId).setValue(user)
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Registration successful! Please verify your email.",
                        Toast.LENGTH_LONG
                    ).show()
                    navigateToLogin()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to save user data: ${dbTask.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity1::class.java))
        finish()
    }
}