package com.faraonelife.musicplayer

import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_music_player.*


class MusicPlayerActivity : AppCompatActivity() {
private var mediaPlayer : MediaPlayer?= null
    private lateinit var musicList: MutableList<Music>
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: MusicAdapter
    private var currPosition: Int=0
    private  var state: Boolean=false
    // false - stop
    // true -play

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)
        musicList = mutableListOf()
        if (Build.VERSION.SDK_INT >= 23)
            checkPermission()
        fab_play.setOnClickListener{
            play(currPosition)
        }
    }
    fun play(currPosition : Int){
        if (!state){
            fab_play.setImageDrawable(resources.getDrawable(R.drawable.ic_stop))
            state=true
            mediaPlayer=MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(this@MusicPlayerActivity, Uri.parse(musicList[currPosition].songUri))
                prepare()
                start()
            }
        }
        else{
            state=false
            mediaPlayer?.stop()
            fab_play.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow))

        }

    }

    private fun getSongs() {
        val selection = MediaStore.Audio.Media.IS_MUSIC
        val projection = arrayOf(
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA
        )
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
        while (cursor!!.moveToNext()) {
            musicList.add(Music(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
        }
        cursor.close()
        linearLayoutManager= LinearLayoutManager(this)
        adapter= MusicAdapter(musicList)
        recycler_view.layoutManager=linearLayoutManager
        recycler_view.adapter=adapter
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getSongs()
        }
        //false -> user asked not to ask me anymore permission
        //true->reject before
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Toast.makeText(this, "Music player need Access to your Files", Toast.LENGTH_SHORT)
                    .show()
            }
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_EXTERNAL_STORAGE
            )

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_READ_EXTERNAL_STORAGE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSongs()
                } else {
                    Toast.makeText(this, "Permission is not granted", Toast.LENGTH_SHORT).show()

                }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }
}