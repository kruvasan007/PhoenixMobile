package com.example.phoenixmobile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.phoenixmobile.databinding.ActivityMainBinding
import com.example.phoenixmobile.service.AudioTest
import com.example.phoenixmobile.service.CPUTest
import com.example.phoenixmobile.service.HardWareCheck
import com.example.phoenixmobile.service.NetworkTest
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startService(Intent(this, HardWareCheck::class.java))
        startService(Intent(this, NetworkTest::class.java))
        startService(Intent(this, CPUTest::class.java))
        startService(Intent(this, AudioTest::class.java))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_mydevice, R.id.navigation_search
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onDestroy() {
        stopService(Intent(this, HardWareCheck::class.java))
        stopService(Intent(this, NetworkTest::class.java))
        stopService(Intent(this, CPUTest::class.java))
        stopService(Intent(this, AudioTest::class.java))
        super.onDestroy()
    }
}