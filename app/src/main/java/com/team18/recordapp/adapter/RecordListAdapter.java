package com.team18.recordapp.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.team18.recordapp.R;
import com.team18.recordapp.Record;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecordListAdapter extends ArrayAdapter<Record> {
    ArrayList<Record> records;
    Context context;
    int layoutResource;
    public RecordListAdapter(@NonNull Context context, int resource, ArrayList<Record> records) {
        super(context, resource, records);
        this.context = context;
        this.records = records;
        this.layoutResource = resource;
    }

    @Nullable
    @Override
    public Record getItem(int position) {
        return records.get(position);
    }

    @Override
    public int getCount() {
        return records.size();
    }




    public boolean isCheck() {
        for (Record record : records) {
            if (record.isCheck()) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        convertView = inflater.inflate(layoutResource,null);
        //Hàm khởi thêm dữ lieu vào các View từ arrayList thông qua position
        TextView tvRecordName = convertView.findViewById(R.id.tv_record_name);
        ImageButton btnEdit = convertView.findViewById(R.id.btn_rename_audio_file);
        CheckBox cbRecord = convertView.findViewById(R.id.cb_audio);

        tvRecordName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecordedFile(getItem(position).getRecordPath());
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameFile(position);
            }
        });



        cbRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getItem(position).setCheck(cbRecord.isChecked());
            }
        });


        cbRecord.setChecked(getItem(position).isCheck());
        tvRecordName.setText(getItem(position).getRecordName());

        return convertView;
    }

    private void renameFile(int position) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.rename_file);

        Window window = dialog.getWindow();
        if (window == null) {
            Toast.makeText(context, "Window is null", Toast.LENGTH_SHORT).show();
            return;
        }

        window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;

//        dialog.setCancelable(true);
        EditText edtRename = dialog.findViewById(R.id.edt_rename);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtRename.getText().toString().isEmpty()) {
                    edtRename.setError("Bạn chưa nhập tên file");
                }
                else {
                    File file = new File(getItem(position).getRecordPath());
                    File newFile = new File(file.getParent(), edtRename.getText().toString() + ".mp3");
                    if (file.renameTo(newFile)) {
                        getItem(position).setRecordName(edtRename.getText().toString());
                        notifyDataSetChanged();
                        dialog.dismiss();
                        Toast.makeText(context, "Đổi tên file thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Đổi tên file thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }


    private void playRecordedFile(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            MediaPlayer mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                // Xử lý ngoại lệ khi không thể phát nhạc
            }
        } else {
            // Xử lý khi file không tồn tại
        }
    }

    private void deleteCheckedItem() {
        for (int i = records.size() - 1; i >= 0; i--) {
            if (records.get(i).isCheck()) {
                deleteRecordedFile(i);
            }
        }
    }

    public void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Bạn có muốn xoá file này không?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Delete" button, delete the recorded file
                        deleteCheckedItem();
                    }
                })
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Cancel" button, dismiss the dialog
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteRecordedFile(int position) {
        String filePath = records.get(position).getRecordPath();
        File file = new File(filePath);
        if (file.exists() && file.delete()) {
            // Remove the file path from the list
            if (!records.isEmpty()) {
                records.remove(position);
            }
            notifyDataSetChanged();
        }
    }
}
