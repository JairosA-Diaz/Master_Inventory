package com.example.equipoOcho.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.equipoOcho.R
import com.example.equipoOcho.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navigationContainer) as NavHostFragment
        navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        val fromWidget = intent?.getBooleanExtra("open_login_from_widget", false) ?: false

        navGraph.setStartDestination(
            when {
                SessionManager.isSessionActive(this) -> R.id.homeInventoryFragment
                else -> R.id.loginFragment
            }
        )

        navController.graph = navGraph

        // Si viene del widget y no hay sesión, igual cae en el loginFragment,
        // así que no necesitamos cerrar nada aquí.
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val fromWidget = intent.getBooleanExtra("open_login_from_widget", false)
        if (fromWidget && ::navController.isInitialized) {
            // Si el widget pide abrir login, lo llevamos allá
            if (navController.currentDestination?.id != R.id.loginFragment) {
                navController.navigate(R.id.loginFragment)
            }
        }
        // Ya NO hacemos finish() si !fromWidget
    }
}