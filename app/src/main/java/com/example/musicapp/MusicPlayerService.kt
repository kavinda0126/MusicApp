package com.example.musicapp

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import java.io.IOException

class MusicPlayerService : Service(), MediaPlayer.OnPreparedListener {

    inner class LocalBinder: Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

        private var mediaPlayer: MediaPlayer? = null
    private var currentTrack = 0
    private lateinit var trackList: List<Int>
    private var isPaused = false
    var nowPlaying: String = ""

    override fun onBind(intent: Intent): IBinder? {
        return LocalBinder()
    }

    override fun onCreate() {
        super.onCreate()
        trackList = listOf(R.raw.kaulu, R.raw.baby, R.raw.heal)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                "ACTION_PLAY" -> playTrack(currentTrack)
                "ACTION_PAUSE" -> pauseTrack()
                "ACTION_SKIP" -> skipTrack()
                "ACTION_STOP" -> {
                    stopTrack()
                    stopSelf() // Stop the service after stopping the track
                }
            }
        }
        // The return statement should be outside the when block to ensure it is always executed
        return START_NOT_STICKY
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()
    }

    private fun playTrack(trackIndex: Int) {
        val uri = Uri.parse("android.resource://$packageName/${trackList[trackIndex]}")
        nowPlaying = "Now Playing: Track ${trackIndex + 1}"

        if (isPaused) {
            mediaPlayer?.start() // Resume playback if the track was paused
            isPaused = false
        } else {
            // Release any previously created MediaPlayer to avoid resource leaks
            mediaPlayer?.release()
            // Create a new MediaPlayer instance and configure it manually
            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener(this@MusicPlayerService)
                setDataSource(this@MusicPlayerService, uri)
                prepareAsync() // Asynchronous preparation to avoid blocking the main thread
            }
        }
    }
    fun play(){
        playTrack(currentTrack)
    }


     fun pauseTrack() {
        // Check if mediaPlayer is currently playing to avoid IllegalStateException
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPaused = true
        }
    }

    fun skipTrack() {
        // Increment the track index and wrap around if necessary
        currentTrack = (currentTrack + 1) % trackList.size
        playTrack(currentTrack) // Play the next track
    }

     fun stopTrack() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop() // Stop the playback if the track is currently playing
            }
            it.release() // Release the MediaPlayer resources
        }
        mediaPlayer = null // Reset mediaPlayer to null
        isPaused = false // Reset the paused state
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // Ensure mediaPlayer is released when the service is destroyed
    }
}
