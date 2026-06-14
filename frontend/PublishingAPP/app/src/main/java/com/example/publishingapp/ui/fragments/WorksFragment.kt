package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.publishingapp.R
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.databinding.FragmentWorksBinding
import com.example.publishingapp.ui.adapters.EditionAdapter
import com.example.publishingapp.ui.viewmodels.CatalogWorksViewModel
import com.example.publishingapp.data.repository.EditionRepository
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.AppPrefs
import com.example.publishingapp.ui.viewmodels.CatalogWorksViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


class WorksFragment : Fragment(R.layout.fragment_works) {

    private lateinit var binding: FragmentWorksBinding

    private val vm: CatalogWorksViewModel by activityViewModels {
        CatalogWorksViewModelFactory(
            EditionRepository(ApiClient.apiService, requireContext())
        )
    }

    private lateinit var adapter: EditionAdapter

    private val selectedGenres = mutableSetOf<String>()
    private var isAdminUser = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentWorksBinding.bind(view)

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = EditionAdapter(emptyList()) { editionId ->
            if (editionId == -1L) {
                openAdminFragment(-1L)
            } else {
                openDetails(editionId)
            }
        }

        binding.recyclerView.adapter = adapter

        vm.editions.observe(viewLifecycleOwner) {
            applyFilter()
        }

        vm.genres.observe(viewLifecycleOwner) {
            setupAutocomplete(it)
        }

        setupInput()

        vm.load()
        checkUserRole()
    }

    private fun setupInput() {
        binding.genreInput.setOnItemClickListener { parent, _, position, _ ->
            val genre = parent.getItemAtPosition(position) as String
            addGenre(genre)
            binding.genreInput.setText("")
        }
    }

    private fun setupAutocomplete(list: List<String>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            list
        )
        binding.genreInput.setAdapter(adapter)
    }

    private fun addGenre(genre: String) {
        if (selectedGenres.contains(genre)) return
        selectedGenres.add(genre)
        renderChips()
        applyFilter()
    }

    private fun removeGenre(genre: String) {
        selectedGenres.remove(genre)
        renderChips()
        applyFilter()
    }

    private fun renderChips() {
        binding.chipsContainer.removeAllViews()

        selectedGenres.forEach { genre ->
            val chip = layoutInflater.inflate(
                R.layout.item_chip,
                binding.chipsContainer,
                false
            ) as TextView

            chip.text = "✕ $genre"

            chip.setOnClickListener {
                removeGenre(genre)
            }

            binding.chipsContainer.addView(chip)
        }
    }

    private fun applyFilter() {
        val list = vm.editions.value ?: return

        val filtered = if (selectedGenres.isEmpty()) {
            list
        } else {
            list.filter { edition ->
                selectedGenres.all { edition.genres.contains(it) }
            }
        }

        adapter.submitList(filtered)
    }

    private fun openDetails(id: Long) {
        val bundle = Bundle().apply { putLong("editionId", id) }

        parentFragmentManager.beginTransaction()
            .replace(R.id.container, EditionDetailFragment().apply {
                arguments = bundle
            })
            .addToBackStack(null)
            .commit()
    }

    private fun checkUserRole() {
        val token = AppPrefs.getToken()

        if (token != null && isAdmin(token)) {
            isAdminUser = true
            adapter.setAdmin(true)

            adapter.setOnLongClickListener { edition ->
                showAdminActionsDialog(edition)
            }
        }
    }

    private fun isAdmin(token: String): Boolean {
        return try {
            val payload = token.split(".")[1]
            val decoded = String(Base64.decode(payload, Base64.URL_SAFE))
            val json = JSONObject(decoded)
            val role = json.optString("role", "")
            role.contains("ADMIN", true)
        } catch (e: Exception) {
            false
        }
    }

    private fun showAdminActionsDialog(edition: Edition) {
        AlertDialog.Builder(requireContext())
            .setTitle("Админ действия")
            .setItems(arrayOf("✏️ Редактировать", "🗑 Удалить")) { _, which ->
                when (which) {
                    0 -> openAdminFragment(edition.id)
                    1 -> confirmDelete(edition)
                }
            }
            .show()
    }

    private fun openAdminFragment(editionId: Long) {
        val fragment = AdminEditionFragment.newInstance(editionId)

        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun confirmDelete(edition: Edition) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление")
            .setMessage("Удалить \"${edition.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteEdition(edition.id)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteEdition(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.apiService.deleteEdition(id)

                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Удалено", Toast.LENGTH_SHORT).show()
                    vm.load()
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}