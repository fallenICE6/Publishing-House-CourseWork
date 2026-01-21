// ReviewerOrdersFragment.kt
package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.OrderFullDto
import com.example.publishingapp.ui.adapters.ReviewerOrdersAdapter
import kotlinx.coroutines.launch

class ReviewerOrdersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: View
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView

    private val adapter = ReviewerOrdersAdapter(
        onOrderClick = { order ->
            openOrderDetails(order.id)
        },
        onWriteReview = { orderId ->
            openWriteReview(orderId)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reviewer_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerReviewerOrders)
        progressBar = view.findViewById(R.id.progressReviewer)
        emptyView = view.findViewById(R.id.emptyBlockReviewer)
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitleReviewer)
        tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitleReviewer)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val orders = ApiClient.apiService.getOrdersForReview()

                if (orders.isEmpty()) {
                    showEmptyState("Нет заказов для рецензирования",
                        "Все заказы проверены или еще не поступили на рецензию")
                } else {
                    adapter.submitList(orders)
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                }
            } catch (e: Exception) {
                showEmptyState("Ошибка загрузки", "Проверьте подключение к интернету")
                e.printStackTrace()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showEmptyState(title: String, subtitle: String) {
        tvEmptyTitle.text = title
        tvEmptySubtitle.text = subtitle
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }

    private fun openOrderDetails(orderId: Long) {
        val fragment = ReviewerOrderDetailsFragment.newInstance(orderId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openWriteReview(orderId: Long) {
        val fragment = WriteReviewFragment.newInstance(orderId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }
}