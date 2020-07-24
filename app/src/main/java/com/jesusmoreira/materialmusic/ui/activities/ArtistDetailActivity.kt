package com.jesusmoreira.materialmusic.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Album
import com.jesusmoreira.materialmusic.models.Artist
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.ui.fragments.albums.AlbumListener
import com.jesusmoreira.materialmusic.ui.fragments.artists.ArtistDetailFragment
import com.jesusmoreira.materialmusic.ui.fragments.songs.SongListener
import com.jesusmoreira.materialmusic.utils.StorageUtil
import kotlinx.android.synthetic.main.artist_detail_activity.*

class ArtistDetailActivity : AppCompatActivity(), SongListener, AlbumListener {

    companion object {
        private const val ARG_ARTIST: String = "ARG_ARTIST"

        fun newIntent(context: Context, artist: Artist? = null) =
            Intent(context, ArtistDetailActivity::class.java).apply {
                if (artist != null) {
                    putExtra(ARG_ARTIST, artist)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.artist_detail_activity)

        setSupportActionBar(toolbar)

        intent?.extras?.getSerializable(ARG_ARTIST)?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ArtistDetailFragment.newInstance(it as? Artist))
                .commitNow()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MainActivity.AUDIO_LIST_REQUEST -> {
                setResult(MainActivity.AUDIO_LIST_REQUEST, data)
                finish()
            }
        }
    }

    override fun onSongClicked(audioList: ArrayList<Audio>, position: Int, preventShuffle: Boolean) {
        val intent = Intent().apply {
            putExtra(MainActivity.ARG_AUDIO_LIST, StorageUtil.audioListToString(audioList))
            putExtra(MainActivity.ARG_INDEX, position)
        }

        setResult(MainActivity.AUDIO_LIST_REQUEST, intent)
        finish()
    }

    override fun onAlbumClicked(album: Album) {
        startActivityForResult(AlbumDetailActivity.newIntent(applicationContext, album),
            MainActivity.AUDIO_LIST_REQUEST
        )
    }
}
