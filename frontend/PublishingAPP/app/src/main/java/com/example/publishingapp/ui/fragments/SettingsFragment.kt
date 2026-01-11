package com.example.publishingapp.ui.fragments

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.publishingapp.R
import com.example.publishingapp.data.repository.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var switchNotifications: SwitchMaterial

    private lateinit var tilCurrent: TextInputLayout
    private lateinit var tilNew: TextInputLayout
    private lateinit var tilConfirm: TextInputLayout

    private lateinit var etCurrent: TextInputEditText
    private lateinit var etNew: TextInputEditText
    private lateinit var etConfirm: TextInputEditText

    private lateinit var btnChangePassword: MaterialButton

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===== Инициализация =====
        switchNotifications = view.findViewById(R.id.switchNotifications)

        tilCurrent = view.findViewById(R.id.tilCurrentPassword)
        tilNew = view.findViewById(R.id.tilNewPassword)
        tilConfirm = view.findViewById(R.id.tilConfirmPassword)

        etCurrent = view.findViewById(R.id.etCurrentPassword)
        etNew = view.findViewById(R.id.etNewPassword)
        etConfirm = view.findViewById(R.id.etConfirmPassword)

        btnChangePassword = view.findViewById(R.id.btnChangePassword)

        setupPasswordChange()
        setupNotificationsToggle()
    }

    private fun setupPasswordChange() {
        btnChangePassword.setOnClickListener {
            val current = etCurrent.text.toString()
            val newPass = etNew.text.toString()
            val confirm = etConfirm.text.toString()

            resetFieldColors()

            // ===== Проверки на фронте =====
            if (current.isBlank()) {
                tilCurrent.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.red_60))
                Toast.makeText(requireContext(), "Введите текущий пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass.length < 6) {
                tilNew.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.red_60))
                Toast.makeText(requireContext(), "Новый пароль слишком короткий (мин. 6 символов)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass != confirm) {
                tilConfirm.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.red_60))
                Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ===== Отправка запроса на бек =====
            lifecycleScope.launch {
                try {
                    AuthRepository.changePassword(current, newPass)
                    Toast.makeText(requireContext(), "Пароль успешно изменен", Toast.LENGTH_SHORT).show()
                    etCurrent.text?.clear()
                    etNew.text?.clear()
                    etConfirm.text?.clear()
                } catch (e: Exception) {
                    // Ошибка на бекенде (неверный текущий пароль или другая проблема)
                    tilCurrent.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.red_60))
                    Toast.makeText(requireContext(), "Текущий пароль неверен", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupNotificationsToggle() {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = nm.getNotificationChannel("default_channel")
                if (channel != null) {
                    channel.importance = if (isChecked)
                        NotificationManager.IMPORTANCE_DEFAULT
                    else
                        NotificationManager.IMPORTANCE_NONE
                    nm.createNotificationChannel(channel)
                }
            }

            Toast.makeText(
                requireContext(),
                if (isChecked) "Уведомления включены" else "Уведомления отключены",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun resetFieldColors() {
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.purple_80)
        tilCurrent.setBoxStrokeColor(defaultColor)
        tilNew.setBoxStrokeColor(defaultColor)
        tilConfirm.setBoxStrokeColor(defaultColor)
    }
}
