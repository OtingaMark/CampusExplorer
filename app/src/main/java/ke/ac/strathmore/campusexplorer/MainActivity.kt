package ke.ac.strathmore.campusexplorer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import ke.ac.strathmore.campusexplorer.databinding.ActivityMainBinding

/**
 * The only Activity in the app. It hosts four fragments and a BottomNavigationView
 * (the tabs at the bottom) that switches between them.
 *
 *   Places     -> RecyclerView + Firebase (Cloud Firestore)
 *   Map        -> Google Maps API
 *   Weather    -> Internet connection (REST API with Retrofit)
 *   Broadcasts -> BroadcastReceivers
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Keep the toolbar clear of the status bar. (The bottom navigation view
        // pads itself above the system navigation bar automatically.)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(left = bars.left, top = bars.top, right = bars.right)
            insets
        }

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController

        // All four tabs are "top level": no back arrow is shown in the toolbar.
        val topLevelDestinations = setOf(
            R.id.placesFragment, R.id.mapFragment, R.id.weatherFragment, R.id.broadcastsFragment
        )
        setupActionBarWithNavController(navController, AppBarConfiguration(topLevelDestinations))

        // This one line connects the bottom tabs to the navigation graph. It works because
        // the menu item ids (res/menu/bottom_nav_menu.xml) match the fragment ids
        // (res/navigation/nav_graph.xml).
        binding.bottomNav.setupWithNavController(navController)
    }
}
