package com.shop.eatatease

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import com.google.android.material.navigation.NavigationView
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.shop.eatatease.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var keepSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        Handler(Looper.getMainLooper()).postDelayed({
            keepSplashScreen = false
        }, 500)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)


        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment?)!!
        val navController = navHostFragment.navController

        binding.navView?.let {
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_transform, R.id.nav_reflow, R.id.nav_slideshow, R.id.nav_settings
                ),
                binding.drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            it.setupWithNavController(navController)
            it.setNavigationItemSelectedListener { item ->
                if (item.itemId == navController.currentDestination?.id) {
                    binding.drawerLayout?.closeDrawers()
                    return@setNavigationItemSelectedListener true
                }
                val builder = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setEnterAnim(R.anim.fade_in)
                    .setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in)
                    .setPopExitAnim(R.anim.fade_out)
                
                val startDestinationId = navController.graph.findStartDestination().id
                builder.setPopUpTo(
                    startDestinationId,
                    inclusive = false,
                    saveState = true
                )
                val options = builder.build()
                try {
                    navController.navigate(item.itemId, null, options)
                    binding.drawerLayout?.closeDrawers()
                    true
                } catch (e: IllegalArgumentException) {
                    false
                }
            }
        }

        binding.appBarMain.contentMain.bottomNavView?.let {
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_transform, R.id.nav_reflow, R.id.nav_profile
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            it.setupWithNavController(navController)
            it.setOnItemSelectedListener { item ->
                if (item.itemId == navController.currentDestination?.id) {
                    return@setOnItemSelectedListener true
                }
                val builder = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setEnterAnim(R.anim.fade_in)
                    .setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in)
                    .setPopExitAnim(R.anim.fade_out)
                
                val startDestinationId = navController.graph.findStartDestination().id
                builder.setPopUpTo(
                    startDestinationId,
                    inclusive = false,
                    saveState = true
                )
                val options = builder.build()
                try {
                    navController.navigate(item.itemId, null, options)
                    true
                } catch (e: IllegalArgumentException) {
                    false
                }
            }
        }

        // Hide toolbar and bottom nav on product detail and checkout pages
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_product_detail, R.id.nav_checkout -> {
                    supportActionBar?.hide()
                    binding.appBarMain.contentMain.bottomNavView?.visibility = android.view.View.GONE
                }
                else -> {
                    supportActionBar?.show()
                    binding.appBarMain.contentMain.bottomNavView?.visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        // Using findViewById because NavigationView exists in different layout files
        // between w600dp and w1240dp
        val navView: NavigationView? = findViewById(R.id.nav_view)
        if (navView == null) {
            // The navigation drawer already has the items including the items in the overflow menu
            // We only inflate the overflow menu if the navigation drawer isn't visible
            menuInflater.inflate(R.menu.overflow, menu)
        }
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                val options = NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build()
                navController.navigate(R.id.nav_settings, null, options)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
