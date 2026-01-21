package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.UserDto
import com.example.publishingapp.ui.adapters.UserManagementAdapter
import com.example.publishingapp.ui.viewmodels.AdminUsersViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminUsersFragment : Fragment(R.layout.fragment_admin_users) {

    private val viewModel: AdminUsersViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateView: View
    private lateinit var emptyStateText: TextView
    private lateinit var errorView: View
    private lateinit var btnBack: MaterialButton

    private val userAdapter = UserManagementAdapter(onUserClick = { user -> showChangeRoleDialog(user) })
    private var searchJob: Job? = null
    private var currentSearchQuery: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupSearch()
        setupObservers()

        // Загрузить первых пользователей
        viewModel.loadInitialUsers()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        searchEditText = view.findViewById(R.id.etSearch)
        progressBar = view.findViewById(R.id.progressBar)
        emptyStateView = view.findViewById(R.id.emptyStateView)
        emptyStateText = emptyStateView.findViewById<TextView>(R.id.tvEmptyStateText)
        errorView = view.findViewById(R.id.errorView)
        btnBack = view.findViewById(R.id.btnBack)

        // Установка цвета текста для поиска
        searchEditText.setTextColor(resources.getColor(android.R.color.black, null))
        searchEditText.setHintTextColor(resources.getColor(android.R.color.darker_gray, null))

        btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Кнопка повторной загрузки при ошибке
        errorView.findViewById<MaterialButton>(R.id.btnRetry).setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = userAdapter

        // Добавляем разделители между элементами
        recyclerView.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )

        // Слушатель прокрутки для подгрузки данных
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (currentSearchQuery.isEmpty()) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (lastVisibleItemPosition >= totalItemCount - 3) {
                        viewModel.loadMoreUsers()
                    }
                }
            }
        })
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(500)

                    val query = s.toString().trim()
                    currentSearchQuery = query

                    if (query.isNotEmpty()) {
                        viewModel.searchUsers(query)
                    } else {
                        viewModel.loadInitialUsers(null)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collect { users ->
                userAdapter.submitList(users)

                if (currentSearchQuery.isNotEmpty()) {
                    emptyStateText.text = "Нет пользователей с username: \"$currentSearchQuery\""
                } else {
                    emptyStateText.text = "Пользователи не найдены"
                }

                emptyStateView.isVisible = users.isEmpty() && !viewModel.isLoading.value
                recyclerView.isVisible = users.isNotEmpty()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                progressBar.isVisible = isLoading
                userAdapter.setLoading(isLoading && currentSearchQuery.isEmpty())

                if (isLoading) {
                    emptyStateView.isVisible = false
                    errorView.isVisible = false
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (error != null) {
                    errorView.isVisible = true
                    recyclerView.isVisible = false
                    emptyStateView.isVisible = false
                    errorView.findViewById<TextView>(R.id.tvError).text = error
                } else {
                    errorView.isVisible = false
                }
            }
        }

        // ДОБАВЛЯЕМ observer для Toast сообщений
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toastMessage.collect { message ->
                message?.let {
                    // Показываем Toast
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    // Очищаем сообщение после показа
                    viewModel.clearToastMessage()
                }
            }
        }
    }

    private fun showChangeRoleDialog(user: UserDto) {
        val availableRoles = listOf("AUTHOR", "ADMIN", "REVIEWER")
            .filter { it != user.role }

        val roleDisplayNames = availableRoles.map { getRoleDisplayName(it) }.toTypedArray()

        if (availableRoles.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Нет доступных ролей для изменения",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        var selectedRoleIndex = 0
        var selectedRole = availableRoles[selectedRoleIndex]

        val dialogView = layoutInflater.inflate(R.layout.dialog_change_role, null)

        dialogView.findViewById<TextView>(R.id.tvUsername).text = "@${user.username}"
        dialogView.findViewById<TextView>(R.id.tvFullName).text = "${user.lastName} ${user.firstName}"
        dialogView.findViewById<TextView>(R.id.tvCurrentRole).text = "Текущая роль: ${getRoleDisplayName(user.role)}"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Изменить роль пользователя")
            .setView(dialogView)
            .setSingleChoiceItems(roleDisplayNames, selectedRoleIndex) { _, which ->
                selectedRoleIndex = which
                selectedRole = availableRoles[which]
            }
            .setPositiveButton("Сохранить") { dialog, _ ->
                // Вызываем метод БЕЗ callback
                viewModel.changeUserRole(user.id, selectedRole)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getRoleDisplayName(role: String): String {
        return when (role) {
            "ADMIN" -> "Администратор"
            "AUTHOR" -> "Автор"
            "REVIEWER" -> "Рецензент"
            else -> role
        }
    }
}