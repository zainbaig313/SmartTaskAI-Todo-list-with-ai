package com.example.smarttaskai

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        bottomNav = findViewById(R.id.bottomNavigationView)

        // Load the default fragment
        loadFragment(Schedule())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_schedule -> loadFragment(Schedule())
                R.id.navigation_todo -> loadFragment(Todo())
                R.id.navigation_priority -> loadFragment(Progress())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.homeContentFrame, fragment)
            .commit()
    }
}
