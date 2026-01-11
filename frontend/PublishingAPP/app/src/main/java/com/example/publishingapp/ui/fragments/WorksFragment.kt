package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.publishingapp.R
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.ApiService
import com.example.publishingapp.data.repository.EditionRepository
import com.example.publishingapp.databinding.FragmentWorksBinding
import com.example.publishingapp.ui.adapters.EditionAdapter
import com.example.publishingapp.ui.viewmodels.CatalogWorksViewModel
import com.example.publishingapp.ui.viewmodels.CatalogWorksViewModelFactory

class WorksFragment : Fragment(R.layout.fragment_works) {

    private lateinit var binding: FragmentWorksBinding
    private val vm: CatalogWorksViewModel by activityViewModels {
        CatalogWorksViewModelFactory(
            EditionRepository(
                ApiClient.apiService)
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentWorksBinding.bind(view)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        val adapter = EditionAdapter(emptyList()) { editionId ->
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
    }

    private fun filterByGenre(genre: String) {
        val allGenres = vm.filtered.value?.flatMap { it.genres }?.distinct() ?: emptyList()
        if (genre in allGenres) {
            vm.filterByGenre(genre)
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

