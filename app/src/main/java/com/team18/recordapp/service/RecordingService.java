package com.team18.recordapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;

import com.team18.recordapp.AudioRecorder;
import com.team18.recordapp.MainActivity;
import com.team18.recordapp.R;
import com.team18.recordapp.broadcast.StopRecordingService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordingService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "RecordingServiceChannel";
    private AudioRecorder audioRecorder;

    @Override
    public void onCreate() {
        super.onCreate();
        audioRecorder = new AudioRecorder("", this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int time = intent.getIntExtra("time", -1);
        // Start recording
        if (time > 0) {
            startForeground(NOTIFICATION_ID, createNotification());
            audioRecorder.record(time);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopForeground(Service.STOP_FOREGROUND_REMOVE);
                    stopSelf();
                }
            }, time*60*1000);
            initSettingFile();
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();


        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create notification channel if API level is appropriate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        // Create intent for the stop action button
        Intent stopIntent = new Intent(this, StopRecordingService.class);
        stopIntent.setAction("stop_recording");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Add the stop action button to the notification
        builder.addAction(R.drawable.stop, "Dừng ghi âm", stopPendingIntent);

        // Build the notification
        return builder.setContentTitle("Recording Service")
                .setContentText("Recording in progress")
                .setSmallIcon(R.drawable.mic)
                .setContentIntent(pendingIntent)
                .build();
    }


    private void initSettingFile() {
        try {
            File currentDir = getBaseContext().getFilesDir();
            File settingFile = new File(currentDir, "setting.txt");
            FileOutputStream fos = new FileOutputStream(settingFile);
            String settingContent = "false,00:00,0";
            fos.write(settingContent.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
