package com.team18.recordapp.broadcast;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.team18.recordapp.service.RecordingService;

public class StopRecordingService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("stop_recording".equals(intent.getAction())) {
            // Handle the stop action here
            // Stop your service
            context.stopService(new Intent(context, RecordingService.class));
        }
    }
}