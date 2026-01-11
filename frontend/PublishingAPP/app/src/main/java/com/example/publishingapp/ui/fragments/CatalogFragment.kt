package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.ServiceDto
import com.example.publishingapp.ui.adapters.ServicesAdapter
import com.example.publishingapp.ui.viewmodels.CatalogViewModel

class CatalogFragment : Fragment(R.layout.activity_catalog) {

    private val viewModel: CatalogViewModel by viewModels()
    private lateinit var adapter: ServicesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvServices)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = ServicesAdapter(emptyList()) { service ->
            openServiceDetails(service.id)
        }

        rv.adapter = adapter

        viewModel.services.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }

        viewModel.loadServices()
    }

    private fun openServiceDetails(serviceId: Long) {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                ServiceDetailsFragment.newInstance(serviceId)
            )
            .addToBackStack(null)
            .commit()
    }
}


