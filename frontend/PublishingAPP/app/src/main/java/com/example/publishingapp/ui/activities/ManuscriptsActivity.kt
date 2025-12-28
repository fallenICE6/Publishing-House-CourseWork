package com.example.publishingapp.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.publishingapp.databinding.ActivityManuscriptsBinding
import com.example.publishingapp.ui.adapters.ManuscriptAdapter
import com.example.publishingapp.ui.viewmodels.ManuscriptsViewModel

class ManuscriptsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManuscriptsBinding
    private val viewModel: ManuscriptsViewModel by viewModels()
    private lateinit var adapter: ManuscriptAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManuscriptsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ManuscriptAdapter { manuscript ->
            val intent = Intent(this, ManuscriptDetailsActivity::class.java)
            intent.putExtra("id", manuscript.id)
            startActivity(intent)
        }

        binding.rvManuscripts.layoutManager = LinearLayoutManager(this)
        binding.rvManuscripts.adapter = adapter

        viewModel.manuscripts.observe(this) {
            adapter.submitList(it)
        }

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, SubmitManuscriptActivity::class.java))
        }
    }
}
