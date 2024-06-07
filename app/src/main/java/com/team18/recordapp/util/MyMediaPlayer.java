package com.team18.recordapp.util;

import android.media.MediaPlayer;

public class MyMediaPlayer {
    private static MediaPlayer instance;

    public static MediaPlayer getInstanse() {
        if (instance == null) {
            instance = new MediaPlayer();
        }
        return instance;
    }

    public static int currentIndex = -1;
}
