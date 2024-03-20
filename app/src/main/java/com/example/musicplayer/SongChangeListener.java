package com.example.musicplayer;

public interface SongChangeListener {

    void onChange(int position);
    void onTrackPrevious();
    void onTrackPlay();
    void onTrackPause();
    void onTrackNext();
}
