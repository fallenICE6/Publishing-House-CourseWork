package com.example.publishingapp.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.publishingapp.data.repository.MockManuscriptRepository
import com.example.publishingapp.databinding.ActivityManuscriptDetailsBinding

class ManuscriptDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManuscriptDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManuscriptDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getLongExtra("id", -1)

        val manuscript = MockManuscriptRepository.getById(id)

        manuscript?.let {
            binding.tvTitle.text = it.title
            binding.tvAnnotation.text = it.annotation
            binding.tvPages.text = "Страниц: ${it.pages}"
            binding.tvStatus.text = "Статус: ${it.status}"
        }
    }
}
