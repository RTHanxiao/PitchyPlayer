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
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MusicPlayerActivity extends AppCompatActivity {
    TextView songname, songdesc;
    ImageButton prev_btn, play_btn, next_btn;
    ImageView albumArt;
    SeekBar seekBar;
    MediaPlayer mediaPlayer = new MediaPlayer();
    int songPos;
    int songID;
    int songCount;
    SQLiteDatabase db;
    Handler handler = new Handler();

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        SongListDatabaseHelper dbHelper = new SongListDatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        songname = findViewById(R.id.songname);
        songdesc = findViewById(R.id.songdesc);
        albumArt = findViewById(R.id.imageView3);
        play_btn = findViewById(R.id.imageButton2);
        next_btn = findViewById(R.id.imageButton4);
        prev_btn = findViewById(R.id.imageButton3);
        seekBar = findViewById(R.id.seekBar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Intent intent = getIntent();
        songID = Integer.parseInt(intent.getStringExtra("songID"));

        String songName = intent.getStringExtra("songName");
        String songDesc = intent.getStringExtra("songDesc");
        String songSrc = intent.getStringExtra("songSrc");

        String sql = "SELECT * FROM songlist;";
        Cursor cursor = db.rawQuery(sql, null);

        Cursor mCount = db.rawQuery("SELECT count(*) FROM songlist", null);
        mCount.moveToFirst();
        songCount = mCount.getInt(0);
        mCount.close();

        songname.setText(songName);
        songdesc.setText(songDesc);

        // 设置专辑封面
        setAlbumArt(songSrc);

        try {
            mediaPlayer.setDataSource(songSrc);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNextSong(cursor);
            }
        });

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
                playNextSong(cursor);
            }
        });

        prev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevSong(cursor);
            }
        });

        // 更新SeekBar的最大值和当前进度
        seekBar.setMax(mediaPlayer.getDuration());
        handler.post(updateSeekBar);

        // 处理用户拖动SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void playNextSong(Cursor cursor) {
        songID++;
        songPos = songID - 1;
        if (songPos == songCount) {
            songPos = 0;
            songID = 1;
        }
        playSong(cursor, songPos);
    }

    private void playPrevSong(Cursor cursor) {
        songID--;
        songPos = songID - 1;
        if (songPos == -1) {
            songPos = songCount - 1;
            songID = songCount;
        }
        playSong(cursor, songPos);
    }

    private void playSong(Cursor cursor, int position) {
        cursor.moveToPosition(position);
        String songName = cursor.getString(cursor.getColumnIndex("songname"));
        String songSrc = cursor.getString(cursor.getColumnIndex("songsrc"));
        String songDesc = cursor.getString(cursor.getColumnIndex("songdesc"));
        songname.setText(songName);
        songdesc.setText(songDesc);
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(songSrc);
            mediaPlayer.prepare();
            mediaPlayer.start();
            setAlbumArt(songSrc);  // 更新专辑封面
            if (mediaPlayer.isPlaying()) {
                play_btn.setImageResource(R.drawable.ic_outline_pause_56);
            } else {
                play_btn.setImageResource(R.drawable.ic_outline_play_arrow_56);
            }
            seekBar.setMax(mediaPlayer.getDuration());  // 更新SeekBar的最大值
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setAlbumArt(String songSrc) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(songSrc);
        byte[] artBytes = mmr.getEmbeddedPicture();
        if (artBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
            albumArt.setImageBitmap(bitmap);
        } else {
            albumArt.setImageResource(R.drawable.background_gradient); // 设置默认封面图像
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.reset();
        handler.removeCallbacks(updateSeekBar);
    }

    // 更新SeekBar的进度
    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        }
    };
}
