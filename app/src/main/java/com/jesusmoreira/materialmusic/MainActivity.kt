package com.jesusmoreira.materialmusic

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.jesusmoreira.materialmusic.controllers.AudioController
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.ui.library.SongsTabFragment
import com.jesusmoreira.materialmusic.ui.player.PlayerFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SongsTabFragment.OnSongListFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_library, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSongClicked(item: Audio?) {
        val playerView: FrameLayout = findViewById(R.id.player_fragment)
        supportFragmentManager.beginTransaction().replace(R.id.player_fragment, PlayerFragment.newInstance(item)).commit()
        playerView.visibility = View.VISIBLE
    }
}
