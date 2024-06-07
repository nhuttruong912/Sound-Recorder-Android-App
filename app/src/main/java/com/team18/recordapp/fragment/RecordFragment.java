package com.team18.recordapp.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.team18.recordapp.AudioRecorder;
import com.team18.recordapp.R;
import com.team18.recordapp.broadcast.StartRecordReceiver;
import com.team18.recordapp.util.Clock;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class RecordFragment extends Fragment {
    private static final int REQUEST_CODE_RECORD_AUDIO = 1;
    private AudioRecorder audioRecorder;
    private String outputFile;
    private int recordingCounter = 1;
    private TextView timeTextView;
    private long startTime = 0;
    private Timer timer;
    private TimerTask timerTask;
    private ImageButton stopButton, startButton;
    private TextView tvStopRecord;

    // Bổ sung phương thức để truy cập vào file ghi âm
    private void accessRecordedFile() {
        String filePath = outputFile; // outputFile là biến chứa đường dẫn của file ghi âm
        File recordedFile = new File(filePath);

        // Kiểm tra xem file đã tồn tại hay không
        if (recordedFile.exists()) {
            // Xử lý với file ghi âm ở đây
            // Ví dụ: Hiển thị đường dẫn hoặc thực hiện các hoạt động khác với file
            String absolutePath = recordedFile.getAbsolutePath();
            Log.d("RecordedFile", "Đường dẫn của file ghi âm: " + absolutePath);
        } else {
            Log.e("RecordedFile", "File ghi âm không tồn tại");
        }
    }

    public static RecordFragment newInstance() {
        RecordFragment fragment = new RecordFragment();
        return fragment;
    }

    public String getRecordedPath() {
        return getActivity().getExternalCacheDir().getAbsolutePath();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        startButton = view.findViewById(R.id.btn_record);
        stopButton = view.findViewById(R.id.stopbutton);
        tvStopRecord = view.findViewById(R.id.tv_stop_record);
        tvStopRecord.setVisibility(View.INVISIBLE);
        stopButton.setVisibility(View.INVISIBLE);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
        String formattedDate = sdf.format(calendar.getTime());
        outputFile = getActivity().getExternalCacheDir().getAbsolutePath() + "/ghiam" + recordingCounter + "." + formattedDate + ".mp3";

        // Tạo tên file ghi âm với số thứ tự tăng dần

        // Create an instance of AudioRecorder with the output file path and context
        audioRecorder = new AudioRecorder(outputFile, getContext());

        // Kiểm tra quyền truy cập microphone
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu quyền truy cập
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_RECORD_AUDIO);
        }

        timeTextView = view.findViewById(R.id.text_time);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bắt đầu ghi âm
                startRecord();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });




        return view;
    }


    private void startRecord() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

            audioRecorder.start();
            Clock.start();
            startRecording();

        } else {
            Toast.makeText(getActivity(), "Vui lòng cấp quyền truy cập microphone", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRecording() {
        stopButton.setVisibility(View.VISIBLE);
        tvStopRecord.setVisibility(View.VISIBLE);

        Toast.makeText(getActivity(), "Bắt đầu ghi âm", Toast.LENGTH_SHORT).show();

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeTextView.setText(Clock.getCurrentTime());
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    private void stopRecording() {
        Toast.makeText(getActivity(), "Dừng ghi âm", Toast.LENGTH_SHORT).show();

        // Dừng đếm thời gian
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        timeTextView.setVisibility(View.VISIBLE); // Hiển thị lại TextView để hiển thị thời gian mới

        startTime = 0; // Reset startTime
        timeTextView.setText("00:00:00"); // Đặt lại thời gian đếm thành 00:00:00
    }

    public void recordFllowMinute(int minute) {
        Handler handler = new Handler();
        startRecord();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                stopRecord();
            }
        };

        handler.postDelayed(runnable, minute * 60 * 1000);
    }

    private void stopRecord() {
        Clock.stop();
        stopButton.setVisibility(View.INVISIBLE);
        tvStopRecord.setVisibility(View.INVISIBLE);
        // Dừng ghi âm
        audioRecorder.stop();
        stopRecording();

        // Tăng số thứ tự file ghi âm để tạo file mới cho lần ghi âm tiếp theo
        recordingCounter++;
        // Tạo lại tên file ghi âm cho lần ghi âm tiếp theo
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
        String formattedDate = sdf.format(calendar.getTime());
        outputFile = getActivity().getExternalCacheDir().getAbsolutePath() + "/ghiam" + recordingCounter+"." + formattedDate + ".mp3";
        // Tạo một đối tượng AudioRecorder mới với tên file mới
        audioRecorder = new AudioRecorder(outputFile, getContext());
        accessRecordedFile();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền truy cập được cấp
                Toast.makeText(getActivity(), "Quyền truy cập microphone được cấp", Toast.LENGTH_SHORT).show();
            } else {
                // Quyền truy cập bị từ chối
                Toast.makeText(getActivity(), "Quyền truy cập microphone bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }
}