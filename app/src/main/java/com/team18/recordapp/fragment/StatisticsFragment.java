package com.team18.recordapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.team18.recordapp.R;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatisticsFragment extends Fragment {

    private BarChart barChart;
    public StatisticsFragment() {
        // Required empty public constructor
    }


    public static StatisticsFragment newInstance() {
        StatisticsFragment fragment = new StatisticsFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic, container, false);
        barChart = view.findViewById(R.id.barChart);
        if (barChart != null) {
            displayBarChart();
        }
        else {
            Log.e("ChartActivity", "BarChart is null");
        }


        return view;
    }

    private void displayBarChart() {
        List<BarEntry> entries = getRecordingsPerDayEntries();
        BarDataSet dataSet = new BarDataSet(entries, "Recordings");
        BarData data = new BarData(dataSet);

        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);

        barChart.getAxisLeft().setGranularity(1f); // Đặt độ chia là 1 (hiển thị số nguyên)
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Chuyển đổi giá trị thành số nguyên
            }
        });

        List<String> xAxisLabels = getXAxisLabels(entries);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setLabelCount(xAxisLabels.size(), true); // Đặt số lượng nhãn bằng số lượng ngày

        barChart.animateY(1000);
        barChart.invalidate();
    }

    private List<BarEntry> getRecordingsPerDayEntries() {
        File path = new File(requireContext().getFilesDir(), "recordings");
        ArrayList<File> fileList = findFile(path);

        TreeMap<String, Integer> recordingsPerDay = new TreeMap<>();

        for (File file : fileList) {
            String dateString = getDateFromFile(file);
//            String formattedDate = formatDate(dateString);
            String formattedDate = dateString;

            if (recordingsPerDay.containsKey(formattedDate)) {
                int count = recordingsPerDay.get(formattedDate);
                recordingsPerDay.put(formattedDate, count + 1);
            } else {
                recordingsPerDay.put(formattedDate, 1);
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : recordingsPerDay.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            index++;
        }

        return entries;
    }

    private ArrayList<File> findFile(File directory) {
        ArrayList<File> arrayList = new ArrayList<>();
        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File singleFile : files) {
                    if (singleFile.getName().toLowerCase().endsWith(".mp3")) {
                        arrayList.add(singleFile);
                    }
                }
            } else {
                Log.e("ChartActivity", "Unable to list files in directory: " + directory.getAbsolutePath());
            }
        } else {
            Log.e("ChartActivity", "Provided file is not a directory or is null");
        }
        return arrayList;
    }

    private String getDateFromFile(File file) {
        String fileName = file.getName();
        return fileName.split("~")[1]; // Lấy chuỗi ngày tháng từ vị trí 10 đến 17 của tên tập tin
    }

//    private String formatDate(String dateString) {
//        String year = dateString.substring(0, 4);
//        String month = dateString.substring(4, 6);
//        String day = dateString.substring(6, 8);
//        return year + "-" + month + "-" + day;
//    }


    private List<String> getXAxisLabels(List<BarEntry> entries) {
        List<String> labels = new ArrayList<>();

        // Lấy danh sách các ngày có bản ghi âm từ TreeMap
        TreeMap<String, Integer> recordingsPerDay = getRecordingsPerDayMap();

        // Thêm tất cả các ngày vào danh sách nhãn
        for (Map.Entry<String, Integer> entry : recordingsPerDay.entrySet()) {
            labels.add(entry.getKey());
        }

        return labels;
    }

    private TreeMap<String, Integer> getRecordingsPerDayMap() {
        File path = new File(requireContext().getFilesDir(), "recordings");
        ArrayList<File> fileList = findFile(path);

        TreeMap<String, Integer> recordingsPerDay = new TreeMap<>();

        // Tính toán số lượng bản ghi âm cho từng ngày
        for (File file : fileList) {
            String dateString = getDateFromFile(file);
//            String formattedDate = formatDate(dateString);
            String formattedDate = dateString;


            if (recordingsPerDay.containsKey(formattedDate)) {
                int count = recordingsPerDay.get(formattedDate);
                recordingsPerDay.put(formattedDate, count + 1);
            } else {
                recordingsPerDay.put(formattedDate, 1);
            }
        }

        return recordingsPerDay;
    }
}