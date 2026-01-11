package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.models.OrderStatus
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.OrderFullDto
import com.example.publishingapp.ui.adapters.MyOrdersAdapter
import kotlinx.coroutines.launch

class MyOrdersFragment : Fragment(R.layout.fragment_my_orders) {

    private lateinit var recycler: RecyclerView
    private lateinit var progress: View
    private lateinit var emptyBlock: View
    private lateinit var statusFilter: Spinner

    private var allOrders: List<OrderFullDto> = emptyList()
    private var currentFilter = OrderStatus.ALL
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    // Сохраняем состояние фильтра
    companion object {
        private const val KEY_CURRENT_FILTER = "current_filter"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerOrders)
        progress = view.findViewById(R.id.progress)
        emptyBlock = view.findViewById(R.id.emptyBlock)
        statusFilter = view.findViewById(R.id.statusFilter)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Восстанавливаем фильтр если есть сохраненное состояние
        savedInstanceState?.getString(KEY_CURRENT_FILTER)?.let {
            currentFilter = try {
                OrderStatus.valueOf(it)
            } catch (e: IllegalArgumentException) {
                OrderStatus.ALL
            }
        }

        setupStatusSpinner()
        loadOrders()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_FILTER, currentFilter.name)
    }

    private fun setupStatusSpinner() {
        val statuses = OrderStatus.values().map { it.title }

        spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statuses
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        statusFilter.adapter = spinnerAdapter

        // Устанавливаем текущую позицию
        val position = OrderStatus.values().indexOf(currentFilter)
        if (position >= 0) {
            statusFilter.setSelection(position, false)
        }

        statusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = OrderStatus.values()[position]
                if (currentFilter != selected) {
                    currentFilter = selected
                    applyFilter(selected)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Ничего не делаем
            }
        }
    }

    private fun loadOrders() {
        progress.visibility = View.VISIBLE
        emptyBlock.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                allOrders = ApiClient.apiService.getMyOrders()
                progress.visibility = View.GONE

                // Применяем текущий фильтр
                applyFilter(currentFilter)

            } catch (e: Exception) {
                progress.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Ошибка загрузки заказов",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun applyFilter(status: OrderStatus) {
        currentFilter = status

        // Обновляем позицию в спиннере если нужно
        val position = OrderStatus.values().indexOf(status)
        if (position >= 0 && statusFilter.selectedItemPosition != position) {
            statusFilter.setSelection(position, false)
        }

        val filtered = if (status == OrderStatus.ALL) {
            allOrders
        } else {
            allOrders.filter { it.status == status.title }
        }

        updateUI(filtered, status)
    }

    private fun updateUI(filtered: List<OrderFullDto>, status: OrderStatus) {
        if (filtered.isEmpty()) {
            recycler.visibility = View.GONE
            emptyBlock.visibility = View.VISIBLE

            val title = emptyBlock.findViewById<TextView>(R.id.tvEmptyTitle)
            val subtitle = emptyBlock.findViewById<TextView>(R.id.tvEmptySubtitle)

            if (allOrders.isEmpty()) {
                title.text = "У вас пока нет заказов"
                subtitle.text = "Перейдите в раздел услуг и оформите первый заказ"
            } else {
                title.text = "Заказов не найдено"
                subtitle.text = "Нет заказов со статусом «${status.title}»"
            }

        } else {
            recycler.visibility = View.VISIBLE
            emptyBlock.visibility = View.GONE

            recycler.adapter = MyOrdersAdapter(filtered) { order ->
                openOrder(order)
            }
        }
    }

    private fun openOrder(order: OrderFullDto) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, OrderDetailsFragment.newInstance(order.id))
            .addToBackStack(null)
            .commit()
    }

    // При возврате на фрагмент восстанавливаем состояние
    override fun onResume() {
        super.onResume()

        // Убедимся, что спиннер показывает правильный статус
        val position = OrderStatus.values().indexOf(currentFilter)
        if (position >= 0 && statusFilter.selectedItemPosition != position) {
            statusFilter.setSelection(position, false)
        }
    }

    // Сохраняем текущий фильтр при паузе
    override fun onPause() {
        super.onPause()
        val selectedPosition = statusFilter.selectedItemPosition
        if (selectedPosition >= 0 && selectedPosition < OrderStatus.values().size) {
            currentFilter = OrderStatus.values()[selectedPosition]
        }
    }
}