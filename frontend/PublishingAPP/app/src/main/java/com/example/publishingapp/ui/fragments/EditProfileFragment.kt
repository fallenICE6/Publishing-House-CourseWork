package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.publishingapp.R
import com.example.publishingapp.data.network.UpdateUserRequest
import com.example.publishingapp.data.repository.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = AuthRepository.currentUser ?: return

        val tilFirst = view.findViewById<TextInputLayout>(R.id.tilFirstName)
        val tilLast = view.findViewById<TextInputLayout>(R.id.tilLastName)
        val tilMiddle = view.findViewById<TextInputLayout>(R.id.tilMiddleName)
        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPhone = view.findViewById<TextInputLayout>(R.id.tilPhone)

        val etFirst = view.findViewById<EditText>(R.id.etFirstName)
        val etLast = view.findViewById<EditText>(R.id.etLastName)
        val etMiddle = view.findViewById<EditText>(R.id.etMiddleName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        // Заполняем поля
        etFirst.setText(user.firstName)
        etLast.setText(user.lastName)
        etMiddle.setText(user.middleName)
        etEmail.setText(user.email)
        etPhone.setText(user.phone)

        // Маска телефона
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
                    else -> "+7$digits"
                }
                etPhone.setText(formatted.take(12))
                etPhone.setSelection(etPhone.text?.length ?: 0)
                isEditing = false
            }
        })

        btnSave.setOnClickListener {
            val firstName = etFirst.text.toString().trim()
            val lastName = etLast.text.toString().trim()
            val middleName = etMiddle.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            // Сброс цвета обводки
            val defaultColor = ContextCompat.getColor(requireContext(), R.color.purple_80)
            tilFirst.setBoxStrokeColor(defaultColor)
            tilLast.setBoxStrokeColor(defaultColor)
            tilMiddle.setBoxStrokeColor(defaultColor)
            tilEmail.setBoxStrokeColor(defaultColor)
            tilPhone.setBoxStrokeColor(defaultColor)

            var hasError = false

            if (firstName.isEmpty()) { tilFirst.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.red_60)); hasError = true }
            if (lastName.isEmpty()) { tilLast.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.red_60)); hasError = true }
            if (phone.isEmpty()) { tilPhone.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.red_60)); hasError = true }
            val phonePattern = Regex("^\\+7\\d{10}$")
            if (!phonePattern.matches(phone)) { tilPhone.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.red_60)); hasError = true }
            if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { tilEmail.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.red_60)); hasError = true }
            if (hasError) {
                Toast.makeText(context, "Проверьте правильность полей", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Обновляем профиль через бекенд
            lifecycleScope.launch {
                try {
                    val updatedUser = AuthRepository.updateProfile(
                        UpdateUserRequest(
                            firstName = firstName,
                            lastName = lastName,
                            middleName = middleName.ifBlank { null },
                            email = email.ifBlank { null },
                            phone = phone
                        )
                    )

                    parentFragmentManager.setFragmentResult("profile_updated", Bundle())
                    parentFragmentManager.popBackStack()
                    Toast.makeText(context, "Профиль обновлен", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка при обновлении: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
