package com.jesusmoreira.materialmusic.ui.podcast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jesusmoreira.materialmusic.R

class PodcastFragment : Fragment() {

    private lateinit var podcastViewModel: PodcastViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        podcastViewModel =
            ViewModelProviders.of(this).get(PodcastViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_podcast, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        podcastViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}