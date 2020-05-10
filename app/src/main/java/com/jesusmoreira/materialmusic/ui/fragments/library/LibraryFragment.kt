package com.jesusmoreira.materialmusic.ui.fragments.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.controllers.MediaController
import com.jesusmoreira.materialmusic.ui.fragments.albums.TabAlbumsFragment
import com.jesusmoreira.materialmusic.ui.fragments.artists.TabArtistsFragment
import com.jesusmoreira.materialmusic.ui.fragments.songs.TabSongsFragment

class LibraryFragment : Fragment() {

    private lateinit var libraryViewModel: LibraryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        libraryViewModel =
            ViewModelProvider(this).get(LibraryViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_library, container, false)

//        val textView: TextView = root.findViewById(R.id.text_home)
//        libraryViewModel.text.observe(this, Observer {
//            textView.text = it
//        })

        context?.apply {
            libraryViewModel.mediaController = MediaController(this)
        }

        val tabLayout: TabLayout = root.findViewById(R.id.tab_layout)

        val viewPager: ViewPager = root.findViewById(R.id.view_pager)
        parentFragmentManager.apply {
            viewPager.adapter = ViewPagerAdapter(this, tabLayout)
        }

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) { tab?.position?.let { viewPager.currentItem = it } }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) { tab?.position?.let { viewPager.currentItem = it } }
        })

        return root
    }

    class ViewPagerAdapter(fragmentManager: FragmentManager, private val tabLayout: TabLayout):
        FragmentStatePagerAdapter(
            fragmentManager, FragmentStatePagerAdapter.
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            private var tabSongsFragment: TabSongsFragment? = null
            private var tabAlbumsFragment: TabAlbumsFragment? = null
            private var tabArtistsFragment: TabArtistsFragment? = null

            override fun getItem(position: Int): Fragment {
                return when(position) {
                    0 -> {
                        if (tabSongsFragment == null)
                            tabSongsFragment = TabSongsFragment.newInstance()
                        tabSongsFragment!!
                    }
                    1 ->  {
                        if (tabAlbumsFragment == null)
                            tabAlbumsFragment = TabAlbumsFragment.newInstance()
                        tabAlbumsFragment!!
                    }
                    2 -> {
                        if (tabArtistsFragment == null)
                            tabArtistsFragment = TabArtistsFragment.newInstance()
                        tabArtistsFragment!!
                    }
                    else ->  TabSongsFragment.newInstance()
                }
            }

            override fun getCount(): Int = tabLayout.tabCount
        }
}
