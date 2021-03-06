package com.jesusmoreira.materialmusic.ui.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Album
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.ui.activities.MainActivity.Companion.ARG_AUDIO_LIST
import com.jesusmoreira.materialmusic.ui.activities.MainActivity.Companion.ARG_INDEX
import com.jesusmoreira.materialmusic.ui.activities.MainActivity.Companion.AUDIO_LIST_REQUEST
import com.jesusmoreira.materialmusic.ui.fragments.albums.AlbumDetailFragment
import com.jesusmoreira.materialmusic.ui.fragments.songs.SongListener
import com.jesusmoreira.materialmusic.utils.StorageUtil
import kotlinx.android.synthetic.main.album_detail_activity.*

class AlbumDetailActivity : AppCompatActivity(), SongListener {

    companion object {
        private const val ARG_ALBUM: String = "ARG_ALBUM"

        fun newIntent(context: Context, album: Album? = null) =
            Intent(context, AlbumDetailActivity::class.java).apply {
                if (album != null) {
                    putExtra(ARG_ALBUM, album)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.album_detail_activity)

        setSupportActionBar(toolbar)

//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, AlbumDetailFragment.newInstance())
//                .commitNow()
//        } else {
            intent?.extras?.getSerializable(ARG_ALBUM)?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, AlbumDetailFragment.newInstance(it as? Album))
                    .commitNow()
            }
//        }
    }

    override fun onSongClicked(audioList: ArrayList<Audio>, position: Int, preventShuffle: Boolean) {
        val intent = Intent().apply {
            putExtra(ARG_AUDIO_LIST, StorageUtil.audioListToString(audioList))
            putExtra(ARG_INDEX, position)
        }

        setResult(AUDIO_LIST_REQUEST, intent)
        finish()

//        startActivity(MainActivity.newIntent(applicationContext, audioList, position).apply {
//            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        })
    }
}
