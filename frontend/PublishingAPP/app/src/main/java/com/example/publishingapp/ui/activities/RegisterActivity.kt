package com.example.publishingapp.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.publishingapp.R
import com.example.publishingapp.data.network.RegisterRequest
import com.example.publishingapp.data.repository.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import retrofit2.HttpException

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

        // Маска телефона +7XXXXXXXXXX
        etPhone.addTextChangedListener(object : TextWatcher {
            var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val digits = s.toString().replace("\\D".toRegex(), "")
                val formatted = when {
                    digits.startsWith("7") -> "+$digits"
                    digits.startsWith("8") -> "+7" + digits.drop(1)
                    digits.isEmpty() -> "+7"
                    else -> "+7$digits"
                }

                val maxLength = 12
                val result = if (formatted.length > maxLength) {
                    formatted.substring(0, maxLength)
                } else {
                    formatted
                }

                val finalResult = if (result.length < 2) "+7" else result

                etPhone.setText(finalResult)
                etPhone.setSelection(etPhone.text?.length ?: 0)

                isEditing = false
            }
        })

        etPhone.setText("+7")

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

            if (!validate(username, firstName, lastName, phone, email, password, repeat)) {
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    AuthRepository.register(
                        RegisterRequest(
                            username = username,
                            phone = phone,
                            email = email.ifBlank { null },
                            password = password,
                            firstName = firstName,
                            lastName = lastName,
                            middleName = middleName.ifBlank { null }
                        )
                    )

                    Toast.makeText(
                        this@RegisterActivity,
                        "Регистрация успешна",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()

                } catch (e: HttpException) {
                    handleHttpError(e)
                } catch (e: Exception) {
                    toast("Ошибка соединения")
                }
            }
        }
    }

    private fun handleHttpError(e: HttpException) {
        val error = e.response()?.errorBody()?.string() ?: ""

        when {
            error.contains("USERNAME_EXISTS") ->
                toast("Имя пользователя уже занято")

            error.contains("PHONE_EXISTS") ->
                toast("Телефон уже используется")

            error.contains("EMAIL_EXISTS") ->
                toast("Email уже используется")

            else ->
                toast("Ошибка регистрации")
        }
    }

    private fun validate(
        username: String,
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        password: String,
        repeatPassword: String
    ): Boolean {

        if (username.isBlank()) return toast("Введите username")
        if (lastName.isBlank()) return toast("Введите фамилию")
        if (firstName.isBlank()) return toast("Введите имя")

        val phonePattern = Regex("^\\+7\\d{10}$")
        if (!phonePattern.matches(phone))
            return toast("Некорректный номер телефона. Формат: +7XXXXXXXXXX")

        if (email.isNotBlank() &&
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) return toast("Некорректный email")

        if (password.length < 6)
            return toast("Пароль минимум 6 символов")

        if (password != repeatPassword)
            return toast("Пароли не совпадают")

        return true
    }

    private fun toast(msg: String): Boolean {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        return false
    }
}