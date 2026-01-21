// WriteReviewFragment.kt
package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.publishingapp.R
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.CreateReviewRequest
import com.example.publishingapp.data.network.ReviewDto
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class WriteReviewFragment : Fragment() {

    companion object {
        private const val ARG_ORDER_ID = "order_id"

        fun newInstance(orderId: Long): WriteReviewFragment {
            return WriteReviewFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ORDER_ID, orderId)
                }
            }
        }
    }

    private var orderId: Long = -1


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_write_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderId = arguments?.getLong(ARG_ORDER_ID) ?: -1L
        if (orderId == -1L) {
            Toast.makeText(requireContext(), "Ошибка: не указан заказ", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return
        }

        val etComment = view.findViewById<EditText>(R.id.etComment)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupDecision)
        val rbApprove = view.findViewById<RadioButton>(R.id.rbApprove)
        val rbReject = view.findViewById<RadioButton>(R.id.rbReject)
        val rbRevision = view.findViewById<RadioButton>(R.id.rbRevision)
        val btnSubmit = view.findViewById<MaterialButton>(R.id.btnSubmit)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // Устанавливаем подсказки для radio buttons
        rbApprove.text = "Одобрить (статус: Готов к печати)"
        rbReject.text = "Отклонить (статус: Отменён)"
        rbRevision.text = "Отправить на доработку (статус: Редактируется)"

        btnSubmit.setOnClickListener {
            val comment = etComment.text.toString().trim()
            val selectedId = radioGroup.checkedRadioButtonId

            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Выберите решение", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val decision = when (selectedId) {
                R.id.rbApprove -> "approve"
                R.id.rbReject -> "reject"
                R.id.rbRevision -> "revision"
                else -> ""
            }

            progressBar.visibility = View.VISIBLE
            btnSubmit.isEnabled = false

            lifecycleScope.launch {
                try {
                    val request = CreateReviewRequest(
                        comment = if (comment.isBlank()) null else comment,
                        decision = decision,
                        orderStatusAfterReview = null
                    )

                    ApiClient.apiService.createReview(orderId, request)

                    Toast.makeText(
                        requireContext(),
                        when (decision) {
                            "approve" -> "Заказ одобрен и отправлен в печать"
                            "reject" -> "Заказ отклонён"
                            "revision" -> "Заказ отправлен на доработку"
                            else -> "Решение принято"
                        },
                        Toast.LENGTH_SHORT
                    ).show()

                    // Возвращаемся назад
                    requireActivity().onBackPressed()

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                } finally {
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                }
            }
        }
    }
}