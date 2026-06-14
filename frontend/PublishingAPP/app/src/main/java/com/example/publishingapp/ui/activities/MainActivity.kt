package com.example.publishingapp.ui.activities

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.publishingapp.R
import com.example.publishingapp.data.network.AppPrefs
import com.example.publishingapp.data.repository.AuthRepository
import com.example.publishingapp.ui.fragments.*

class MainActivity : AppCompatActivity() {

    private var lastSelectedItemId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        AppPrefs.init(this)
        AuthRepository.init()

        setContentView(R.layout.activity_main)

        val navHome = findViewById<LinearLayout>(R.id.nav_home)
        val navCatalog = findViewById<LinearLayout>(R.id.nav_catalog)
        val navWorks = findViewById<LinearLayout>(R.id.nav_works)
        val navProfile = findViewById<LinearLayout>(R.id.nav_profile)

        // стартовый экран
        openFragment(HomeFragment())
        lastSelectedItemId = R.id.nav_home
        updateSelection(R.id.nav_home)

        navHome.setOnClickListener {
            if (lastSelectedItemId != R.id.nav_home) {
                openFragment(HomeFragment())
                lastSelectedItemId = R.id.nav_home
                updateSelection(R.id.nav_home)
            }
        }

        navCatalog.setOnClickListener {
            if (lastSelectedItemId != R.id.nav_catalog) {
                openFragment(CatalogFragment())
                lastSelectedItemId = R.id.nav_catalog
                updateSelection(R.id.nav_catalog)
            }
        }

        navWorks.setOnClickListener {
            if (lastSelectedItemId != R.id.nav_works) {
                openFragment(WorksFragment())
                lastSelectedItemId = R.id.nav_works
                updateSelection(R.id.nav_works)
            }
        }

        navProfile.setOnClickListener {
            if (lastSelectedItemId != R.id.nav_profile) {
                openFragment(ProfileFragment())
                lastSelectedItemId = R.id.nav_profile
                updateSelection(R.id.nav_profile)
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun updateSelection(selectedId: Int) {
        val items = listOf(
            R.id.nav_home,
            R.id.nav_catalog,
            R.id.nav_works,
            R.id.nav_profile
        )

        items.forEach { id ->
            val item = findViewById<LinearLayout>(id)
            item.isSelected = (id == selectedId)
        }
    }

    fun openCatalog() {
        openFragment(CatalogFragment())
        lastSelectedItemId = R.id.nav_catalog
        updateSelection(R.id.nav_catalog)
    }

    fun openWorks() {
        openFragment(WorksFragment())
        lastSelectedItemId = R.id.nav_works
        updateSelection(R.id.nav_works)
    }
}