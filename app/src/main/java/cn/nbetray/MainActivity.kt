package cn.nbetray

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import cn.nbetray.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Define top-level destinations (no back button shown)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_proxies,
                R.id.navigation_rules,
                R.id.navigation_connections,
                R.id.navigation_logs
            )
        )

        // Setup toolbar with navigation (without using ActionBar)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        // Navigation options for config/backend pages - don't stack, just replace
        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(navController.graph.startDestinationId, false)
            .build()

        // Handle toolbar menu clicks
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            val currentDestination = navController.currentDestination?.id
            when (menuItem.itemId) {
                R.id.action_config -> {
                    if (currentDestination != R.id.navigation_config) {
                        navController.navigate(R.id.navigation_config, null, navOptions)
                    }
                    true
                }
                R.id.action_backend -> {
                    if (currentDestination != R.id.navigation_backend) {
                        navController.navigate(R.id.navigation_backend, null, navOptions)
                    }
                    true
                }
                R.id.action_about -> {
                    showAboutDialog()
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun showAboutDialog() {
        val version = packageManager.getPackageInfo(packageName, 0).versionName
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.about)
            .setMessage(getString(R.string.version, version))
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}
