package com.team18.recordapp.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.team18.recordapp.MainActivity;
import com.team18.recordapp.R;
import com.team18.recordapp.broadcast.StartRecordReceiver;
import com.team18.recordapp.service.RecordingService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private AlarmManager alarmManager;

    private TimePicker timePicker;
    private TextView tvSelectedTime;
    private Button btnConfirm;
    private boolean isSetTime;
    private Intent intent;
    private String selectedTime = "00:00";
    private Button btnCancel;
    private EditText edtTimeRecord;
    private String recordTime = "1";
    private File settingFile;
    private  PendingIntent pendingIntent;

    public ScheduleFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ScheduleFragment newInstance() {
        ScheduleFragment fragment = new ScheduleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }



    private void setControl(View view){
        File currentDir = requireActivity().getFilesDir();
        settingFile = new File(currentDir, "setting.txt");
        timePicker = view.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        tvSelectedTime = view.findViewById(R.id.tv_selected_time);
        btnCancel = view.findViewById(R.id.btn_cancel);
        edtTimeRecord = view.findViewById(R.id.edt_time_record);
        edtTimeRecord.setText(selectedTime);
        intent = new Intent(requireActivity(), MainActivity.class);
        readSettingFile();
    }

    private void setEvent() {

        btnCancel.setOnClickListener(v -> {
            isSetTime=false;
            edtTimeRecord.setText("");
            tvSelectedTime.setText("Thời gian đã chọn:");
            intent.putExtra("is_set_time", false);
            writeSettingFile();
            if (alarmManager != null && pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtTimeRecord.getText().toString().isEmpty()){
                    edtTimeRecord.setError("Thời gian ghi không được để trống");
                }
                else if (Integer.parseInt(edtTimeRecord.getText().toString()) == 0) {
                    edtTimeRecord.setError("Thời gian ghi phải > 0");
                }
                else {
                    isSetTime = true;
                    recordTime = edtTimeRecord.getText().toString().trim();
                    selectedTime = String.format(Locale.getDefault(),
                            "%02d:%02d",
                            timePicker.getHour(), timePicker.getMinute());

                    String txtSelectedTime = "Thời gian đã chọn: " + selectedTime;
                    tvSelectedTime.setText(txtSelectedTime);
                    writeSettingFile();
                    isSetTime = true;
                    intent.putExtra("is_set_time", true);
                    if (isSetTime) {
                        int hour = timePicker.getHour();
                        int minute = timePicker.getMinute();
                        Intent intent = new Intent(requireActivity(), StartRecordReceiver.class);
                        intent.setAction("schedule_record");
                        intent.putExtra("hour", hour);
                        intent.putExtra("minute", minute);
                        intent.putExtra("time", Integer.parseInt(edtTimeRecord.getText().toString()));
                        pendingIntent = PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, minute);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);

                        alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);

                    }
                }

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        // Inflate the layout for this fragment
        setControl(view);
        setEvent();
        return view;
    }

    private void initSettingFile() {
        try {
            FileOutputStream fos = new FileOutputStream(settingFile);
            String settingContent = "false,00:00,1";
            fos.write(settingContent.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeSettingFile() {
        if (settingFile.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(settingFile);
                recordTime = (recordTime.isEmpty())? "1" : recordTime;
                String settingContent = isSetTime + "," + selectedTime + "," + recordTime;
                fos.write(settingContent.getBytes());
                fos.close();
            } catch (IOException e) {
                Log.d("IO file setting", "Lỗi khi ghi file setting");
            }
        } else {
            initSettingFile();
        }
    }

    private void readSettingFile() {
        if (settingFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(settingFile));
                String line = br.readLine();
                if (line != null) {
                    String[] settingContent = line.split(",");
                    isSetTime = Boolean.parseBoolean(settingContent[0].trim());
                    selectedTime = settingContent[1].trim();
                    recordTime = settingContent[2].trim();
                    recordTime = (recordTime.equals("0"))? "" : recordTime;
                    edtTimeRecord.setText(recordTime);

                    String txtSelectedTime = "Thời gian đã chọn: " + selectedTime;
                    tvSelectedTime.setText(txtSelectedTime);
                }
                br.close();
            } catch (IOException e) {
                Log.d("IO file setting", "Lỗi khi ọc file setting");
            }
        } else {
            initSettingFile();
        }
    }
}