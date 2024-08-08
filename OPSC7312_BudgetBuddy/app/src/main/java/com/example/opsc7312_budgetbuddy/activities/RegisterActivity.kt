package com.example.opsc7312_budgetbuddy.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc7312_budgetbuddy.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        passwordListener()
        firebaseAuth =  FirebaseAuth.getInstance()

        binding.registerBtn.setOnClickListener {
            val email = binding.emailAdrdessInput.text.toString()
            val password = binding.passwordInput.text.toString()

            // Check if the email and password are not empty
            if (email.isNotEmpty() && password.isNotEmpty()) {

                if (validPassword() == null && validEmail() == null) {
                    // Password is valid, proceed with registration of the user
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // Shows an error message on registration failure
                                Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                // Show a message if email or password is empty
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginLink.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun passwordListener(){
        binding.passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.passwordContainer.helperText = validPassword()
                binding.emailContainer.helperText = validEmail()

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.passwordContainer.helperText = validPassword()
                binding.emailContainer.helperText = validEmail()
            }
        })
        binding.emailAdrdessInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.emailContainer.helperText = validEmail()

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.emailContainer.helperText = validEmail()
            }
        })


    }

    private fun validEmail(): String?{
        val emailText = binding.emailAdrdessInput.text.toString()
        if(!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
            return "Invalid Email Address"
        }
        return null
    }

    private fun validPassword(): String? {
        val passwordText = binding.passwordInput.text.toString()
        if (passwordText.length < 8) {
            return "Minimum 8 characters required"
        }
        if (!passwordText.matches(".*[A-Z].*".toRegex())) {
            return "Must contain at least 1 Upper-case character"
        }
        if (!passwordText.matches(".*[a-z].*".toRegex())) {
            return "Must contain at least 1 Lower-case character"
        }
        if (!passwordText.matches(".*\\d.*".toRegex())) {
            return "Must contain at least 1 digit"
        }
        if (!passwordText.matches(".*[@#\$%&*!?~^()].*".toRegex())) {
            return "Must contain at least 1 Special character"
        }

        return null
    }
}