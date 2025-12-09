package com.newthingwidgets.clone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.newthingwidgets.clone.fragments.WidgetsFragment
import com.newthingwidgets.clone.fragments.SettingsFragment
import com.newthingwidgets.clone.fragments.WallpapersFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(WidgetsFragment())
            bottomNavigation.selectedItemId = R.id.navigation_widgets
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                R.id.navigation_widgets -> {
                    loadFragment(WidgetsFragment())
                    true
                }
                R.id.navigation_wallpapers -> {
                    loadFragment(WallpapersFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
