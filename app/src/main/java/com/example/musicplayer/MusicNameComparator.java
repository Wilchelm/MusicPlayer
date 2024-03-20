package com.example.musicplayer;

import java.util.Comparator;

public class MusicNameComparator implements Comparator<Track> {
    public int compare(Track track1, Track track2) {
        return track1.getTitle().compareTo(track2.getTitle());
    }
}
