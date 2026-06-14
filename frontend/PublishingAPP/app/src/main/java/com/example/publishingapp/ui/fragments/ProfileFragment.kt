package com.example.publishingapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.publishingapp.R
import com.example.publishingapp.data.repository.AuthRepository
import com.example.publishingapp.ui.activities.RegisterActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var authContainer: View
    private lateinit var profileContainer: View

    private lateinit var btnOrders: MaterialButton
    private lateinit var btnManageUsers: MaterialButton

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvGoRegister: TextView

    private lateinit var tvUsername: TextView
    private lateinit var tvFio: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var chipRole: Chip
    private lateinit var btnLogout: MaterialButton
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnSettings: MaterialButton

    private lateinit var errorCard: MaterialCardView
    private lateinit var tvError: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authContainer = view.findViewById(R.id.includeAuth)
        profileContainer = view.findViewById(R.id.includeProfile)

        btnOrders = view.findViewById(R.id.btnOrders)
        btnManageUsers = view.findViewById(R.id.btnManageUsers)

        etUsername = view.findViewById(R.id.etUsername)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        tvGoRegister = view.findViewById(R.id.tvGoRegister)

        tvUsername = view.findViewById(R.id.tvUsername)
        tvFio = view.findViewById(R.id.tvFio)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvPhone = view.findViewById(R.id.tvPhone)
        chipRole = view.findViewById(R.id.chipRole)

        btnLogout = view.findViewById(R.id.btnLogout)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnSettings = view.findViewById(R.id.btnSettings)

        errorCard = view.findViewById(R.id.errorCard)
        tvError = view.findViewById(R.id.tvError)

        setupListeners()

        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }


    private fun updateUI() {
        val user = AuthRepository.currentUser

        if (user != null) {
            showProfile()
        } else {
            showAuth()
        }
    }

    private fun showAuth() {
        authContainer.visibility = View.VISIBLE
        profileContainer.visibility = View.GONE
    }
    private fun showProfile() {
        val user = AuthRepository.currentUser ?: return

        authContainer.visibility = View.GONE
        profileContainer.visibility = View.VISIBLE

        tvUsername.text = "@${user.username}"
        tvFio.text = "${user.lastName} ${user.firstName} ${user.middleName ?: ""}"
        tvEmail.text = user.email ?: "—"
        tvPhone.text = user.phone

        when (user.role) {
            "ADMIN" -> {
                chipRole.text = "Администратор"

                btnOrders.text = "Управление заказами"
                btnOrders.setIconResource(R.drawable.ic_orders)

                btnManageUsers.visibility = View.VISIBLE
                btnManageUsers.text = "Управление пользователями"
                btnManageUsers.setIconResource(R.drawable.ic_users_empty)
            }

            "AUTHOR" -> {
                chipRole.text = "Автор"

                btnOrders.text = "Мои заказы"
                btnOrders.setIconResource(R.drawable.ic_orders)

                btnManageUsers.visibility = View.GONE
            }

            "REVIEWER" -> {
                chipRole.text = "Рецензент"

                btnOrders.text = "Заказы для рецензии"
                btnOrders.setIconResource(R.drawable.ic_orders)

                btnManageUsers.visibility = View.VISIBLE
                btnManageUsers.text = "Мои рецензии"
                btnManageUsers.setIconResource(R.drawable.ic_list)
            }

            else -> {
                chipRole.text = "Пользователь"

                btnOrders.text = "Мои заказы"
                btnOrders.setIconResource(R.drawable.ic_orders)

                btnManageUsers.visibility = View.GONE
            }
        }
    }


    private fun setupListeners() {

        btnLogin.setOnClickListener {
            lifecycleScope.launch {
                try {
                    errorCard.visibility = View.GONE

                    if (!validateCredentials()) return@launch

                    AuthRepository.login(
                        etUsername.text.toString().trim(),
                        etPassword.text.toString()
                    )

                    updateUI()

                } catch (e: retrofit2.HttpException) {
                    when (e.code()) {
                        401, 404, 403 -> showError("Неверное имя пользователя или пароль")
                        500 -> showError("Ошибка сервера")
                        else -> showError("Ошибка (${e.code()})")
                    }
                } catch (e: Exception) {
                    showError("Ошибка соединения")
                }
            }
        }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(requireContext(), RegisterActivity::class.java))
        }

        btnLogout.setOnClickListener {
            AuthRepository.logout()
            updateUI()
        }

        btnOrders.setOnClickListener {
            val user = AuthRepository.currentUser

            when (user?.role) {
                "ADMIN" -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, AdminOrdersFragment())
                        .addToBackStack(null)
                        .commit()
                }

                "REVIEWER" -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, ReviewerOrdersFragment())
                        .addToBackStack(null)
                        .commit()
                }

                else -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, MyOrdersFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        btnManageUsers.setOnClickListener {
            val user = AuthRepository.currentUser

            when (user?.role) {
                "ADMIN" -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, AdminUsersFragment())
                        .addToBackStack(null)
                        .commit()
                }

                "REVIEWER" -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, MyReviewsFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        btnEditProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        btnSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun validateCredentials(): Boolean {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        return when {
            username.isEmpty() -> {
                showError("Введите имя пользователя")
                false
            }

            password.isEmpty() -> {
                showError("Введите пароль")
                false
            }

            else -> true
        }
    }

    private fun showError(message: String) {
        tvError.text = message
        errorCard.visibility = View.VISIBLE
    }
}