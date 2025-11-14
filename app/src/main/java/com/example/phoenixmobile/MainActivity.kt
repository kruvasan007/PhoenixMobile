package com.example.phoenixmobile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.phoenixmobile.R
import com.example.phoenixmobile.data.AuthManager
import com.example.phoenixmobile.databinding.ActivityMainBinding
import com.example.phoenixmobile.service.AudioTest
import com.example.phoenixmobile.service.CPUTest
import com.example.phoenixmobile.service.HardWareTest
import com.example.phoenixmobile.service.NetworkTest
import com.example.phoenixmobile.util.SettingsManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsManager.initialize(applicationContext)
        AuthManager.initialize(applicationContext)

        startService(Intent(this, HardWareTest::class.java))
        startService(Intent(this, NetworkTest::class.java))
        startService(Intent(this, CPUTest::class.java))
        startService(Intent(this, AudioTest::class.java))
        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_500)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Проверяем аутентификацию
        lifecycleScope.launch {
            AuthManager.isAuthenticated.collect { isAuthenticated ->
                if (!isAuthenticated) {
                    // Переходим на экран входа
                    try {
                        navController.navigate(R.id.navigation_auth)
                    } catch (_: Exception) {
                        // Если экран аутентификации недоступен, просто скрываем навигацию
                    }
                    navView.visibility = android.view.View.GONE
                } else {
                    navView.visibility = android.view.View.VISIBLE
                    // Переходим на основной экран если сейчас на экране аутентификации
                    try {
                        if (navController.currentDestination?.id == R.id.navigation_auth) {
                            navController.navigate(R.id.navigation_mydevice)
                        }
                    } catch (_: Exception) {
                        // Ignore navigation errors
                    }
                }
            }
        }

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_mydevice, R.id.navigation_chat
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onDestroy() {
        stopService(Intent(this, HardWareTest::class.java))
        stopService(Intent(this, NetworkTest::class.java))
        stopService(Intent(this, CPUTest::class.java))
        stopService(Intent(this, AudioTest::class.java))
        super.onDestroy()
    }
}