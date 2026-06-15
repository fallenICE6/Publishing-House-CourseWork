package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.AddCommentRequest
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.OrderFullDto
import com.example.publishingapp.ui.adapters.CommentsAdapter
import com.example.publishingapp.ui.adapters.FileAdapter
import com.example.publishingapp.ui.adapters.MaterialAdapter
import com.example.publishingapp.utils.FileDownloader
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class EditorOrderDetailsFragment : Fragment(R.layout.fragment_editor_order_details) {

    companion object {
        private const val ARG_ORDER_ID = "order_id"

        fun newInstance(orderId: Long): EditorOrderDetailsFragment {
            return EditorOrderDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ORDER_ID, orderId)
                }
            }
        }
    }

    private var order: OrderFullDto? = null
    private lateinit var commentsAdapter: CommentsAdapter
    private var btnSendToReview: MaterialCardView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orderId = arguments?.getLong(ARG_ORDER_ID) ?: -1L
        if (orderId == -1L) {
            Toast.makeText(requireContext(), "Ошибка загрузки заказа", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return
        }

        setupUI()
        loadOrder(orderId)
    }

    private fun setupUI() {
        commentsAdapter = CommentsAdapter()

        val rvComments = view?.findViewById<RecyclerView>(R.id.rvComments)
        rvComments?.layoutManager = LinearLayoutManager(requireContext())
        rvComments?.adapter = commentsAdapter

        val btnSendComment = view?.findViewById<MaterialButton>(R.id.btnSendComment)
        val etComment = view?.findViewById<TextInputEditText>(R.id.etComment)
        btnSendToReview = view?.findViewById<MaterialCardView>(R.id.btnSendToReview)

        btnSendComment?.setOnClickListener {
            val comment = etComment?.text?.toString()?.trim()
            if (!comment.isNullOrEmpty()) {
                sendComment(comment)
                etComment?.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Введите комментарий", Toast.LENGTH_SHORT).show()
            }
        }

        btnSendToReview?.setOnClickListener {
            sendToReview()
        }
    }

    private fun loadOrder(orderId: Long) {
        showProgress(true)

        lifecycleScope.launch {
            try {
                order = ApiClient.apiService.getOrderByIdForEditor(orderId)

                if (order != null) {
                    updateUI(order!!)
                } else {
                    Toast.makeText(requireContext(), "Заказ не найден", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            } finally {
                showProgress(false)
            }
        }
    }

    private fun updateUI(order: OrderFullDto) {
        view?.findViewById<TextView>(R.id.tvOrderNumber)?.text = "Заказ №${order.id}"

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date = try {
            dateFormat.format(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .parse(order.createdAt)
            )
        } catch (e: Exception) {
            order.createdAt
        }
        view?.findViewById<TextView>(R.id.tvOrderDate)?.text = date

        val statusView = view?.findViewById<TextView>(R.id.tvStatus)
        statusView?.text = order.status
        val statusBackground = when (order.status.lowercase()) {
            "редактируется" -> R.drawable.status_background_editing
            "на проверке" -> R.drawable.status_background_under_review
            else -> R.drawable.status_background_created
        }
        statusView?.setBackgroundResource(statusBackground)

        view?.findViewById<TextView>(R.id.tvFullName)?.text = order.fullName
        view?.findViewById<TextView>(R.id.tvEmail)?.text = order.email ?: "—"
        view?.findViewById<TextView>(R.id.tvPhone)?.text = order.phone ?: "—"

        view?.findViewById<TextView>(R.id.tvServiceTitle)?.text = order.serviceTitle
        view?.findViewById<TextView>(R.id.tvPages)?.text = order.pages?.toString() ?: "—"
        view?.findViewById<TextView>(R.id.tvQuantity)?.text = order.quantity?.toString() ?: "—"

        setupMaterials(order.materials)
        setupFiles(order.files)
        setupReview(order.review)
        commentsAdapter.submitList(order.comments)

        view?.findViewById<TextView>(R.id.tvTotalPrice)?.text = "${order.totalPrice} ₽"

        val isEditing = order.status.lowercase() == "редактируется"
        btnSendToReview?.isVisible = isEditing
    }

    private fun setupMaterials(materials: List<com.example.publishingapp.data.network.OrderMaterialDto>) {
        val recycler = view?.findViewById<RecyclerView>(R.id.recyclerMaterials)
        val noMaterials = view?.findViewById<TextView>(R.id.tvNoMaterials)
        val card = view?.findViewById<MaterialCardView>(R.id.cardMaterials)

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
        val recycler = view?.findViewById<RecyclerView>(R.id.recyclerFiles)
        val noFiles = view?.findViewById<TextView>(R.id.tvNoFiles)
        val card = view?.findViewById<MaterialCardView>(R.id.cardFiles)

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

    private fun sendComment(comment: String) {
        lifecycleScope.launch {
            try {
                val request = AddCommentRequest(comment)
                val newComment = ApiClient.apiService.addEditorComment(order!!.id, request)

                val updatedComments = order!!.comments.toMutableList()
                updatedComments.add(newComment)
                order = order!!.copy(comments = updatedComments)
                commentsAdapter.submitList(updatedComments)

                val rvComments = view?.findViewById<RecyclerView>(R.id.rvComments)
                rvComments?.postDelayed({
                    rvComments.smoothScrollToPosition(updatedComments.size - 1)
                }, 100)

                Toast.makeText(requireContext(), "Сообщение отправлено", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupReview(review: com.example.publishingapp.data.network.ReviewDto?) {
        val card = view?.findViewById<MaterialCardView>(R.id.cardReview)
        val noReview = view?.findViewById<TextView>(R.id.tvNoReview)
        val status = view?.findViewById<TextView>(R.id.tvReviewStatus)
        val comment = view?.findViewById<TextView>(R.id.tvReviewComment)

        if (review != null) {
            card?.visibility = View.VISIBLE
            noReview?.visibility = View.GONE
            status?.visibility = View.VISIBLE
            comment?.visibility = View.VISIBLE

            val decisionText = when (review.status?.lowercase()) {
                "approved" -> "Одобрен"
                "rejected" -> "Отклонен"
                "pending" -> "Отправлен на доработку"
                else -> review.status ?: "Не указан"
            }

            status?.text = "Статус рецензии: $decisionText"
            comment?.text = review.comment ?: "Без комментария"
        } else {
            card?.visibility = View.GONE
            noReview?.visibility = View.VISIBLE
        }
    }
    private fun sendToReview() {
        lifecycleScope.launch {
            try {
                showProgress(true)
                val updatedOrder = ApiClient.apiService.sendToReview(order!!.id)
                order = updatedOrder
                commentsAdapter.submitList(updatedOrder.comments)
                updateUI(updatedOrder)
                Toast.makeText(requireContext(), "Заказ отправлен на проверку.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showProgress(false)
            }
        }
    }

    private fun downloadFile(file: com.example.publishingapp.data.network.OrderFileDto) {
        lifecycleScope.launch {
            try {
                Toast.makeText(requireContext(), "Скачивание ${file.fileName}...", Toast.LENGTH_SHORT).show()
                val responseBody = ApiClient.apiService.downloadFile(file.id)
                val success = FileDownloader.downloadFile(requireContext(), responseBody, file.fileName)
                if (success) {
                    Toast.makeText(requireContext(), "Файл сохранен", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProgress(show: Boolean) {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = if (show) View.VISIBLE else View.GONE
        btnSendToReview?.isEnabled = !show
        view?.findViewById<MaterialButton>(R.id.btnSendComment)?.isEnabled = !show
    }
}