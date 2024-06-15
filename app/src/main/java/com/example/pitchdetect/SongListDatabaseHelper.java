package com.example.pitchdetect;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SongListDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "music_list.db";
    private static final int DATABASE_VERSION = 1;

    public SongListDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建数据库表
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS songlist (" +
                "songid INTEGER PRIMARY KEY AUTOINCREMENT," +
                "songname VARCHAR(100)," +
                "songdesc VARCHAR(50)," +
                "songtime VARCHAR(10)," +
                "songsrc VARCHAR(200));";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 更新数据库表
        db.execSQL("DROP TABLE IF EXISTS songlist;");
        onCreate(db);
    }
}
