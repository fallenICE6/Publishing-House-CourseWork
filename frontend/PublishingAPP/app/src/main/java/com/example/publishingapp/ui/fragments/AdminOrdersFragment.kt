package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.OrderFullDto
import com.example.publishingapp.ui.adapters.AdminOrdersAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class AdminOrdersFragment : Fragment(R.layout.fragment_admin_orders) {

    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var emptyBlock: LinearLayout
    private lateinit var searchView: SearchView
    private lateinit var btnClear: MaterialButton
    private lateinit var hintContainer: LinearLayout
    private lateinit var statusFilterChipGroup: ChipGroup
    private lateinit var btnResetFilters: MaterialButton

    private var allOrders: List<OrderFullDto> = emptyList()
    private lateinit var adapter: AdminOrdersAdapter
    private val selectedStatuses = mutableSetOf<String>()

    private val allStatuses = listOf("Создан", "На проверке", "Редактируется", "Готов к печати", "Завершён", "Отменён")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerAdminOrders)
        progress = view.findViewById(R.id.progressAdmin)
        emptyBlock = view.findViewById(R.id.emptyBlockAdmin)
        searchView = view.findViewById(R.id.searchView)
        btnClear = view.findViewById(R.id.btnClear)
        hintContainer = view.findViewById(R.id.hintContainer)
        statusFilterChipGroup = view.findViewById(R.id.statusFilterChipGroup)
        btnResetFilters = view.findViewById(R.id.btnResetFilters)

        setupRecyclerView()
        setupSearch()
        setupStatusFilters()
        loadOrders()
    }

    private fun setupRecyclerView() {
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = AdminOrdersAdapter(
            onOrderClick = { order ->
                openOrderDetails(order)
            },
            onStatusChange = { order ->
                showStatusChangeDialog(order)
            }
        )
        recycler.adapter = adapter
    }

    private fun setupStatusFilters() {
        // Создаем чипы для каждого статуса
        allStatuses.forEach { status ->
            val chip = Chip(requireContext()).apply {
                text = status
                isCheckable = true
                isClickable = true
                chipBackgroundColor = androidx.core.content.ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.chip_background_selector
                )
                setChipIconResource(R.drawable.ic_check_circle)
                chipIconTint = androidx.core.content.ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.chip_icon_selector
                )
                setEnsureMinTouchTargetSize(false)
            }
            statusFilterChipGroup.addView(chip)

            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedStatuses.add(status)
                } else {
                    selectedStatuses.remove(status)
                }
                applySearch()
                btnResetFilters.isVisible = selectedStatuses.isNotEmpty()
            }
        }

        btnResetFilters.setOnClickListener {
            resetFilters()
        }
    }

    private fun resetFilters() {
        for (i in 0 until statusFilterChipGroup.childCount) {
            val chip = statusFilterChipGroup.getChildAt(i) as? Chip
            chip?.isChecked = false
        }
        selectedStatuses.clear()
        btnResetFilters.isVisible = false
        applySearch()
    }

    private fun setupSearch() {
        searchView.queryHint = "Номер заказа или ФИО клиента"

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            hintContainer.isVisible = hasFocus && searchView.query.isNullOrEmpty()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                applySearch()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                btnClear.isVisible = !newText.isNullOrEmpty()
                hintContainer.isVisible = newText.isNullOrEmpty() && searchView.hasFocus()

                if (!newText.isNullOrEmpty() && newText.length >= 2) {
                    lifecycleScope.launch {
                        delay(500)
                        if (searchView.query.toString() == newText) {
                            applySearch()
                        }
                    }
                } else if (newText.isNullOrEmpty()) {
                    applySearch()
                }

                return true
            }
        })

        btnClear.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            hintContainer.isVisible = false
            applySearch()
        }
    }

    private fun loadOrders() {
        progress.isVisible = true
        emptyBlock.isVisible = false

        lifecycleScope.launch {
            try {
                allOrders = ApiClient.apiService.getAllOrders()
                progress.isVisible = false

                if (allOrders.isEmpty()) {
                    showEmptyState("Заказов не найдено", "В системе пока нет заказов")
                } else {
                    applySearch()
                }
            } catch (e: Exception) {
                progress.isVisible = false
                showEmptyState("Ошибка загрузки", "Не удалось загрузить список заказов")
                Toast.makeText(requireContext(), "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applySearch() {
        val searchQuery = searchView.query.toString().trim()


        val filteredOrders = allOrders.filter { order ->

            val statusMatch = if (selectedStatuses.isEmpty()) {
                true
            } else {
                selectedStatuses.contains(order.status)
            }


            val searchMatch = if (searchQuery.isEmpty()) {
                true
            } else {
                order.id.toString().contains(searchQuery, ignoreCase = true) ||
                        order.fullName.contains(searchQuery, ignoreCase = true)
            }

            statusMatch && searchMatch
        }

        progress.isVisible = false

        if (filteredOrders.isEmpty()) {
            showEmptyState(
                "Заказов не найдено",
                if (selectedStatuses.isNotEmpty() && searchQuery.isNotEmpty()) {
                    "По запросу \"$searchQuery\" и выбранным статусам ничего не найдено"
                } else if (selectedStatuses.isNotEmpty()) {
                    "Заказы с выбранными статусами не найдены"
                } else if (searchQuery.isNotEmpty()) {
                    "По запросу \"$searchQuery\" ничего не найдено"
                } else {
                    "Введите номер заказа или ФИО клиента"
                }
            )
        } else {
            emptyBlock.isVisible = false
            recycler.isVisible = true
            adapter.submitList(filteredOrders)
        }
    }

    private fun showEmptyState(title: String, subtitle: String) {
        emptyBlock.isVisible = true
        recycler.isVisible = false

        emptyBlock.findViewById<TextView>(R.id.tvEmptyTitleAdmin).text = title
        emptyBlock.findViewById<TextView>(R.id.tvEmptySubtitleAdmin).text = subtitle
    }

    private fun openOrderDetails(order: OrderFullDto) {
        val fragment = AdminOrderDetailsFragment.newInstance(order.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun showStatusChangeDialog(order: OrderFullDto) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_status, null)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = "Изменение статуса заказа №${order.id}"

        val currentStatus = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.tvCurrentStatus)
        val statusSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerNewStatus)

        currentStatus.text = order.status

        val availableStatuses = getAvailableStatuses(order.status)

        if (availableStatuses.isEmpty()) {
            Toast.makeText(requireContext(), "Этот заказ нельзя изменить", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            availableStatuses
        )
        statusSpinner.setAdapter(adapter)

        if (availableStatuses.isNotEmpty()) {
            statusSpinner.setText(availableStatuses[0], false)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.show()

        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)
        val btnChangeStatus = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnChangeStatus)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnChangeStatus.setOnClickListener {
            val newStatus = statusSpinner.text.toString()
            if (newStatus.isNotEmpty()) {
                updateOrderStatus(order.id, newStatus)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Выберите статус", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAvailableStatuses(currentStatus: String): List<String> {
        return when (currentStatus.lowercase()) {
            "создан" -> listOf("На проверке")
            "на проверке" -> listOf("Редактируется", "Готов к печати", "Отменён")
            "редактируется" -> listOf("На проверке")
            "готов к печати" -> listOf("Завершён")
            "завершён", "отменён" -> emptyList()
            else -> emptyList()
        }
    }

    private fun updateOrderStatus(orderId: Long, newStatus: String) {
        progress.isVisible = true

        lifecycleScope.launch {
            try {
                val request = com.example.publishingapp.data.network.UpdateOrderStatusRequest(
                    status = mapRussianToEnglishStatus(newStatus)
                )

                val updatedOrder = ApiClient.apiService.updateOrderStatus(orderId, request)

                Toast.makeText(
                    requireContext(),
                    "Статус заказа №$orderId изменен на \"$newStatus\"",
                    Toast.LENGTH_SHORT
                ).show()

                loadOrders()

            } catch (e: Exception) {
                progress.isVisible = false
                val errorMessage = when {
                    e.message?.contains("Неизвестный статус") == true -> "Неизвестный статус"
                    e.message?.contains("Из статуса") == true -> e.message
                    e.message?.contains("Завершённые") == true -> "Завершённые и отменённые заказы нельзя изменять"
                    else -> "Ошибка: ${e.message}"
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mapRussianToEnglishStatus(russianStatus: String): String {
        return when (russianStatus.lowercase()) {
            "создан" -> "created"
            "на проверке" -> "under_review"
            "редактируется" -> "editing"
            "готов к печати" -> "ready_for_print"
            "завершён" -> "completed"
            "отменён" -> "canceled"
            else -> russianStatus
        }
    }
}