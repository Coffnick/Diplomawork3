package com.example.diplomawork

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.diplomawork.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException


class Registration : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        binding.RegBtn.setOnClickListener{
            val email = binding.RegEmail.text.toString().trim()
            val pass = binding.RegPassword.text.toString().trim()
            if (email.isEmpty()){
                binding.RegEmail.error="Не может быть пустым"

            }else if (pass.isEmpty()){
                binding.RegPassword.error="Не может быть пустым"

            }else{
                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this@Registration, Profile::class.java)
                        Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_LONG).show()
                        startActivity(intent)
                        finish()
                    } else { //код обработки ошибки
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Неверный формат адреса электронной почты", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e("RegistrationActivity", "Исключение: ${e.message}", e)
                        }
                    }
                }
            }
        }
    }
}