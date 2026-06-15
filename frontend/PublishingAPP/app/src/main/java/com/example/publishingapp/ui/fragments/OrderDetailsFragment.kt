package com.example.publishingapp.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.example.publishingapp.ui.adapters.FileManageAdapter
import com.example.publishingapp.ui.adapters.MaterialAdapter
import com.example.publishingapp.utils.FileDownloader
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailsFragment : Fragment(R.layout.fragment_order_details) {

    companion object {
        private const val ARG_ORDER_ID = "order_id"
        private const val MAX_TOTAL_SIZE_MB = 10
        private const val MAX_TOTAL_SIZE_BYTES = MAX_TOTAL_SIZE_MB * 1024 * 1024

        fun newInstance(orderId: Long): OrderDetailsFragment {
            return OrderDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ORDER_ID, orderId)
                }
            }
        }
    }

    private var order: OrderFullDto? = null
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var fileManageAdapter: FileManageAdapter
    private val newFiles = mutableListOf<Uri>()

    private val pickFilesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isEmpty()) return@registerForActivityResult

        val totalSize = calculateTotalSize(uris)
        if (totalSize > MAX_TOTAL_SIZE_BYTES) {
            Toast.makeText(
                requireContext(),
                "Суммарный размер файлов не должен превышать $MAX_TOTAL_SIZE_MB МБ",
                Toast.LENGTH_LONG
            ).show()
            return@registerForActivityResult
        }

        newFiles.clear()
        newFiles.addAll(uris)
        updateNewFilesUI()
    }

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
        fileManageAdapter = FileManageAdapter(
            onDeleteFile = { fileId ->
                deleteSingleFile(fileId)
            },
            onDownloadFile = { file ->
                downloadFile(file)
            }
        )

        val rvComments = view?.findViewById<RecyclerView>(R.id.rvComments)
        rvComments?.layoutManager = LinearLayoutManager(requireContext())
        rvComments?.adapter = commentsAdapter

        val recyclerFiles = view?.findViewById<RecyclerView>(R.id.recyclerFiles)
        recyclerFiles?.layoutManager = LinearLayoutManager(requireContext())

        val btnSendComment = view?.findViewById<MaterialButton>(R.id.btnSendComment)
        val etComment = view?.findViewById<TextInputEditText>(R.id.etComment)

        btnSendComment?.setOnClickListener {
            val comment = etComment?.text?.toString()?.trim()
            if (!comment.isNullOrEmpty()) {
                sendComment(comment)
                etComment?.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Введите комментарий", Toast.LENGTH_SHORT).show()
            }
        }

        val recyclerCurrentFiles = view?.findViewById<RecyclerView>(R.id.recyclerCurrentFiles)
        recyclerCurrentFiles?.layoutManager = LinearLayoutManager(requireContext())
        recyclerCurrentFiles?.adapter = fileManageAdapter

        val uploadArea = view?.findViewById<MaterialCardView>(R.id.uploadAreaAuthor)
        uploadArea?.setOnClickListener {
            pickFilesLauncher.launch("*/*")
        }

        val btnSaveChanges = view?.findViewById<MaterialButton>(R.id.btnSaveChanges)
        btnSaveChanges?.setOnClickListener {
            saveFileChanges()
        }

        val btnReplaceAll = view?.findViewById<MaterialButton>(R.id.btnReplaceAll)
        btnReplaceAll?.setOnClickListener {
            confirmReplaceAllFiles()
        }
    }

    private fun updateNewFilesUI() {
        val tvNewFiles = view?.findViewById<TextView>(R.id.tvNewFiles)
        val newFilesContainer = view?.findViewById<LinearLayout>(R.id.newFilesContainer)

        if (newFiles.isNotEmpty()) {
            val names = newFiles.mapNotNull { uri -> getFileName(uri) }.joinToString(", ")
            tvNewFiles?.text = "Новые файлы для добавления: $names"
            newFilesContainer?.visibility = View.VISIBLE
        } else {
            newFilesContainer?.visibility = View.GONE
        }
    }

    private fun saveFileChanges() {
        val currentFilesCount = (order?.files?.size ?: 0)
        val willHaveFiles = currentFilesCount + newFiles.size

        if (willHaveFiles == 0) {
            Toast.makeText(requireContext(), "❌ Заказ должен содержать хотя бы один файл", Toast.LENGTH_LONG).show()
            return
        }

        if (newFiles.isEmpty()) {
            Toast.makeText(requireContext(), "Нет новых файлов для добавления", Toast.LENGTH_SHORT).show()
            return
        }

        showProgress(true)

        lifecycleScope.launch {
            try {
                val fileParts = newFiles.map { uri ->
                    val contentType = requireContext().contentResolver.getType(uri) ?: "application/octet-stream"
                    val fileName = getFileName(uri) ?: "file"
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                        ?: throw Exception("Cannot open file stream")

                    val bytes = inputStream.use { it.readBytes() }
                    val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())

                    MultipartBody.Part.createFormData("files", fileName, requestBody)
                }

                val updatedOrder = ApiClient.apiService.addFiles(order!!.id, fileParts)

                order = updatedOrder
                newFiles.clear()
                updateNewFilesUI()
                updateFileManageUI(updatedOrder.files)
                updateRegularFilesList(updatedOrder.files)

                Toast.makeText(requireContext(), "✅ Файлы добавлены", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showProgress(false)
            }
        }
    }

    private fun deleteSingleFile(fileId: Long) {
        val currentFiles = order?.files ?: return

        if (currentFiles.size == 1 && newFiles.isEmpty()) {
            Toast.makeText(requireContext(), "❌ Нельзя удалить последний файл. Сначала добавьте новый.", Toast.LENGTH_LONG).show()
            return
        }

        showProgress(true)

        lifecycleScope.launch {
            try {
                val updatedOrder = ApiClient.apiService.deleteFiles(order!!.id, listOf(fileId))

                order = updatedOrder
                updateFileManageUI(updatedOrder.files)
                updateRegularFilesList(updatedOrder.files)

                Toast.makeText(requireContext(), "✅ Файл удалён", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showProgress(false)
            }
        }
    }
    private fun updateRegularFilesList(files: List<com.example.publishingapp.data.network.OrderFileDto>) {
        val recyclerFiles = view?.findViewById<RecyclerView>(R.id.recyclerFiles)
        val tvNoFiles = view?.findViewById<TextView>(R.id.tvNoFiles)
        val cardFiles = view?.findViewById<MaterialCardView>(R.id.cardFiles)

        if (files.isNotEmpty()) {
            tvNoFiles?.visibility = View.GONE
            recyclerFiles?.visibility = View.VISIBLE
            cardFiles?.visibility = View.VISIBLE

            recyclerFiles?.let {
                if (it.layoutManager == null) {
                    it.layoutManager = LinearLayoutManager(requireContext())
                }
                val adapter = FileAdapter(files) { file ->
                    downloadFile(file)
                }
                it.adapter = adapter
                it.adapter?.notifyDataSetChanged()
            }
        } else {
            tvNoFiles?.visibility = View.VISIBLE
            recyclerFiles?.visibility = View.GONE
            cardFiles?.visibility = View.GONE
        }
    }

    private fun confirmReplaceAllFiles() {
        AlertDialog.Builder(requireContext())
            .setTitle("Заменить все файлы")
            .setMessage("Все текущие файлы будут удалены. Выберите новые файлы для загрузки.")
            .setPositiveButton("Продолжить") { _, _ ->
                replaceAllFilesLauncher.launch("*/*")
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private val replaceAllFilesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isEmpty()) {
            Toast.makeText(requireContext(), "❌ Выберите хотя бы один файл", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        replaceAllFiles(uris)
    }

    private fun replaceAllFiles(uris: List<Uri>) {
        showProgress(true)

        lifecycleScope.launch {
            try {
                val fileParts = uris.map { uri ->
                    val contentType = requireContext().contentResolver.getType(uri) ?: "application/octet-stream"
                    val fileName = getFileName(uri) ?: "file"
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                        ?: throw Exception("Cannot open file stream")

                    val bytes = inputStream.use { it.readBytes() }
                    val requestBody = bytes.toRequestBody(contentType.toMediaTypeOrNull())

                    MultipartBody.Part.createFormData("files", fileName, requestBody)
                }

                val updatedOrder = ApiClient.apiService.reuploadFiles(
                    orderId = order!!.id,
                    files = fileParts,
                    comment = null
                )

                order = updatedOrder
                newFiles.clear()
                updateNewFilesUI()
                updateFileManageUI(updatedOrder.files)
                updateRegularFilesList(updatedOrder.files)

                Toast.makeText(requireContext(), "✅ Все файлы заменены", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showProgress(false)
            }
        }
    }

    private fun updateFileManageUI(files: List<com.example.publishingapp.data.network.OrderFileDto>) {
        fileManageAdapter.submitList(files)
    }

    private fun getFileName(uri: Uri): String? {
        return requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }

    private fun calculateTotalSize(uris: List<Uri>): Long {
        var total = 0L
        val resolver = requireContext().contentResolver
        uris.forEach { uri ->
            resolver.openFileDescriptor(uri, "r")?.use { total += it.statSize }
        }
        return total
    }

    private fun sendComment(comment: String) {
        lifecycleScope.launch {
            try {
                val request = AddCommentRequest(comment)
                val newComment = ApiClient.apiService.addAuthorComment(order!!.id, request)

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

    private fun loadOrder(orderId: Long) {
        showProgress(true)

        lifecycleScope.launch {
            try {
                val allOrders = ApiClient.apiService.getMyOrders()
                order = allOrders.find { it.id == orderId }

                if (order != null) {
                    updateUI(order!!)
                } else {
                    Toast.makeText(requireContext(), "Заказ не найден", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки заказа: ${e.message}", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            } finally {
                showProgress(false)
            }
        }
    }

    private fun updateUI(order: OrderFullDto) {
        view?.findViewById<TextView>(R.id.tvOrderNumber)?.text = "Заказ №${order.id}"

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = try {
            dateFormat.format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .parse(order.createdAt))
        } catch (e: Exception) {
            order.createdAt.take(10)
        }
        view?.findViewById<TextView>(R.id.tvOrderDate)?.text = "от $date"

        val statusView = view?.findViewById<TextView>(R.id.tvStatus)
        statusView?.text = order.status

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

        view?.findViewById<TextView>(R.id.tvFullName)?.text = order.fullName
        view?.findViewById<TextView>(R.id.tvEmail)?.text = order.email ?: "—"
        view?.findViewById<TextView>(R.id.tvPhone)?.text = order.phone ?: "—"

        view?.findViewById<TextView>(R.id.tvServiceTitle)?.text = order.serviceTitle
        view?.findViewById<TextView>(R.id.tvPages)?.text = order.pages?.toString() ?: "—"
        view?.findViewById<TextView>(R.id.tvQuantity)?.text = order.quantity?.toString() ?: "—"

        setupMaterials(order.materials)
        setupReview(order.review)

        view?.findViewById<TextView>(R.id.tvTotalPrice)?.text = "${order.totalPrice} ₽"

        val isEditing = order.status.lowercase() == "редактируется"
        val cardChat = view?.findViewById<MaterialCardView>(R.id.cardChat)
        val cardFileManage = view?.findViewById<MaterialCardView>(R.id.cardFileManage)
        val cardFiles = view?.findViewById<MaterialCardView>(R.id.cardFiles)

        if (isEditing) {
            cardChat?.visibility = View.VISIBLE
            cardFileManage?.visibility = View.VISIBLE
            cardFiles?.visibility = View.GONE

            commentsAdapter.submitList(order.comments)
            updateFileManageUI(order.files)
        } else {
            cardChat?.visibility = View.GONE
            cardFileManage?.visibility = View.GONE
            cardFiles?.visibility = View.VISIBLE

            updateRegularFilesList(order.files)
        }
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
                Toast.makeText(requireContext(), "Ошибка скачивания: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProgress(show: Boolean) {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = if (show) View.VISIBLE else View.GONE
        view?.findViewById<MaterialButton>(R.id.btnSendComment)?.isEnabled = !show
        view?.findViewById<MaterialButton>(R.id.btnSaveChanges)?.isEnabled = !show
        view?.findViewById<MaterialButton>(R.id.btnReplaceAll)?.isEnabled = !show
    }
}