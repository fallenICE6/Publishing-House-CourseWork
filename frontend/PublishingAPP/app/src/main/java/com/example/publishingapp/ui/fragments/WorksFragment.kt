package com.example.publishingapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class WorksFragment : Fragment(R.layout.fragment_works) {

    private lateinit var binding: FragmentWorksBinding
    private val vm: CatalogWorksViewModel by activityViewModels {
        CatalogWorksViewModelFactory(
            EditionRepository(
                ApiClient.apiService,
                requireContext()
            )
        )
    }

    private lateinit var adapter: EditionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWorksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = EditionAdapter(emptyList()) { editionId ->
            val bundle = Bundle().apply { putLong("editionId", editionId) }
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, EditionDetailFragment().apply { arguments = bundle })
                .addToBackStack(null)
                .commit()
        }

        binding.recyclerView.adapter = adapter

        vm.filtered.observe(viewLifecycleOwner) { editions ->
            adapter.submitList(editions)
            updateGenreFilter(editions)
        }

        binding.genreSpinner.setOnItemClickListener { parent, _, position, _ ->
            val selectedGenre = parent.getItemAtPosition(position) as String
            filterByGenre(selectedGenre)
        }

        binding.genreSpinner.setOnEditorActionListener { _, _, _ ->
            val input = binding.genreSpinner.text.toString().trim()
            if (input.isNotEmpty()) {
                filterByGenre(input)
            }
            true
        }

        vm.load()
        checkUserRole()
    }

    private fun checkUserRole() {
        // Используйте AppPrefs вместо SharedPreferences напрямую
        val token = AppPrefs.getToken()

        if (token != null && isAdmin(token)) {
            showAdminFab()
            adapter.setOnLongClickListener { edition ->
                showAdminActionsDialog(edition)
            }

            // Для отладки
            println("DEBUG: Admin detected, showing admin buttons")
        } else {
            println("DEBUG: Not admin or no token. Token exists: ${token != null}")
            if (token != null) {
                println("DEBUG: Token exists, checking role...")
                println("DEBUG: Is admin: ${isAdmin(token)}")
            }
        }
    }

    private fun isAdmin(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size < 3) {
                println("DEBUG: Token has less than 3 parts: ${parts.size}")
                return false
            }

            val payload = parts[1]
            // Base64 decode без добавления padding
            val decoded = String(Base64.decode(payload, Base64.URL_SAFE))
            val json = JSONObject(decoded)
            val role = json.optString("role", "")

            println("DEBUG: Parsed role from JWT: '$role'")

            val isAdmin = role.equals("ADMIN", ignoreCase = true) ||
                    role.contains("ADMIN", ignoreCase = true) ||
                    role.equals("ROLE_ADMIN", ignoreCase = true)

            println("DEBUG: Is admin result: $isAdmin")
            isAdmin
        } catch (e: Exception) {
            println("DEBUG: Error parsing JWT: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun showAdminFab() {
        val fab = FloatingActionButton(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
                setMargins(0, 0, 32, 20)
            }
            setImageResource(android.R.drawable.ic_menu_add)
            setOnClickListener {
                openAdminFragment(-1L)
            }
        }

        (binding.root as? FrameLayout)?.addView(fab) ?: run {
            val root = binding.root as? ViewGroup
            root?.addView(fab)
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
                    vm.load() // Перезагрузить список
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Ошибка удаления: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun filterByGenre(genre: String) {
        val allGenres = vm.filtered.value?.flatMap { it.genres }?.distinct() ?: emptyList()
        if (genre == "Все" || genre in allGenres) {
            vm.filterByGenre(if (genre == "Все") null else genre)
        } else {
            Toast.makeText(requireContext(), "Жанр не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateGenreFilter(editions: List<Edition>) {
        val genres = editions.flatMap { it.genres }.distinct()
        val list = listOf("Все") + genres
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            list
        )
        binding.genreSpinner.setAdapter(spinnerAdapter)
    }
}