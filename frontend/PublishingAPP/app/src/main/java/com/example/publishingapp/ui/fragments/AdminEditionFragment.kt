package com.example.publishingapp.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.MultiAutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import androidx.fragment.app.Fragment
import coil.load
import com.example.publishingapp.R
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.EditionRequest
import com.example.publishingapp.databinding.FragmentAdminEditionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import com.example.publishingapp.data.network.NetworkConstants

class AdminEditionFragment : Fragment() {

    companion object {
        private const val ARG_EDITION_ID = "edition_id"

        fun newInstance(editionId: Long = -1): AdminEditionFragment {
            return AdminEditionFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_EDITION_ID, editionId)
                }
            }
        }
    }

    private var _binding: FragmentAdminEditionBinding? = null
    private val binding get() = _binding!!

    private var isEditMode = false
    private var currentEdition: Edition? = null
    private var editionId: Long = -1

    private var coverImageUri: Uri? = null
    private val interiorImageUris = mutableListOf<Uri>()
    private var coverImageUrl: String? = null
    private val interiorImageUrls = mutableListOf<String>()

    private val pickCoverLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                coverImageUri = uri
                binding.coverImage.load(uri) {
                    placeholder(R.drawable.book1)
                    error(R.drawable.book1)
                }
            }
        }
    }

    private val pickInteriorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                interiorImageUris.add(uri)
                updateInteriorImagesPreview()
            }
        }
    }

    private lateinit var genresAdapter: ArrayAdapter<String>
    private val availableGenres = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            editionId = it.getLong(ARG_EDITION_ID, -1)
            isEditMode = editionId != -1L
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminEditionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        if (isEditMode) {
            binding.toolbar.title = "Редактирование издания"
        } else {
            binding.toolbar.title = "Добавление издания"
        }

        loadInitialData()
    }

    private fun loadInitialData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {

                val genres = ApiClient.apiService.getAllGenres()

                availableGenres.clear()
                availableGenres.addAll(genres)

                genresAdapter.notifyDataSetChanged()

                if (isEditMode) {

                    val edition =
                        ApiClient.apiService.getEditionById(
                            editionId
                        )

                    currentEdition = edition

                    coverImageUrl = edition.coverImage

                    interiorImageUrls.clear()
                    interiorImageUrls.addAll(
                        edition.interiorImages
                    )

                    populateForm(edition)
                }

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    e.message ?: "Ошибка загрузки данных",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupUI() {
        val multiAutoComplete = binding.autoCompleteGenres as? AppCompatMultiAutoCompleteTextView
        multiAutoComplete?.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())

        genresAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            availableGenres
        )
        binding.autoCompleteGenres.setAdapter(genresAdapter)

        binding.btnPickCover.setOnClickListener {
            pickCoverImage()
        }

        binding.btnPickInterior.setOnClickListener {
            pickInteriorImage()
        }

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                saveEdition()
            }
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        if (isEditMode) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener {
                currentEdition?.id?.let { id ->
                    deleteEdition(id)
                }
            }
        }
    }


    private fun pickCoverImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickCoverLauncher.launch(intent)
    }

    private fun pickInteriorImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickInteriorLauncher.launch(intent)
    }

    private fun updateInteriorImagesPreview() {
        binding.tvInteriorCount.text = "Выбрано: ${interiorImageUris.size} изображений"
    }


    private fun populateForm(
        edition: Edition
    ) {

        binding.etTitle.setText(
            edition.title
        )

        binding.etAuthorFirstName.setText(
            edition.authorFirstName
        )

        binding.etAuthorLastName.setText(
            edition.authorLastName
        )

        binding.etAuthorMiddleName.setText(
            edition.authorMiddleName ?: ""
        )

        binding.etDescription.setText(
            edition.description ?: ""
        )


        binding.autoCompleteGenres.setText(
            edition.genres.joinToString(", "),
            false
        )


        if (edition.interiorImages.isNotEmpty()) {

            binding.tvInteriorCount.text =
                "Загружено: ${edition.interiorImages.size} изображений"
        }


        edition.coverImage?.let { imagePath ->

            val imageUrl =
                NetworkConstants.getFullImageUrl(
                    imagePath
                )

            binding.coverImage.load(imageUrl) {

                crossfade(true)

                placeholder(
                    R.drawable.book1
                )

                error(
                    R.drawable.book1
                )
            }
        }
        }


    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.etTitle.text.isNullOrEmpty()) {
            binding.etTitle.error = "Введите название"
            isValid = false
        } else {
            binding.etTitle.error = null
        }

        if (binding.etAuthorFirstName.text.isNullOrEmpty()) {
            binding.etAuthorFirstName.error = "Введите имя автора"
            isValid = false
        } else {
            binding.etAuthorFirstName.error = null
        }

        if (binding.etAuthorLastName.text.isNullOrEmpty()) {
            binding.etAuthorLastName.error = "Введите фамилию автора"
            isValid = false
        } else {
            binding.etAuthorLastName.error = null
        }

        val genresText = binding.autoCompleteGenres.text.toString().trim()
        if (genresText.isEmpty()) {
            Toast.makeText(requireContext(), "Выберите хотя бы один жанр", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Проверка обложки (обязательна при создании, опциональна при редактировании)
        if (!isEditMode && coverImageUri == null) {
            Toast.makeText(requireContext(), "Необходимо выбрать обложку", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Проверка внутренних изображений (минимум 1 при создании)
        if (!isEditMode && interiorImageUris.isEmpty()) {
            Toast.makeText(requireContext(), "Необходимо добавить хотя бы одно внутреннее изображение", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun saveEdition() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Загружаем файлы на сервер
                val (newCoverUrl, newInteriorUrls) = uploadFiles()

                // Создаем запрос
                val request = createEditionRequest(
                    newCoverUrl ?: coverImageUrl,
                    if (newInteriorUrls.isNotEmpty()) newInteriorUrls else interiorImageUrls
                )

                if (isEditMode) {
                    // Обновляем
                    val updatedEdition = ApiClient.apiService.updateEdition(editionId, request)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "Издание обновлено", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                } else {
                    // Создаем
                    val newEdition = ApiClient.apiService.createEdition(request)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "Издание создано", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun uploadFiles(): Pair<String?, List<String>> {
        var newCoverUrl: String? = null
        val newInteriorUrls = mutableListOf<String>()

        try {
            // Загружаем обложку если выбрана новая
            coverImageUri?.let { uri ->
                val coverFile = uri.toFile(requireContext())
                val requestFile = coverFile.asRequestBody("image/*".toMediaTypeOrNull())
                val coverPart = MultipartBody.Part.createFormData(
                    "cover",
                    "cover_${System.currentTimeMillis()}.jpg",
                    requestFile
                )

                val response = ApiClient.apiService.uploadFiles(coverPart, null)
                if (response.isNotEmpty()) {
                    newCoverUrl = response[0]
                }
            }

            // Загружаем внутренние изображения если есть новые
            if (interiorImageUris.isNotEmpty()) {
                val interiorParts = interiorImageUris.mapIndexed { index, uri ->
                    val file = uri.toFile(requireContext())
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData(
                        "images",
                        "interior_${System.currentTimeMillis()}_$index.jpg",
                        requestFile
                    )
                }

                val response = ApiClient.apiService.uploadFiles(null, interiorParts)
                newInteriorUrls.addAll(response)
            }
        } catch (e: Exception) {
            throw Exception("Ошибка загрузки файлов: ${e.message}")
        }

        return Pair(newCoverUrl, newInteriorUrls)
    }

    private fun Uri.toFile(context: android.content.Context): File {
        val inputStream = context.contentResolver.openInputStream(this)!!
        val file = File.createTempFile("temp_", ".jpg", context.cacheDir)
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        return file
    }

    private fun createEditionRequest(
        coverImage: String?,
        interiorImages: List<String>
    ): EditionRequest {
        val genresText = binding.autoCompleteGenres.text.toString().trim()
        val genres = if (genresText.isNotEmpty()) {
            genresText.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() && it.isNotEmpty() }
        } else {
            currentEdition?.genres ?: emptyList()
        }

        return EditionRequest(
            title = binding.etTitle.text.toString(),
            authorFirstName = binding.etAuthorFirstName.text.toString(),
            authorLastName = binding.etAuthorLastName.text.toString(),
            authorMiddleName = binding.etAuthorMiddleName.text.toString().takeIf { it.isNotBlank() },
            description = binding.etDescription.text.toString().takeIf { it.isNotBlank() },
            coverImage = coverImage ?: "",
            genres = genres,
            interiorImages = interiorImages
        )
    }

    private fun deleteEdition(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.apiService.deleteEdition(id)
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Издание удалено", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Ошибка удаления: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}