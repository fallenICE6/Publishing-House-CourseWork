package com.example.publishingapp.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.publishingapp.R
import com.example.publishingapp.data.network.*
import com.example.publishingapp.databinding.FragmentServiceOrderBinding
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
    private var materials: List<MaterialDto> = emptyList()

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
            binding.tvFiles.text = selectedFiles.joinToString { it.lastPathSegment ?: "file" }
            updateFilesSizeIndicator()
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
        binding.tvServicePrice.text = "${service.price} ₽"

        val isPrinting = service.category.lowercase() == "printing"

        binding.blockQuantityPages.visibility = if (isPrinting) View.VISIBLE else View.GONE
        binding.layoutMaterials.visibility = if (isPrinting) View.VISIBLE else View.GONE

        if (isPrinting) loadMaterials()

        binding.btnUploadFiles.setOnClickListener { pickFilesLauncher.launch("*/*") }

        binding.btnPlaceOrder.setOnClickListener {
            if (selectedFiles.isEmpty()) {
                Toast.makeText(requireContext(), "Прикрепите хотя бы один файл", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = if (isPrinting) {
                binding.etQuantity.text.toString().toIntOrNull() ?: 0
            } else {
                0
            }

            val pages = if (isPrinting) {
                binding.etPages.text.toString().toIntOrNull() ?: 0
            } else {
                0
            }

            if (isPrinting) {
                if (quantity <= 0) {
                    Toast.makeText(requireContext(), "Введите корректный тираж", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (pages <= 0) {
                    Toast.makeText(requireContext(), "Введите корректное количество страниц", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val totalPrice = calculateTotalPrice(isPrinting)
            showOrderConfirmDialog(totalPrice)
        }

        updateFilesSizeIndicator()
    }

    private fun loadMaterials() {
        ApiClient.apiService.getMaterials().enqueue(object : Callback<List<MaterialDto>> {
            override fun onResponse(call: Call<List<MaterialDto>>, response: Response<List<MaterialDto>>) {
                materials = response.body() ?: emptyList()
                val papers = materials.filter { it.category == "paper" }
                val covers = materials.filter { it.category == "cover" }
                val bindings = materials.filter { it.category == "binding" }

                binding.spinnerPaper.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    papers.map { "${it.name} — ${it.price} ₽/лист" }
                )
                binding.spinnerCover.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    covers.map { "${it.name} — ${it.price} ₽/шт" }
                )
                binding.spinnerBinding.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    bindings.map { "${it.name} — ${it.price} ₽/шт" }
                )
            }

            override fun onFailure(call: Call<List<MaterialDto>>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка загрузки материалов", Toast.LENGTH_SHORT).show()
            }
        })
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
        val totalSizeMB = calculateTotalSize(selectedFiles) / (1024.0 * 1024.0)
        binding.tvFilesSize.text = String.format("%.1f МБ / %d МБ", totalSizeMB, MAX_TOTAL_SIZE_MB)
    }

    private fun getSelectedMaterials(): List<OrderMaterialRequest> {
        val result = mutableListOf<OrderMaterialRequest>()
        val papers = materials.filter { it.category == "paper" }
        val covers = materials.filter { it.category == "cover" }
        val bindings = materials.filter { it.category == "binding" }

        if (papers.isNotEmpty()) result.add(OrderMaterialRequest(papers[binding.spinnerPaper.selectedItemPosition].id))
        if (covers.isNotEmpty()) result.add(OrderMaterialRequest(covers[binding.spinnerCover.selectedItemPosition].id))
        if (bindings.isNotEmpty()) result.add(OrderMaterialRequest(bindings[binding.spinnerBinding.selectedItemPosition].id))

        return result
    }

    private fun uriToFilePreserveName(uri: Uri): File {
        val resolver = requireContext().contentResolver
        val fileName = resolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(index)
        } ?: "file"

        val file = File(requireContext().cacheDir, fileName)
        resolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }

    private fun getMimeType(uri: Uri): String = requireContext().contentResolver.getType(uri) ?: "*/*"

    private fun calculateTotalPrice(isPrinting: Boolean): Double {
        var total = service.price
        if (isPrinting) {
            getSelectedMaterials().forEach { materialReq ->
                val m = materials.find { it.id == materialReq.materialId }
                m?.let {
                    val qty = if (it.category == "paper") {
                        (binding.etQuantity.text.toString().toIntOrNull() ?: 1) *
                                (binding.etPages.text.toString().toIntOrNull() ?: 1)
                    } else binding.etQuantity.text.toString().toIntOrNull() ?: 1
                    total += it.price * qty
                }
            }
        }
        return total
    }

    private fun showOrderConfirmDialog(totalPrice: Double) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_order_confirm, null)
        val tvTotal = dialogView.findViewById<TextView>(R.id.tvTotalPrice)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmOrder)

        tvTotal.text = "$totalPrice ₽"
        AppPrefs.getUser()?.email?.let { etEmail.setText(it) }

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
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
            0
        }

        val pages = if (isPrinting) {
            binding.etPages.text.toString().toIntOrNull()
        } else {
            0
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
                        Toast.makeText(requireContext(), "Ошибка ${response.code()}", Toast.LENGTH_SHORT).show()
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
        binding.btnUploadFiles.isEnabled = !show
        binding.etQuantity.isEnabled = !show
        binding.etPages.isEnabled = !show
        binding.spinnerPaper.isEnabled = !show
        binding.spinnerCover.isEnabled = !show
        binding.spinnerBinding.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
