package com.example.publishingapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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

    private lateinit var authContainer: MaterialCardView
    private lateinit var profileContainer: View

    private lateinit var btnOrders: MaterialButton


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authContainer = view.findViewById(R.id.authContainer)
        profileContainer = view.findViewById(R.id.profileContainer)
        btnOrders = view.findViewById(R.id.btnOrders)

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

        setupListeners()

        if (AuthRepository.isLoggedIn()) {
            showProfile()
        }
    }

    private fun setupListeners() {

        btnLogin.setOnClickListener {
            lifecycleScope.launch {
                try {
                    AuthRepository.login(
                        etUsername.text.toString(),
                        etPassword.text.toString()
                    )
                    showProfile()
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Неверный логин или пароль",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(requireContext(), RegisterActivity::class.java))
        }

        btnLogout.setOnClickListener {
            AuthRepository.logout()
            authContainer.visibility = View.VISIBLE
            profileContainer.visibility = View.GONE
        }
        btnOrders.setOnClickListener {
            val user = AuthRepository.currentUser
            if (user?.role == "ADMIN") {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, AdminOrdersFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, MyOrdersFragment())
                    .addToBackStack(null)
                    .commit()
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

    override fun onResume() {
        super.onResume()
        if (AuthRepository.isLoggedIn()) {
            showProfile()
        }
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
            }
            "AUTHOR" -> {
                chipRole.text = "Автор"
                btnOrders.text = "Мои заказы"
                btnOrders.setIconResource(R.drawable.ic_orders)
            }
            "REVIEWER" -> {
                chipRole.text = "Рецензент"
                btnOrders.text = "Ожидают рецензии"
                btnOrders.setIconResource(R.drawable.ic_orders)
            }
            else -> {
                chipRole.text = "Пользователь"
                btnOrders.text = "Мои заказы"
                btnOrders.setIconResource(R.drawable.ic_orders)
            }
        }
    }
}
