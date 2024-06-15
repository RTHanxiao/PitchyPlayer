package com.example.pitchdetect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class MusicPlayerActivity extends AppCompatActivity {
    TextView songname, songdesc;
    ImageButton prev_btn, play_btn, next_btn;
    ImageView albumArt;
    MediaPlayer mediaPlayer = new MediaPlayer();
    int songPos;
    int songID;
    int songCount;
    SQLiteDatabase db;
    Cursor cursor;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        //载入数据库
        SongListDatabaseHelper dbHelper = new SongListDatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        //载入歌曲信息
        songname = findViewById(R.id.songname);
        songdesc = findViewById(R.id.songdesc);
        albumArt = findViewById(R.id.imageView3);
        play_btn = findViewById(R.id.imageButton2);
        next_btn = findViewById(R.id.imageButton4);
        prev_btn = findViewById(R.id.imageButton3);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Intent intent = getIntent();
        songID = Integer.parseInt(intent.getStringExtra("songID"));

        String songName = intent.getStringExtra("songName");
        String songDesc = intent.getStringExtra("songDesc");
        String songSrc = intent.getStringExtra("songSrc");

        cursor = db.rawQuery("SELECT * FROM songlist", null);

        Cursor mCount = db.rawQuery("SELECT count(*) FROM songlist", null);
        mCount.moveToFirst();
        songCount = mCount.getInt(0);
        mCount.close();

        songname.setText(songName);
        songdesc.setText(songDesc);

        // 设置专辑封面
        setAlbumArt(songSrc);

        try {
            Log.d("MusicPlayerActivity", "Trying to play: " + songSrc);
            if (new File(songSrc).exists()) {
                mediaPlayer.setDataSource(songSrc);
                mediaPlayer.prepare();
            } else {
                Toast.makeText(this, "File does not exist: " + songSrc, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    play_btn.setImageResource(R.drawable.ic_outline_play_arrow_56);
                    mediaPlayer.pause();
                } else {
                    play_btn.setImageResource(R.drawable.ic_outline_pause_56);
                    mediaPlayer.start();
                }
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });

        prev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevSong();
            }
        });
    }

    private void playNextSong() {
        songID++;
        if (songID > songCount) {
            songID = 1;
        }
        songPos = songID - 1;
        playSongAtPosition(songPos);
    }

    private void playPrevSong() {
        songID--;
        if (songID < 1) {
            songID = songCount;
        }
        songPos = songID - 1;
        playSongAtPosition(songPos);
    }

    private void playSongAtPosition(int position) {
        if (cursor.moveToPosition(position)) {
            //读取歌曲并播放
            String songName = cursor.getString(cursor.getColumnIndex("songname"));
            String songSrc = cursor.getString(cursor.getColumnIndex("songsrc"));
            String songDesc = cursor.getString(cursor.getColumnIndex("songdesc"));
            songname.setText(songName);
            songdesc.setText(songDesc);
            mediaPlayer.reset();
            try {
                Log.d("MusicPlayerActivity", "Trying to play: " + songSrc);
                if (new File(songSrc).exists()) {
                    mediaPlayer.setDataSource(songSrc);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    setAlbumArt(songSrc);  // 更新专辑封面
                    if (mediaPlayer.isPlaying()) {
                        play_btn.setImageResource(R.drawable.ic_outline_pause_56);
                    } else {
                        play_btn.setImageResource(R.drawable.ic_outline_play_arrow_56);
                    }
                } else {
                    Toast.makeText(this, "File does not exist: " + songSrc, Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //设定专辑封面
    private void setAlbumArt(String songSrc) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(songSrc);
            byte[] artBytes = mmr.getEmbeddedPicture();
            if (artBytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                albumArt.setImageBitmap(bitmap);
            } else {
                albumArt.setImageResource(R.drawable.background_gradient); // 设置默认封面图像
            }
        } catch (Exception e) {
            e.printStackTrace();
            albumArt.setImageResource(R.drawable.background_gradient); // 设置默认封面图像
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.reset();
    }
}
