package com.example.publishingapp.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.PixelCopy.request
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.publishingapp.R
import com.example.publishingapp.data.network.AppPrefs
import com.example.publishingapp.data.network.RegisterRequest
import com.example.publishingapp.data.repository.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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

        val tilUsername = findViewById<TextInputLayout>(R.id.tilUsername)
        val tilLastName = findViewById<TextInputLayout>(R.id.tilLastName)
        val tilFirstName = findViewById<TextInputLayout>(R.id.tilFirstName)
        val tilPhone = findViewById<TextInputLayout>(R.id.tilPhone)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val tilRepeat = findViewById<TextInputLayout>(R.id.tilRepeat)

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

            if (!validateAndScroll()) {
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val request = RegisterRequest(
                        username = username,
                        phone = phone,
                        email = email.ifBlank { null },
                        password = password,
                        firstName = firstName,
                        lastName = lastName,
                        middleName = middleName.ifBlank { null }
                    )

                    AuthRepository.register(request)

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
        val errorCard = findViewById<View>(R.id.errorCard)
        val tvError = findViewById<TextView>(R.id.tvError)

        val error = e.response()?.errorBody()?.string() ?: ""

        val message = when {
            error.contains("USERNAME_EXISTS") -> "Имя пользователя уже занято"
            error.contains("PHONE_EXISTS") -> "Телефон уже используется"
            error.contains("EMAIL_EXISTS") -> "Email уже используется"
            else -> "Ошибка регистрации"
        }

        tvError.text = message
        errorCard.visibility = View.VISIBLE
    }
    private fun setError(til: TextInputLayout, message: String) {
        til.error = message
    }

    private fun clearErrors(vararg tils: TextInputLayout) {
        tils.forEach { it.error = null }
    }

    private fun validateAndScroll(): Boolean {

        val scroll = findViewById<ScrollView>(R.id.scrollView)

        val tilUsername = findViewById<TextInputLayout>(R.id.tilUsername)
        val tilLastName = findViewById<TextInputLayout>(R.id.tilLastName)
        val tilFirstName = findViewById<TextInputLayout>(R.id.tilFirstName)
        val tilPhone = findViewById<TextInputLayout>(R.id.tilPhone)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val tilRepeat = findViewById<TextInputLayout>(R.id.tilRepeat)

        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etLastName = findViewById<TextInputEditText>(R.id.etLastName)
        val etFirstName = findViewById<TextInputEditText>(R.id.etFirstName)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etRepeat = findViewById<TextInputEditText>(R.id.etRepeatPassword)

        clearErrors(
            tilUsername, tilLastName, tilFirstName,
            tilPhone, tilEmail, tilPassword, tilRepeat
        )

        var firstError: View? = null

        if (etUsername.text.isNullOrBlank()) {
            setError(tilUsername, "Введите имя пользователя")
            firstError = tilUsername
        }

        if (etLastName.text.isNullOrBlank()) {
            setError(tilLastName, "Введите фамилию")
            if (firstError == null) firstError = tilLastName
        }

        if (etFirstName.text.isNullOrBlank()) {
            setError(tilFirstName, "Введите имя")
            if (firstError == null) firstError = tilFirstName
        }

        val phonePattern = Regex("^\\+7\\d{10}$")
        if (!phonePattern.matches(etPhone.text.toString())) {
            setError(tilPhone, "Формат: +7XXXXXXXXXX")
            if (firstError == null) firstError = tilPhone
        }

        if (etEmail.text.toString().isNotBlank() &&
            !android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches()
        ) {
            setError(tilEmail, "Некорректный email")
            if (firstError == null) firstError = tilEmail
        }

        if (etPassword.text.toString().length < 6) {
            setError(tilPassword, "Минимум 6 символов")
            if (firstError == null) firstError = tilPassword
        }

        if (etPassword.text.toString() != etRepeat.text.toString()) {
            setError(tilRepeat, "Пароли не совпадают")
            if (firstError == null) firstError = tilRepeat
        }

        firstError?.let {
            scroll.post {
                scroll.smoothScrollTo(0, it.top)
            }
            return false
        }

        return true
    }

    private fun toast(msg: String): Boolean {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        return false
    }


}