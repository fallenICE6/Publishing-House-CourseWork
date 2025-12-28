package com.example.publishingapp.ui.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.publishingapp.R
import com.example.publishingapp.data.mock.MockUserRepository
import com.example.publishingapp.data.models.User
import com.example.publishingapp.data.models.UserRole
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etLastName = findViewById<TextInputEditText>(R.id.etLastName)
        val etFirstName = findViewById<TextInputEditText>(R.id.etFirstName)
        val etMiddleName = findViewById<TextInputEditText>(R.id.etMiddleName)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etRepeatPassword = findViewById<TextInputEditText>(R.id.etRepeatPassword)
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnRegister).setOnClickListener {

            val username = etUsername.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val middleName = etMiddleName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val repeat = etRepeatPassword.text.toString()

            if (!validate(username, firstName, lastName, email, password, repeat)) {
                return@setOnClickListener
            }

            val user = User(
                id = System.currentTimeMillis().toInt(),
                username = username,
                phone = phone,
                email = email,
                password = password,
                first_name = firstName,
                last_name = lastName,
                middle_name = middleName,
                role = UserRole.AUTHOR
            )

            if (MockUserRepository.register(user)) {
                Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Username уже занят", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validate(
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        repeatPassword: String
    ): Boolean {

        if (username.isBlank()) {
            toast("Введите username")
            return false
        }

        if (lastName.isBlank()) {
            toast("Введите фамилию")
            return false
        }

        if (firstName.isBlank()) {
            toast("Введите имя")
            return false
        }

        if (email.isBlank()) {
            toast("Введите email")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Некорректный email")
            return false
        }

        if (password.length < 6) {
            toast("Пароль должен быть не менее 6 символов")
            return false
        }

        if (password != repeatPassword) {
            toast("Пароли не совпадают")
            return false
        }

        return true
    }


    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
