package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.ui.adapters.EditorOrdersAdapter
import kotlinx.coroutines.launch

class EditorOrdersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: View
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView
    private lateinit var adapter: EditorOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_editor_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerEditorOrders)
        progressBar = view.findViewById(R.id.progressEditor)
        emptyView = view.findViewById(R.id.emptyBlockEditor)
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitleEditor)
        tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitleEditor)

        adapter = EditorOrdersAdapter { order ->
            openOrderDetails(order.id)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadOrders()
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    private fun loadOrders() {
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val orders = ApiClient.apiService.getEditingOrdersForEditor()

                if (orders.isEmpty()) {
                    showEmptyState()
                } else {
                    adapter.submitList(orders)
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                }
            } catch (e: Exception) {
                showEmptyState()
                Toast.makeText(requireContext(), "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }

    private fun openOrderDetails(orderId: Long) {
        val fragment = EditorOrderDetailsFragment.newInstance(orderId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }
}