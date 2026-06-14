package com.example.publishingapp.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.publishingapp.R
import com.example.publishingapp.data.network.*
import com.example.publishingapp.databinding.FragmentServiceOrderBinding
import com.example.publishingapp.ui.adapters.SelectedFilesAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.NumberFormat
import java.util.Locale

class ServiceOrderFragment : Fragment(R.layout.fragment_service_order) {

    companion object {
        private const val ARG_SERVICE = "service"
        private const val MAX_TOTAL_SIZE_MB = 10
        private const val MAX_TOTAL_SIZE_BYTES = MAX_TOTAL_SIZE_MB * 1024 * 1024

        fun newInstance(service: ServiceDto) =
            ServiceOrderFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SERVICE, service)
                }
            }
    }

    private var _binding: FragmentServiceOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var service: ServiceDto
    private val selectedFiles = mutableListOf<Uri>()
    private lateinit var filesAdapter: SelectedFilesAdapter
    private var materials: List<MaterialDto> = emptyList()

    private var papers: List<MaterialDto> = emptyList()
    private var covers: List<MaterialDto> = emptyList()
    private var bindings: List<MaterialDto> = emptyList()

    // Хранение выбранных материалов
    private var selectedPaper: MaterialDto? = null
    private var selectedCover: MaterialDto? = null
    private var selectedBinding: MaterialDto? = null

    private val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }

    private val pickFilesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
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

            selectedFiles.clear()
            selectedFiles.addAll(uris)
            filesAdapter.submitList(selectedFiles.toList())
            updateFilesSizeIndicator()

            if (selectedFiles.isNotEmpty()) {
                binding.filesContainer.visibility = View.VISIBLE
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentServiceOrderBinding.bind(view)

        service = arguments?.getSerializable(ARG_SERVICE) as? ServiceDto
            ?: run {
                Toast.makeText(requireContext(), "Услуга не найдена", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
                return
            }

        setupUI()
    }

    private fun setupUI() {
        binding.tvServiceTitle.text = service.title
        binding.tvServicePrice.text = "${formatPrice(service.price)} ₽"

        val isPrinting = service.category.lowercase() == "printing"

        binding.cardParameters.visibility = if (isPrinting) View.VISIBLE else View.GONE

        setupFilesRecycler()

        binding.uploadArea.setOnClickListener {
            pickFilesLauncher.launch("*/*")
        }

        if (isPrinting) {
            loadMaterials()
            setupPriceCalculation()
        }

        updateTotalPrice()

        binding.btnPlaceOrder.setOnClickListener {
            submitOrder(isPrinting)
        }

        updateFilesSizeIndicator()
    }

    private fun setupPriceCalculation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateTotalPrice()
            }
        }

        binding.etQuantity.addTextChangedListener(textWatcher)
        binding.etPages.addTextChangedListener(textWatcher)

        binding.spinnerPaper.setOnItemClickListener { _, _, position, _ ->
            val items = papers.map { "${it.name} — ${formatPrice(it.price)} ₽/лист" }
            selectedPaper = papers.getOrNull(position)
            binding.spinnerPaper.setText(items[position], false)
            updateTotalPrice()
        }

        binding.spinnerCover.setOnItemClickListener { _, _, position, _ ->
            val items = covers.map { "${it.name} — ${formatPrice(it.price)} ₽/шт" }
            selectedCover = covers.getOrNull(position)
            binding.spinnerCover.setText(items[position], false)
            updateTotalPrice()
        }

        binding.spinnerBinding.setOnItemClickListener { _, _, position, _ ->
            val items = bindings.map { "${it.name} — ${formatPrice(it.price)} ₽/шт" }
            selectedBinding = bindings.getOrNull(position)
            binding.spinnerBinding.setText(items[position], false)
            updateTotalPrice()
        }
    }

    private fun updateTotalPrice() {
        val isPrinting = service.category.lowercase() == "printing"
        val total = calculateTotalPrice(isPrinting)
        binding.tvTotalPrice.text = "${formatPrice(total)} ₽"
    }

    private fun formatPrice(price: Double): String {
        return numberFormat.format(price)
    }

    private fun setupFilesRecycler() {
        filesAdapter = SelectedFilesAdapter { uri ->
            selectedFiles.remove(uri)
            filesAdapter.submitList(selectedFiles.toList())
            updateFilesSizeIndicator()
            if (selectedFiles.isEmpty()) {
                binding.filesContainer.visibility = View.GONE
            }
        }
        binding.filesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.filesRecycler.adapter = filesAdapter
    }

    private fun loadMaterials() {
        showLoading(true)

        ApiClient.apiService.getMaterials().enqueue(object : Callback<List<MaterialDto>> {
            override fun onResponse(call: Call<List<MaterialDto>>, response: Response<List<MaterialDto>>) {
                showLoading(false)
                materials = response.body() ?: emptyList()
                papers = materials.filter { it.category == "paper" }
                covers = materials.filter { it.category == "cover" }
                bindings = materials.filter { it.category == "binding" }

                setupMaterialSpinners()
            }

            override fun onFailure(call: Call<List<MaterialDto>>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Ошибка загрузки материалов: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupMaterialSpinners() {
        // Бумага
        val paperItems = papers.map { "${it.name} — ${formatPrice(it.price)} ₽/лист" }
        val paperAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, paperItems)
        binding.spinnerPaper.setAdapter(paperAdapter)

        // Отключаем редактирование и проверку орфографии
        binding.spinnerPaper.setOnTouchListener { _, _ ->
            binding.spinnerPaper.showDropDown()
            true
        }

        if (papers.isNotEmpty()) {
            selectedPaper = papers[0]
            binding.spinnerPaper.setText(paperItems[0], false)
        } else {
            binding.spinnerPaper.setText("Бумага не найдена", false)
            binding.spinnerPaper.isEnabled = false
        }

        // Обложка
        val coverItems = covers.map { "${it.name} — ${formatPrice(it.price)} ₽/шт" }
        val coverAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, coverItems)
        binding.spinnerCover.setAdapter(coverAdapter)

        binding.spinnerCover.setOnTouchListener { _, _ ->
            binding.spinnerCover.showDropDown()
            true
        }

        if (covers.isNotEmpty()) {
            selectedCover = covers[0]
            binding.spinnerCover.setText(coverItems[0], false)
        } else {
            binding.spinnerCover.setText("Обложка не найдена", false)
            binding.spinnerCover.isEnabled = false
        }

        // Переплет
        val bindingItems = bindings.map { "${it.name} — ${formatPrice(it.price)} ₽/шт" }
        val bindingAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, bindingItems)
        binding.spinnerBinding.setAdapter(bindingAdapter)

        binding.spinnerBinding.setOnTouchListener { _, _ ->
            binding.spinnerBinding.showDropDown()
            true
        }

        if (bindings.isNotEmpty()) {
            selectedBinding = bindings[0]
            binding.spinnerBinding.setText(bindingItems[0], false)
        } else {
            binding.spinnerBinding.setText("Переплет не найден", false)
            binding.spinnerBinding.isEnabled = false
        }

        updateTotalPrice()
    }

    private fun calculateTotalSize(uris: List<Uri>): Long {
        var total = 0L
        val resolver = requireContext().contentResolver
        uris.forEach { uri ->
            resolver.openFileDescriptor(uri, "r")?.use { total += it.statSize }
        }
        return total
    }

    private fun updateFilesSizeIndicator() {
        val totalSizeBytes = calculateTotalSize(selectedFiles)
        val totalSizeMB = totalSizeBytes / (1024.0 * 1024.0)
        val progress = ((totalSizeBytes.toFloat() / MAX_TOTAL_SIZE_BYTES) * 100).toInt()
        binding.tvFilesSize.text = String.format("%.1f / %d МБ", totalSizeMB, MAX_TOTAL_SIZE_MB)
        binding.sizeProgress.progress = progress
    }

    private fun getSelectedMaterials(): List<OrderMaterialRequest> {
        val result = mutableListOf<OrderMaterialRequest>()
        selectedPaper?.let { result.add(OrderMaterialRequest(it.id)) }
        selectedCover?.let { result.add(OrderMaterialRequest(it.id)) }
        selectedBinding?.let { result.add(OrderMaterialRequest(it.id)) }
        return result
    }

    private fun uriToFilePreserveName(uri: Uri): File {
        val resolver = requireContext().contentResolver
        val fileName = resolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(index)
        } ?: "file_${System.currentTimeMillis()}"

        val file = File(requireContext().cacheDir, fileName)
        resolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }

    private fun getMimeType(uri: Uri): String = requireContext().contentResolver.getType(uri) ?: "application/octet-stream"

    private fun calculateTotalPrice(isPrinting: Boolean): Double {
        var total = service.price

        if (isPrinting) {
            val quantity = (binding.etQuantity.text.toString().toIntOrNull() ?: 1).coerceAtLeast(1)
            val pages = (binding.etPages.text.toString().toIntOrNull() ?: 1).coerceAtLeast(1)

            selectedPaper?.let {
                total += it.price * quantity * pages
            }

            selectedCover?.let {
                total += it.price * quantity
            }

            selectedBinding?.let {
                total += it.price * quantity
            }
        }

        return total
    }

    private fun validatePrintingOrder(): Boolean {
        var isValid = true

        // Проверка тиража
        val quantity = binding.etQuantity.text.toString().toIntOrNull()
        if (quantity == null || quantity <= 0) {
            binding.etQuantity.error = "Введите корректный тираж"
            isValid = false
        } else {
            binding.etQuantity.error = null
        }

        // Проверка количества страниц
        val pages = binding.etPages.text.toString().toIntOrNull()
        if (pages == null || pages <= 0) {
            binding.etPages.error = "Введите корректное количество страниц"
            isValid = false
        } else {
            binding.etPages.error = null
        }

        // Проверка выбора бумаги - показываем Toast вместо error на спиннере
        if (selectedPaper == null) {
            Toast.makeText(requireContext(), "Выберите тип бумаги", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Проверка выбора обложки
        if (selectedCover == null) {
            Toast.makeText(requireContext(), "Выберите тип обложки", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Проверка выбора переплета
        if (selectedBinding == null) {
            Toast.makeText(requireContext(), "Выберите тип переплета", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(requireContext(), "Заполните все параметры печати", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }

    private fun submitOrder(isPrinting: Boolean) {
        if (selectedFiles.isEmpty()) {
            Toast.makeText(requireContext(), "Прикрепите хотя бы один файл", Toast.LENGTH_SHORT).show()
            return
        }

        if (isPrinting && !validatePrintingOrder()) {
            return
        }

        val totalPrice = calculateTotalPrice(isPrinting)
        showOrderConfirmDialog(totalPrice)
    }

    private fun showOrderConfirmDialog(totalPrice: Double) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_order_confirm, null)
        val tvTotal = dialogView.findViewById<TextView>(R.id.tvTotalPrice)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
        val btnConfirm = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConfirmOrder)

        tvTotal.text = "${formatPrice(totalPrice)} ₽"

        val user = AppPrefs.getUser()
        if (user?.email != null) {
            etEmail.setText(user.email)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnConfirm.setOnClickListener {
            val email = etEmail.text.toString()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Введите корректный email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            sendOrder(email)
        }

        dialog.show()
    }

    private fun sendOrder(email: String) {
        showLoading(true)

        val isPrinting = service.category.lowercase() == "printing"

        val quantity = if (isPrinting) {
            binding.etQuantity.text.toString().toIntOrNull()
        } else {
            null
        }

        val pages = if (isPrinting) {
            binding.etPages.text.toString().toIntOrNull()
        } else {
            null
        }

        val orderRequest = CreateOrderRequest(
            serviceId = service.id,
            pages = pages,
            quantity = quantity,
            materials = if (isPrinting) getSelectedMaterials() else emptyList(),
            email = email
        )

        val orderPart = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            Gson().toJson(orderRequest)
        )

        val fileParts = selectedFiles.map { uri ->
            val file = uriToFilePreserveName(uri)
            val mimeType = getMimeType(uri)
            MultipartBody.Part.createFormData(
                "files",
                file.name,
                file.asRequestBody(mimeType.toMediaTypeOrNull())
            )
        }

        ApiClient.apiService.createOrder(orderPart, fileParts)
            .enqueue(object : Callback<OrderDto> {
                override fun onResponse(call: Call<OrderDto>, response: Response<OrderDto>) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Заказ создан. Уведомление отправлено на $email",
                            Toast.LENGTH_LONG
                        ).show()
                        requireActivity().onBackPressed()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Ошибка ${response.code()}"
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OrderDto>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnPlaceOrder.isEnabled = !show
        binding.uploadArea.isEnabled = !show
        binding.spinnerPaper.isEnabled = !show
        binding.spinnerCover.isEnabled = !show
        binding.spinnerBinding.isEnabled = !show
        binding.etQuantity.isEnabled = !show
        binding.etPages.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}