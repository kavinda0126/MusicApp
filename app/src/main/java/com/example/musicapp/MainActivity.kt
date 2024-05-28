package com.example.musicapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var playpauseBtn:ImageView
    private lateinit var nextBtn:ImageView
    private lateinit var musicTitle:TextView

    private var isPlaying = false
    private var isBound = false
    private lateinit var musicPlayerService: MusicPlayerService

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.LocalBinder
            musicPlayerService = binder.getService()
            isBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playpauseBtn=findViewById(R.id.playbtn)
        nextBtn=findViewById(R.id.nextbtn)
        musicTitle=findViewById(R.id.songname)


        playpauseBtn.setOnClickListener {
            if (!isPlaying){
                musicPlayerService.play()
                musicTitle.text = musicPlayerService.nowPlaying
                isPlaying = true
                playpauseBtn.setImageResource(R.drawable.pause)
            }else{
                musicPlayerService.pauseTrack()
                isPlaying = false
                playpauseBtn.setImageResource(R.drawable.play)
            }
        }

        nextBtn.setOnClickListener{
            musicPlayerService.skipTrack()
            musicTitle.text = musicPlayerService.nowPlaying
        }
    }



    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicPlayerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        Log.d("MainActivity","Service Started")
    }
    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }



}