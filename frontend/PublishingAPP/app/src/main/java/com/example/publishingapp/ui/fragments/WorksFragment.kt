package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.publishingapp.R
import com.example.publishingapp.databinding.FragmentWorksBinding
import com.example.publishingapp.ui.adapters.EditionAdapter
import com.example.publishingapp.ui.viewmodels.CatalogWorksViewModel

class WorksFragment : Fragment(R.layout.fragment_works) {

    private lateinit var binding: FragmentWorksBinding
    private val vm: CatalogWorksViewModel by activityViewModels() // Общий ViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentWorksBinding.bind(view)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        val adapter = EditionAdapter(emptyList()) { editionId ->
            // Переход на детальный экран через Bundle
            val bundle = Bundle().apply { putInt("editionId", editionId) }
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, EditionDetailFragment().apply { arguments = bundle })
                .addToBackStack(null)
                .commit()
        }
        binding.recyclerView.adapter = adapter

        // Фильтр по жанрам
        vm.genres.observe(viewLifecycleOwner) { genres ->
            val list = listOf("Все") + genres
            val spinnerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                list
            )
            binding.genreSpinner.setAdapter(spinnerAdapter)
        }

        // Список книг
        vm.filtered.observe(viewLifecycleOwner) { editions ->
            adapter.submitList(editions)
        }

        // Обработка выбора жанра
        binding.genreSpinner.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as String
            vm.filterByGenre(if (selected == "Все") null else selected)
        }

        // Загрузка mock-данных
        vm.load()
    }
}
