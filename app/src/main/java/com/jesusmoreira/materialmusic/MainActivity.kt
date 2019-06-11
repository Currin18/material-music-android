package com.jesusmoreira.materialmusic

//import kotlinx.coroutines.experimental.async
//import kotlinx.coroutines.experimental.launch
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.ui.library.SongsTabFragment
import com.jesusmoreira.materialmusic.ui.player.PlayerFragment
import com.mtechviral.mplaylib.MusicFinder
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), SongsTabFragment.OnSongListFragmentInteractionListener {

    private var songs: List<MusicFinder.Song>? = null
    private var mediaPlayer: MediaPlayer? = null

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        } else {
            createPlayer()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createPlayer()
        } else {
            longToast("Permission not granted. Shutting down.")
            finish()
        }
    }

    private fun createPlayer() {

        var songsJob = async {
            val songFinder = MusicFinder(contentResolver)
            songFinder.prepare()
            songFinder.allSongs
        }

        launch(kotlinx.coroutines.android.UI) {
            songs = songsJob.await()

//            val playerUI = object: AnkoComponent<MainActivity> {
//                override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
//                    relativeLayout{
//                        backgroundColor = Color.BLACK
//
//                        albumArt = imageView{
//                            scaleType = ImageView.ScaleType.FIT_CENTER
//                        }.lparams(matchParent, matchParent)
//
//                        verticalLayout{
//                            backgroundColor = Color.parseColor("#99000000")
//                            songTitle = textView{
//                                textColor = Color.WHITE
//                                typeface = Typeface.DEFAULT_BOLD
//                                textSize = 18f
//                            }
//
//                            songArtist = textView{
//                                textColor = Color.WHITE
//                            }
//
//                            linearLayout{
//                                playButton = imageButton{
//                                    imageResource = R.drawable.ic_play_arrow_black_24dp
//                                    onClick {
//                                        playOrPause()
//                                    }
//                                }.lparams(0, wrapContent,0.5f)
//
//                                shuffleButton = imageButton{
//                                    imageResource = R.drawable.ic_shuffle_black_24dp
//                                    onClick {
//                                        playRandom()
//                                    }
//                                }.lparams(0, wrapContent,0.5f)
//                            }.lparams(matchParent, wrapContent){
//                                topMargin = dip(5)
//                            }
//
//
//
//                        }.lparams(matchParent, wrapContent){
//                            alignParentBottom()
//                        }
//                    }
//                }
//            }
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }

    private fun shortToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
    private fun longToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    override fun onSongClicked(item: Audio) {
        val playerView: FrameLayout = findViewById(R.id.player_fragment)
        supportFragmentManager.beginTransaction().replace(R.id.player_fragment, PlayerFragment.newInstance(item)).commit()
        playerView.visibility = View.VISIBLE

//        songs?.get(0)?.let { startSong(it) }
        startSong(item)
    }

    private fun startSong(song: Audio) {
        mediaPlayer?.reset()
        song.getURI()?.let { uri ->
            mediaPlayer = MediaPlayer.create(this, uri)
        }
        mediaPlayer?.setOnCompletionListener {

        }

        mediaPlayer?.start()
    }

    private fun playOrStart() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            // change button to start
        } else {
            mediaPlayer?.start()
            // change button to pause
        }
    }
}
