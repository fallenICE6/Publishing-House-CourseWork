package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewDetailsFragment : Fragment() {

    companion object {
        private const val ARG_REVIEW_JSON = "review_json"

        fun newInstance(reviewJson: String): ReviewDetailsFragment {
            return ReviewDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_REVIEW_JSON, reviewJson)
                }
            }
        }
    }

    private var order: OrderFullDto? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_review_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reviewJson = arguments?.getString(ARG_REVIEW_JSON) ?: return
        val review = Gson().fromJson(reviewJson, com.example.publishingapp.data.network.ReviewDto::class.java)

        // Сначала показываем данные рецензии
        updateReviewUI(review)

        // Затем загружаем и показываем данные заказа используя orderId из рецензии
        loadOrderDetails(review.orderId) // Передаем orderId, а не reviewId
    }

    private fun updateReviewUI(review: com.example.publishingapp.data.network.ReviewDto) {
        // Номер заказа из рецензии
        view?.findViewById<TextView>(R.id.tvOrderNumber)?.text = "Заказ №${review.orderId}"

        // Дата рецензии
        val reviewDate = try {
            review.createdAt.substring(0, 10)
        } catch (e: Exception) {
            review.createdAt
        }
        view?.findViewById<TextView>(R.id.tvReviewDate)?.text = reviewDate

        // Рецензент
        view?.findViewById<TextView>(R.id.tvReviewerName)?.text =
            review.reviewerName ?: "Не указан"

        // Комментарий
        view?.findViewById<TextView>(R.id.tvComment)?.text =
            review.comment ?: "Без комментария"

        // Решение
        val decisionText = when (review.status?.lowercase()) {
            "approved", "одобрена", "одобрено" -> "Одобрено"
            "rejected", "отклонена", "отклонено" -> "Отклонено"
            "pending", "на рассмотрении", "на доработку" -> "На доработку"
            else -> review.status ?: "Неизвестно"
        }

        val decisionColorRes = when (review.status?.lowercase()) {
            "approved", "одобрена", "одобрено" -> android.R.color.holo_green_dark
            "rejected", "отклонена", "отклонено" -> android.R.color.holo_red_dark
            "pending", "на рассмотрении", "на доработку" -> android.R.color.holo_orange_dark
            else -> android.R.color.darker_gray
        }

        view?.findViewById<TextView>(R.id.tvDecision)?.apply {
            text = "Решение: $decisionText"
            setTextColor(context.resources.getColor(decisionColorRes, context.theme))
        }

        // Статус заказа
        val (orderStatusText, statusBackgroundRes) = when (review.status.lowercase()) {
            "approved", "одобрена" -> Pair("Готов к печати", R.drawable.status_background_ready_for_print)
            "rejected", "отклонена" -> Pair("Отменён", R.drawable.status_background_canceled)
            "pending", "на рассмотрении" -> Pair("Редактируется", R.drawable.status_background_editing)
            else -> Pair("Не указан", R.drawable.status_background_created)
        }

        view?.findViewById<TextView>(R.id.tvStatus)?.apply {
            text = "Статус заказа: $orderStatusText"
            setBackgroundResource(statusBackgroundRes)
        }


        view?.findViewById<TextView>(R.id.tvStatus)?.apply {
            text = "Статус заказа: $orderStatusText"
            setBackgroundResource(statusBackgroundRes)
        }
    }

    private fun loadOrderDetails(orderId: Long) {
        lifecycleScope.launch {
            try {
                // Используем существующий endpoint для рецензентов
                order = ApiClient.apiService.getOrderDetailsForReview(orderId)
                order?.let { updateOrderUI(it) }

            } catch (e: Exception) {
                // Если не получается, пробуем другие способы
                try {
                    // Пробуем получить через админский endpoint
                    order = ApiClient.apiService.getOrderByIdAdmin(orderId)
                    order?.let { updateOrderUI(it) }
                } catch (e2: Exception) {
                    // Логируем ошибку
                    println("Ошибка загрузки заказа: ${e.message}")
                    e.printStackTrace()

                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка загрузки данных заказа",
                            Toast.LENGTH_LONG
                        ).show()
                        view?.findViewById<TextView>(R.id.tvServiceTitle)?.text = "Ошибка загрузки"
                    }
                }
            }
        }
    }

    private fun updateOrderUI(order: OrderFullDto) {
        // Дата создания заказа
        val orderDate = try {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .parse(order.createdAt)
            )
        } catch (e: Exception) {
            order.createdAt
        }
        view?.findViewById<TextView>(R.id.tvOrderDate)?.text = "от $orderDate"

        // Клиент
        view?.findViewById<TextView>(R.id.tvFullName)?.text = order.fullName ?: "Не указан"
        view?.findViewById<TextView>(R.id.tvEmail)?.text = order.email ?: "—"
        view?.findViewById<TextView>(R.id.tvPhone)?.text = order.phone ?: "—"

        // Услуга
        view?.findViewById<TextView>(R.id.tvServiceTitle)?.text = order.serviceTitle ?: "Не указана"

        // Параметры
        val pagesText = when {
            order.pages == null || order.pages == 0 -> "—"
            else -> order.pages.toString()
        }
        view?.findViewById<TextView>(R.id.tvPages)?.text = pagesText

        val quantityText = when {
            order.quantity == null || order.quantity == 0 -> "—"
            else -> "${order.quantity}"
        }
        view?.findViewById<TextView>(R.id.tvQuantity)?.text = quantityText

        // Материалы (если есть)
        setupMaterials(order.materials)

        // Файлы (если есть)
        setupFiles(order.files)

        // Стоимость
        view?.findViewById<TextView>(R.id.tvTotalPrice)?.text = "${order.totalPrice} ₽"
    }

    private fun setupMaterials(materials: List<com.example.publishingapp.data.network.OrderMaterialDto>) {
        val recycler = view?.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerMaterials)
        val noMaterials = view?.findViewById<TextView>(R.id.tvNoMaterials)
        val card = view?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardMaterials)

        if (materials.isNotEmpty()) {
            card?.visibility = View.VISIBLE
            noMaterials?.visibility = View.GONE
            recycler?.visibility = View.VISIBLE

            recycler?.layoutManager = LinearLayoutManager(requireContext())
            recycler?.adapter = MaterialAdapter(materials)
        } else {
            card?.visibility = View.GONE
        }
    }

    private fun setupFiles(files: List<com.example.publishingapp.data.network.OrderFileDto>) {
        val recycler = view?.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerFiles)
        val noFiles = view?.findViewById<TextView>(R.id.tvNoFiles)
        val card = view?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardFiles)

        if (files.isNotEmpty()) {
            card?.visibility = View.VISIBLE
            noFiles?.visibility = View.GONE
            recycler?.visibility = View.VISIBLE

            recycler?.layoutManager = LinearLayoutManager(requireContext())
            recycler?.adapter = FileAdapter(files) { file ->
                downloadFile(file)
            }
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
}