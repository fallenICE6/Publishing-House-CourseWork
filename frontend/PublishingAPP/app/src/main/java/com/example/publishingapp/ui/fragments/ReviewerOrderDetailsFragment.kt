package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.publishingapp.R
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.OrderFullDto
import com.example.publishingapp.ui.adapters.FileAdapter
import com.example.publishingapp.ui.adapters.MaterialAdapter
import com.example.publishingapp.utils.FileDownloader
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewerOrderDetailsFragment : Fragment() {

    companion object {
        private const val ARG_ORDER_ID = "order_id"

        fun newInstance(orderId: Long): ReviewerOrderDetailsFragment {
            return ReviewerOrderDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ORDER_ID, orderId)
                }
            }
        }
    }

    private var order: OrderFullDto? = null
    private lateinit var btnWriteReview: MaterialButton
    private lateinit var btnViewReview: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_order_details_reviewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnWriteReview = view.findViewById(R.id.btnWriteReview)
        btnViewReview = view.findViewById(R.id.btnViewReview)

        val orderId = arguments?.getLong(ARG_ORDER_ID) ?: -1L
        if (orderId == -1L) {
            Toast.makeText(requireContext(), "Ошибка загрузки заказа", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return
        }

        btnWriteReview.setOnClickListener {
            openWriteReview(orderId)
        }

        btnViewReview.setOnClickListener {
            order?.review?.let { review ->
                openReviewDetails(review)
            }
        }

        loadOrder(orderId)
    }

    private fun loadOrder(orderId: Long) {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                order = ApiClient.apiService.getOrderDetailsForReview(orderId)

                if (order != null) {
                    updateUI(order!!)
                    updateButtons(order!!)
                } else {
                    Toast.makeText(requireContext(), "Заказ не найден", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки заказа: ${e.message}", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            } finally {
                view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
            }
        }
    }

    private fun updateButtons(order: OrderFullDto) {
        val hasReview = order.review != null

        if (hasReview) {
            btnWriteReview.visibility = View.GONE
            btnViewReview.visibility = View.VISIBLE
            btnViewReview.text = "Просмотреть рецензию"
        } else {
            btnWriteReview.visibility = View.VISIBLE
            btnViewReview.visibility = View.GONE
            btnWriteReview.text = "Написать рецензию"
        }
    }

    private fun updateUI(order: OrderFullDto) {
        // Номер заказа и дата
        view?.findViewById<android.widget.TextView>(R.id.tvOrderNumber)?.text = "Заказ №${order.id}"

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = try {
            dateFormat.format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .parse(order.createdAt))
        } catch (e: Exception) {
            order.createdAt
        }
        view?.findViewById<android.widget.TextView>(R.id.tvOrderDate)?.text = "от $date"

        // Статус
        val statusView = view?.findViewById<android.widget.TextView>(R.id.tvStatus)
        statusView?.text = order.status

        // Цвет статуса
        val statusBackground = when (order.status.lowercase()) {
            "создан" -> R.drawable.status_background_created
            "на проверке" -> R.drawable.status_background_under_review
            "редактируется" -> R.drawable.status_background_editing
            "готов к печати" -> R.drawable.status_background_ready_for_print
            "завершён" -> R.drawable.status_background_completed
            "отменён" -> R.drawable.status_background_canceled
            else -> R.drawable.status_background_created
        }
        statusView?.setBackgroundResource(statusBackground)

        // Клиент
        view?.findViewById<android.widget.TextView>(R.id.tvFullName)?.text = order.fullName
        view?.findViewById<android.widget.TextView>(R.id.tvEmail)?.text = order.email ?: "—"
        view?.findViewById<android.widget.TextView>(R.id.tvPhone)?.text = order.phone ?: "—"

        // Услуга и параметры
        view?.findViewById<android.widget.TextView>(R.id.tvServiceTitle)?.text = order.serviceTitle

        val pagesText = when {
            order.pages == null || order.pages == 0 -> "—"
            else -> order.pages.toString()
        }
        view?.findViewById<android.widget.TextView>(R.id.tvPages)?.text = pagesText

        val quantityText = when {
            order.quantity == null || order.quantity == 0 -> "—"
            else -> order.quantity.toString()
        }
        view?.findViewById<android.widget.TextView>(R.id.tvQuantity)?.text = quantityText

        // Материалы
        setupMaterials(order.materials)

        // Файлы
        setupFiles(order.files)

        // Рецензия
        setupReview(order.review)

        // Итоговая стоимость
        view?.findViewById<android.widget.TextView>(R.id.tvTotalPrice)?.text =
            "${order.totalPrice} ₽"
    }

    private fun setupMaterials(materials: List<com.example.publishingapp.data.network.OrderMaterialDto>) {
        val recycler = view?.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerMaterials)
        val noMaterials = view?.findViewById<android.widget.TextView>(R.id.tvNoMaterials)
        val card = view?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardMaterials)

        if (materials.isNotEmpty()) {
            noMaterials?.visibility = View.GONE
            recycler?.visibility = View.VISIBLE
            card?.visibility = View.VISIBLE

            recycler?.layoutManager = LinearLayoutManager(requireContext())
            recycler?.adapter = MaterialAdapter(materials)
        } else {
            noMaterials?.visibility = View.VISIBLE
            recycler?.visibility = View.GONE
            card?.visibility = View.GONE
        }
    }

    private fun setupFiles(files: List<com.example.publishingapp.data.network.OrderFileDto>) {
        val recycler = view?.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerFiles)
        val noFiles = view?.findViewById<android.widget.TextView>(R.id.tvNoFiles)
        val card = view?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardFiles)

        if (files.isNotEmpty()) {
            noFiles?.visibility = View.GONE
            recycler?.visibility = View.VISIBLE
            card?.visibility = View.VISIBLE

            recycler?.layoutManager = LinearLayoutManager(requireContext())
            recycler?.adapter = FileAdapter(files) { file ->
                downloadFile(file)
            }
        } else {
            noFiles?.visibility = View.VISIBLE
            recycler?.visibility = View.GONE
            card?.visibility = View.GONE
        }
    }

    private fun setupReview(review: com.example.publishingapp.data.network.ReviewDto?) {
        val card = view?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardReview)
        val noReview = view?.findViewById<android.widget.TextView>(R.id.tvNoReview)
        val status = view?.findViewById<android.widget.TextView>(R.id.tvReviewStatus)
        val comment = view?.findViewById<android.widget.TextView>(R.id.tvReviewComment)
        val decision = view?.findViewById<android.widget.TextView>(R.id.tvReviewDecision)

        if (review != null) {
            card?.visibility = View.VISIBLE
            noReview?.visibility = View.GONE
            status?.visibility = View.VISIBLE
            comment?.visibility = View.VISIBLE
            decision?.visibility = View.VISIBLE

            // Показываем решение
            val decisionText = when (review.status.lowercase()) {
                "approved", "одобрена" -> "Одобрена"
                "rejected", "отклонена" -> "Отклонена"
                "pending", "на рассмотрении" -> "На доработку"
                else -> review.status ?: "—"
            }

            decision?.text = decisionText
            comment?.text = review.comment ?: "Без комментария"

            val orderStatusText = when (review.orderStatusAfterReview?.lowercase()) {
                "editing" -> "Заказ отправлен на доработку"
                "ready_for_print" -> "Заказ одобрен для печати"
                "canceled" -> "Заказ отклонён"
                else -> "Статус обновлён"
            }

            status?.text = orderStatusText

        } else {
            card?.visibility = View.GONE
        }
    }

    private fun downloadFile(file: com.example.publishingapp.data.network.OrderFileDto) {
        lifecycleScope.launch {
            try {
                Toast.makeText(
                    requireContext(),
                    "Начинаем скачивание ${file.fileName}...",
                    Toast.LENGTH_SHORT
                ).show()

                val responseBody = ApiClient.apiService.downloadFile(file.id)

                val success = FileDownloader.downloadFile(
                    context = requireContext(),
                    responseBody = responseBody,
                    fileName = file.fileName
                )

                if (success) {
                    Toast.makeText(
                        requireContext(),
                        "Файл успешно сохранен в папку приложения",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка скачивания: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openWriteReview(orderId: Long) {
        val fragment = WriteReviewFragment.newInstance(orderId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openReviewDetails(review: com.example.publishingapp.data.network.ReviewDto) {
        val reviewJson = Gson().toJson(review)
        val fragment = ReviewDetailsFragment.newInstance(reviewJson)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }
}