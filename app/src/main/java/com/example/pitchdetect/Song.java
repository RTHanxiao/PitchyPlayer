package com.example.pitchdetect;

public class Song {
    private String songID;
    private String songName;
    private String songDesc;
    private String songTime;
    private String songSrc;

    public Song() {
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongID() {
        return songID;
    }

    public void setSongID(String songID) { this.songID = songID; }

    public String getSongDesc() {
        return songDesc;
    }

    public void setSongDesc(String songDesc) {
        this.songDesc = songDesc;
    }

    public String getSongTime() {
        return songTime;
    }

    public void setSongTime(String songTime) {
        this.songTime = songTime;
    }

    public String getSongSrc() { return  songSrc; }

    public void setSongSrc(String songSrc){this.songSrc=songSrc;}
}
