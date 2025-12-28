package com.example.publishingapp.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.publishingapp.R
import com.example.publishingapp.ui.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        openFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { openFragment(HomeFragment()); true }
                R.id.nav_catalog -> { openFragment(CatalogFragment()); true }
                R.id.nav_works -> { openFragment(WorksFragment()); true }
                R.id.nav_profile -> { openFragment(ProfileFragment()); true }
                else -> false
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
    fun openCatalog() {
        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            .selectedItemId = R.id.nav_catalog
    }

    fun openWorks() {
        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            .selectedItemId = R.id.nav_works
    }


}
