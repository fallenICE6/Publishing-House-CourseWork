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

    private var allOrders: List<OrderFullDto> = emptyList()
    private lateinit var adapter: AdminOrdersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerAdminOrders)
        progress = view.findViewById(R.id.progressAdmin)
        emptyBlock = view.findViewById(R.id.emptyBlockAdmin)
        searchView = view.findViewById(R.id.searchView)
        btnClear = view.findViewById(R.id.btnClear)
        hintContainer = view.findViewById(R.id.hintContainer)

        setupRecyclerView()
        setupSearch()
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

    private fun setupSearch() {
        // Настраиваем SearchView
        searchView.queryHint = "Номер заказа или ФИО клиента"

        // Показываем подсказку при фокусе
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            hintContainer.isVisible = hasFocus && searchView.query.isNullOrEmpty()
        }

        // Обработка ввода текста
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                applySearch()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Показываем/скрываем кнопку очистки
                btnClear.isVisible = !newText.isNullOrEmpty()
                // Показываем/скрываем подсказку
                hintContainer.isVisible = newText.isNullOrEmpty() && searchView.hasFocus()

                // Автоматический поиск с задержкой (по желанию)
                if (!newText.isNullOrEmpty() && newText.length >= 2) {
                    lifecycleScope.launch {
                        delay(500) // Задержка 500ms для предотвращения частых запросов
                        if (searchView.query.toString() == newText) {
                            applySearch()
                        }
                    }
                } else if (newText.isNullOrEmpty()) {
                    applySearch() // Показываем все заказы при очистке
                }

                return true
            }
        })

        // Кнопка очистки
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
                // Загружаем все заказы без фильтров
                allOrders = ApiClient.apiService.getAllOrders()
                progress.isVisible = false

                if (allOrders.isEmpty()) {
                    showEmptyState("Заказов не найдено", "В системе пока нет заказов")
                } else {
                    adapter.submitList(allOrders)
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

        progress.isVisible = true

        lifecycleScope.launch {
            try {
                val filteredOrders = if (searchQuery.isEmpty()) {
                    // Перезагружаем все заказы с сервера
                    ApiClient.apiService.getAllOrders()
                } else {
                    ApiClient.apiService.getAllOrders(search = searchQuery)
                }

                // Обновляем локальный список
                allOrders = filteredOrders

                progress.isVisible = false

                if (filteredOrders.isEmpty()) {
                    showEmptyState("Заказов не найдено", "Попробуйте изменить параметры поиска")
                    emptyBlock.findViewById<TextView>(R.id.tvEmptySubtitleAdmin).text =
                        if (searchQuery.isNotEmpty()) {
                            "По запросу \"$searchQuery\" ничего не найдено"
                        } else {
                            "Введите номер заказа или ФИО клиента"
                        }
                } else {
                    emptyBlock.isVisible = false
                    recycler.isVisible = true
                    adapter.submitList(filteredOrders)
                }
            } catch (e: Exception) {
                progress.isVisible = false
                Toast.makeText(requireContext(), "Ошибка поиска", Toast.LENGTH_SHORT).show()
            }
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

        val currentStatus = dialogView.findViewById<TextView>(R.id.tvCurrentStatus)
        val statusSpinner = dialogView.findViewById<Spinner>(R.id.spinnerNewStatus)

        currentStatus.text = "Текущий статус: ${order.status}"

        // Определяем доступные статусы в зависимости от текущего
        val availableStatuses = getAvailableStatuses(order.status)

        if (availableStatuses.isEmpty()) {
            Toast.makeText(requireContext(), "Этот заказ нельзя изменить", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, availableStatuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = adapter

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Изменение статуса заказа №${order.id}")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val newStatus = statusSpinner.selectedItem.toString()
                updateOrderStatus(order.id, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
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

                // Отправляем запрос на изменение статуса
                val updatedOrder = ApiClient.apiService.updateOrderStatus(orderId, request)

                // ПОСЛЕ успешного обновления показываем уведомление
                Toast.makeText(
                    requireContext(),
                    "Статус заказа №$orderId изменен на \"$newStatus\"",
                    Toast.LENGTH_SHORT
                ).show()

                // Обновляем список заказов
                applySearch()

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