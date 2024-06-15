package com.example.pitchdetect;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        //初始化数据库
        SongListDatabaseHelper dbHelper = new SongListDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        recyclerView = findViewById(R.id.recyclerView);

        //接收信息查询歌曲
        String sql = "SELECT * FROM songlist;";
        List<Song> songList = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            Song song = new Song();
            song.setSongName(cursor.getString(cursor.getColumnIndex("songname")));
            song.setSongDesc(cursor.getString(cursor.getColumnIndex("songdesc")));
            song.setSongTime(cursor.getString(cursor.getColumnIndex("songtime")));
            song.setSongSrc(cursor.getString(cursor.getColumnIndex("songsrc")));
            song.setSongID(cursor.getString(cursor.getColumnIndex("songid")));
            songList.add(song);
        }
        cursor.close();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        MusicListAdapter musicListAdapter = new MusicListAdapter(songList);
        recyclerView.setAdapter(musicListAdapter);
    }
}
