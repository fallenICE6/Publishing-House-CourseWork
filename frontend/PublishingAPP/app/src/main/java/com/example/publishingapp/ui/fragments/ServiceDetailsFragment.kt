package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.example.publishingapp.R
import com.example.publishingapp.data.network.AppPrefs
import com.example.publishingapp.data.network.NetworkConstants
import com.example.publishingapp.databinding.FragmentServiceDetailsBinding
import com.example.publishingapp.ui.viewmodels.ServiceDetailsViewModel

class ServiceDetailsFragment : Fragment(R.layout.fragment_service_details) {

    companion object {
        private const val ARG_SERVICE_ID = "service_id"

        fun newInstance(serviceId: Long): ServiceDetailsFragment {
            return ServiceDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_SERVICE_ID, serviceId)
                }
            }
        }
    }

    private var _binding: FragmentServiceDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ServiceDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentServiceDetailsBinding.bind(view)

        val serviceId = requireArguments().getLong(ARG_SERVICE_ID)
        viewModel.loadService(serviceId)

        viewModel.service.observe(viewLifecycleOwner) { service ->
            binding.tvTitle.text = service.title
            binding.tvDescription.text = service.fullDescription
            binding.tvPrice.text = "${service.price} ₽"

            binding.ivImage.load(
                "${NetworkConstants.BASE_URL}images/${service.image}"
            ) {
                placeholder(R.drawable.loading)
                error(R.drawable.loading)
            }
        }

        updateAuthState()
    }

    override fun onResume() {
        super.onResume()
        updateAuthState()
    }

    private fun updateAuthState() {
        val loggedIn = !AppPrefs.getToken().isNullOrEmpty()

        binding.btnOrder.visibility = if (loggedIn) View.VISIBLE else View.GONE
        binding.tvLoginRequired.visibility = if (loggedIn) View.GONE else View.VISIBLE

        binding.btnOrder.setOnClickListener {
            val fragment = ServiceOrderFragment.newInstance(viewModel.service.value!!)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
