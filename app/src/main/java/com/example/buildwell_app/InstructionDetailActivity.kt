package com.example.buildwell_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class InstructionDetailActivity : AppCompatActivity() {

    private lateinit var instructionDetailTextView: TextView
    private lateinit var youtubePlayerView: YouTubePlayerView

    private var currentVideoId: String? = null

    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction_detail)

        instructionDetailTextView = findViewById(R.id.instructionDetailTextView)
        youtubePlayerView = findViewById(R.id.youtube_player_view)

        lifecycle.addObserver(youtubePlayerView) // Important!

        val instructions = intent.getStringExtra("instructions")
        currentVideoId = intent.getStringExtra("youtubeVideoId")
        Log.d("InstructionDetail", "Received video ID: $currentVideoId")

        if (instructions != null) {
            instructionDetailTextView.text = instructions
        } else {
            instructionDetailTextView.text = "Instructions not available."
        }

        currentVideoId?.let { videoId ->
            playYouTubeVideo(videoId)
        }
    }

    // Optional: Handle WebView lifecycle methods
    private fun playYouTubeVideo(videoId: String) {
        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.cueVideo(videoId, 0f) // or use loadVideo(videoId, 0f) to auto-play
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        youtubePlayerView.release()
    }


}