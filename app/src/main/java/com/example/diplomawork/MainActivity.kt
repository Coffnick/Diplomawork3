package com.example.diplomawork

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.diplomawork.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.LoginBtn.setOnClickListener {
            val email = binding.Email.text.toString().trim()
            val pass = binding.Password.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                binding.Email.error = "Smth wrong"
                binding.Password.error = "Smth wrong"


            } else {
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this@MainActivity,Profile::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "Okay", Toast.LENGTH_LONG).show()

                    } else {
                        Toast.makeText(this, "wrong", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
        binding.registration.setOnClickListener{
           val intent = Intent(this@MainActivity,Registration::class.java)
            startActivity(intent)

        }
    }
}