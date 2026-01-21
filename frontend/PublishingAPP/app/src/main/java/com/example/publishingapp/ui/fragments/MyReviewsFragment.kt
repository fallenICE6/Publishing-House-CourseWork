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
import com.example.publishingapp.data.network.ReviewDto
import com.example.publishingapp.ui.adapters.MyReviewsAdapter
import com.google.gson.Gson
import kotlinx.coroutines.launch

class MyReviewsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: View
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView

    private val adapter = MyReviewsAdapter(
        onReviewClick = { review ->
            openReviewDetails(review)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerMyReviews)
        progressBar = view.findViewById(R.id.progressMyReviews)
        emptyView = view.findViewById(R.id.emptyBlockMyReviews)
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitleMyReviews)
        tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitleMyReviews)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadReviews()
    }

    private fun loadReviews() {
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val reviews = ApiClient.apiService.getMyReviews()

                if (reviews.isEmpty()) {
                    showEmptyState("Нет ваших рецензий",
                        "Вы еще не написали ни одной рецензии")
                } else {
                    adapter.submitList(reviews)
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

    private fun openReviewDetails(review: ReviewDto) {
        val reviewJson = Gson().toJson(review)
        val fragment = ReviewDetailsFragment.newInstance(reviewJson)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }
}