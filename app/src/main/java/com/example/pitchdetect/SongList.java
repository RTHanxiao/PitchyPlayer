package com.example.pitchdetect;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class SongList {
    private static final String TAG = "SongList";
    private Context context;
    private SQLiteDatabase db;

    public SongList(Context context) {
        this.context = context;
        SongListDatabaseHelper dbHelper = new SongListDatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public void main() {
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        if (musicDir.exists() && musicDir.isDirectory()) {
            listAllFiles(musicDir);
        } else {
            Log.e(TAG, "Music directory does not exist or is not a directory");
        }
    }

    private void listAllFiles(File dir) {
        //读取本地歌曲
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.toLowerCase().endsWith(".mp3") || fileName.toLowerCase().endsWith(".flac") || fileName.toLowerCase().endsWith(".aac")) {
                        String filePath = file.getAbsolutePath();
                        try {
                            mmr.setDataSource(filePath);
                            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            long minutes = TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(duration));
                            long seconds = TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(duration)) - TimeUnit.MINUTES.toSeconds(minutes);
                            String songTime = String.format("%d分%d秒", minutes, seconds);
                            db.execSQL("INSERT INTO songlist (songname, songdesc, songtime, songsrc) VALUES (?, ?, ?, ?)",
                                    new Object[]{title != null ? title : fileName, artist != null ? artist : "Unknown", songTime, filePath});
                            Log.d(TAG, "Added song: " + title + " (" + artist + ")");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (file.isDirectory()) {
                    listAllFiles(file);
                }
            }
        } else {
            Log.e(TAG, "No files found in directory: " + dir.getAbsolutePath());
        }
    }
}
