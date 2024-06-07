package com.team18.recordapp.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.team18.recordapp.service.RecordingService;
import com.team18.recordapp.util.Clock;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class StartRecordReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() == "schedule_record") {
            Toast.makeText(context, "fooString", Toast.LENGTH_LONG).show();
            Intent intent1 = new Intent(context, RecordingService.class);
            int time = intent.getIntExtra("time", -1);
            intent1.putExtra("time", time);
            context.startService(intent1);
            Clock.start();


            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    context.stopService(intent1);
                    Clock.stop();
                }
            }, (long) time * 1000 * 60);
        }
    }
}
