package com.team18.recordapp;//package com.team18.recordapp;
//
//import android.content.Context;
//import android.media.MediaRecorder;
//import android.widget.Toast;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//
//public class AudioRecorder {
//    private final Context context;
//    private MediaRecorder mediaRecorder;
//    private String outputFile;
//    private boolean isRecording = false;
//    private ArrayList<byte[]> capturedAudioData; // Store captured audio data in a temporary buffer
//
//    public AudioRecorder(String outputFile, Context context) {
//        this.outputFile = outputFile;
//        this.context = context;  // Store the context for later use
//    }
//
//    public void start() {
//        if (!isRecording) {
//            try {
//                if (mediaRecorder == null) {
//                    mediaRecorder = new MediaRecorder();
//                    configureMediaRecorder(); // Call a separate method for configuration
//                }
//
//                // Initialize capturedAudioData for storing audio chunks
//                capturedAudioData = new ArrayList<>();
//
//                mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
//                    @Override
//                    public void onError(MediaRecorder mr, int what, int extra) {
//                        // Handle specific recording errors (e.g., handle different error codes)
//                        stop();
//                        Toast.makeText(context, "Recording error: " + what, Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                mediaRecorder.prepare();
//                mediaRecorder.start();
//                isRecording = true;
//            } catch (IOException e) {
//                e.printStackTrace();
//                Toast.makeText(context, "Error starting recording", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    public void stop() {
//        if (isRecording) {
//            isRecording = false;
//            mediaRecorder.stop();
//            mediaRecorder.release();
//            mediaRecorder = null;
//        }
//    }
//
//    public void save() {
//        if (capturedAudioData != null && !capturedAudioData.isEmpty()) {
//            try {
//                // Create the output file
//                File file = new File(outputFile);
//                FileOutputStream fos = new FileOutputStream(file);
//
//                // Write the captured audio data to the file
//                for (byte[] dataChunk : capturedAudioData) {
//                    fos.write(dataChunk);
//                }
//
//                fos.close();
//
//                Toast.makeText(context, "Audio recorded and saved successfully", Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                e.printStackTrace();
//                Toast.makeText(context, "Error saving audio file", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(context, "No audio data captured", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // Separate method for media recorder configuration with better audio quality
//    private void configureMediaRecorder() {
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        File internalStorageDir = context.getFilesDir();
//        String subdirectory = "recordings"; // Example subdirectory name
//        File recordingsDir = new File(internalStorageDir, subdirectory);
//        recordingsDir.mkdirs(); // Creates the directory if it doesn't exist
//        long currentTimeMillis = System.currentTimeMillis();
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        Date currentDate = new Date(currentTimeMillis);
//
//        String formattedDateTime = sdf.format(currentDate);
//        String filename = "recording_" + formattedDateTime + ".mp3";
//        String path = new File(recordingsDir, filename).getAbsolutePath();
//        mediaRecorder.setOutputFile(path);
//    }
//}
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.widget.Toast;

import com.team18.recordapp.util.Clock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AudioRecorder {
    private final Context context;
    private MediaRecorder mediaRecorder;
    private String outputFile;
    private boolean isRecording = false;
    private ArrayList<byte[]> capturedAudioData; // Store captured audio data in a temporary buffer

    public AudioRecorder(String outputFile, Context context) {
        this.outputFile = outputFile;
        this.context = context;  // Store the context for later use
    }

    public void start() {
        if (!isRecording) {
            try {
                if (mediaRecorder == null) {
                    mediaRecorder = new MediaRecorder();
                    configureMediaRecorder(); // Call a separate method for configuration
                }
                Clock.start();
                // Initialize capturedAudioData for storing audio chunks
                capturedAudioData = new ArrayList<>();

                mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                    @Override
                    public void onError(MediaRecorder mr, int what, int extra) {
                        // Handle specific recording errors (e.g., handle different error codes)
                        stop();
                        Toast.makeText(context, "Recording error: " + what, Toast.LENGTH_SHORT).show();
                    }
                });

                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Error starting recording", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void stop() {
        if (isRecording) {
            Clock.stop();
            isRecording = false;
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    public void save() {
        if (capturedAudioData != null && !capturedAudioData.isEmpty()) {
            try {
                // Create the output file
                File file = new File(outputFile);
                FileOutputStream fos = new FileOutputStream(file);

                // Write the captured audio data to the file
                for (byte[] dataChunk : capturedAudioData) {
                    fos.write(dataChunk);
                }

                fos.close();

                Toast.makeText(context, "Audio recorded and saved successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Error saving audio file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "No audio data captured", Toast.LENGTH_SHORT).show();
        }
    }

    // Separate method for media recorder configuration with better audio quality
    private void configureMediaRecorder() {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        File internalStorageDir = context.getExternalFilesDir("recordings");

        long currentTimeMillis = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("'~'yyyy-MM-dd'~'");

        Date currentDate = new Date(currentTimeMillis);

        String formattedDateTime = sdf.format(currentDate);
        String filename =  "record_" + currentTimeMillis + formattedDateTime + ".mp3";
        String path = new File(internalStorageDir, filename).getAbsolutePath();
        mediaRecorder.setOutputFile(path);
    }

    // Record audio for a specified duration in minutes
    public void record(final int durationMinutes) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stop();
                timer.cancel();
            }
        }, durationMinutes * 60 * 1000); // Convert minutes to milliseconds

        start();
    }
}
