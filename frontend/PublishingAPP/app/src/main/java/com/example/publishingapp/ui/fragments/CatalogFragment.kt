package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.ui.adapters.ServicesAdapter
import com.example.publishingapp.ui.viewmodels.CatalogViewModel

class CatalogFragment : Fragment(R.layout.activity_catalog) {

    private val viewModel: CatalogViewModel by viewModels()

    private lateinit var adapter: ServicesAdapter
    private lateinit var etSearch: EditText
    private lateinit var tvCancel: TextView
    private lateinit var emptyContainer: View

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvServices)

        etSearch = view.findViewById(R.id.etSearch)
        tvCancel = view.findViewById(R.id.tvCancel)
        emptyContainer = view.findViewById(R.id.emptyContainer)

        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = ServicesAdapter { service ->
            openServiceDetails(service.id)
        }

        rv.adapter = adapter

        adapter.setOnEmptyChangedListener { isEmpty ->
            if (isEmpty) {
                emptyContainer.alpha = 0f
                emptyContainer.visibility = View.VISIBLE
                emptyContainer.animate().alpha(1f).setDuration(200).start()
            } else {
                emptyContainer.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction {
                        emptyContainer.visibility = View.GONE
                    }
                    .start()
            }
        }

        viewModel.services.observe(viewLifecycleOwner) { list ->
            adapter.setData(list)
        }

        viewModel.loadServices()

        etSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

                searchRunnable?.let { handler.removeCallbacks(it) }

                searchRunnable = Runnable {
                    val query = s.toString()
                    adapter.search(query)
                }

                handler.postDelayed(searchRunnable!!, 300)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                tvCancel.visibility = View.VISIBLE
                tvCancel.alpha = 0f
                tvCancel.animate().alpha(1f).setDuration(200).start()
            } else {
                tvCancel.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction {
                        tvCancel.visibility = View.GONE
                    }
                    .start()
            }
        }

        tvCancel.setOnClickListener {
            etSearch.setText("")
            etSearch.clearFocus()

            adapter.reset()

            emptyContainer.visibility = View.GONE
            tvCancel.visibility = View.GONE
        }
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