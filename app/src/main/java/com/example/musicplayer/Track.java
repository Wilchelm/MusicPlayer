package com.example.musicplayer;

import android.graphics.Bitmap;
import android.net.Uri;

public class Track {
    private String title, artist, duration;
    private boolean isPlaying;
    private Bitmap image;
    private Uri musicFile;

    public Track(String title, String artist, String duration, boolean isPlaying, Bitmap image, Uri musicFile) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.isPlaying = isPlaying;
        this.image = image;
        this.musicFile = musicFile;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration() { return duration; }

    public boolean isPlaying() {
        return isPlaying;
    }

    public Uri getMusicFile() {
        return musicFile;
    }

    public Bitmap getImage() { return image; }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
