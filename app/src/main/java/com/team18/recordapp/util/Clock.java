package com.team18.recordapp.util;

import android.os.Handler;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Clock {
    public static boolean isRunning = false;

    private static Timer timer = new Timer();

    private static SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
    private static Handler handler = new Handler();

    private static String currentTime = "00:00:00";

    public static void start() {
        final Date startDate = new Date();
        isRunning = true;
        timer.schedule(new TimerTask() {
            long startTime = System.currentTimeMillis();
            @Override
            public void run() {
                long time = System.currentTimeMillis() - startTime;
                int seconds = (int) (time / 1000) % 60;
                int minutes = (int) (time / 1000 / 60) % 60;
                int hours = (int) (time / 1000 / 60 / 60);

                currentTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
        }, 0, 1000);
    }

    public static void stop() {
        isRunning = false;
        timer.cancel();
        timer = new Timer();
        currentTime = "00:00:00";
    }


    public static String getCurrentTime() {
        return currentTime;
    }
}
