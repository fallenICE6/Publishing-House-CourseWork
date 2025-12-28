package com.example.publishingapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.publishingapp.R
import com.example.publishingapp.data.mock.MockUserRepository
import com.example.publishingapp.ui.activities.RegisterActivity

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val authContainer = view.findViewById<LinearLayout>(R.id.authContainer)
        val profileContainer = view.findViewById<LinearLayout>(R.id.profileContainer)
        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)

        view.findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val success = MockUserRepository.login(
                etUsername.text.toString(),
                etPassword.text.toString()
            )
            if (success) updateUI(authContainer, profileContainer, tvUsername)
            else Toast.makeText(context, "Неверные данные", Toast.LENGTH_SHORT).show()
        }

        // Перенаправляем на отдельную активность регистрации
        view.findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val intent = Intent(requireContext(), RegisterActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            MockUserRepository.logout()
            authContainer.visibility = View.VISIBLE
            profileContainer.visibility = View.GONE
        }

        if (MockUserRepository.isLoggedIn()) {
            updateUI(authContainer, profileContainer, tvUsername)
        }
    }

    private fun updateUI(
        authContainer: LinearLayout,
        profileContainer: LinearLayout,
        tvUsername: TextView
    ) {
        authContainer.visibility = View.GONE
        profileContainer.visibility = View.VISIBLE
        tvUsername.text = "@" + MockUserRepository.currentUser?.username
    }
}
