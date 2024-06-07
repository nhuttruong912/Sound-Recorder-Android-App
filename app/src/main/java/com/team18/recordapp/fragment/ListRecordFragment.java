package com.team18.recordapp.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.team18.recordapp.Audio;
import com.team18.recordapp.R;
import com.team18.recordapp.Record;
import com.team18.recordapp.adapter.AudioAdapter;
import com.team18.recordapp.adapter.RecordListAdapter;
import com.team18.recordapp.listener.IClickItemAudioListener;
import com.team18.recordapp.util.MyMediaPlayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ListRecordFragment extends Fragment {
    private RecyclerView rcvListAudio;
    private List<Audio> listAudio;
    private AudioAdapter audioAdapter;
    private ArrayList<Record> records;
    private RecordListAdapter adapter;
    private Button btnDelete;
    private IClickItemAudioListener iClickItemAudioListener;
    private static MediaPlayer mediaPlayer;
    private TextView tvTitleAudio, tvCurrentTime, tvTotalTime;
    private SeekBar sbTime;
    private ImageButton btnPlayPause, btnSkipNext, btnSkipPrevious, btnCloseAudioBox;
    private LinearLayout musicBox;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_record, container, false);

        setListener();
        setControl(view);
        setEvent();

        return view;
    }

    private void setListener() {
        iClickItemAudioListener = new IClickItemAudioListener() {


            @Override
            public void onClickNameAudio(String audioPath, int position) {
                setResourcesWithMusic(position);
//                playAudioRecordFile(audioPath, position);
            }

            @Override
            public void onClickButtonRenameAudio(int position) {
                renameFile(position);
            }

            @Override
            public void onClickCheckBoxAudio(boolean isChecked) {
                int visible = isChecked ? View.VISIBLE : View.GONE;
                audioAdapter.notifyDataSetChanged();
                btnDelete.setVisibility(visible);
                musicBox.setVisibility(View.GONE);
            }

            @Override
            public void onClickButtonShareAudio(int position) {
                sharingAudio(listAudio.get(position).getPath());
            }

        };
    }

    private void playAudioRecordFile(String audioPath, int position) {
        MyMediaPlayer.getInstanse().reset();
        MyMediaPlayer.currentIndex = position;
    }

    private void setEvent() {

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlayPause.setImageResource(R.drawable.icon_play);
                }
                else {
                    mediaPlayer.start();
                    btnPlayPause.setImageResource(R.drawable.icon_pause);
                }
            }
        });

        btnSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });

        btnSkipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousSong();
            }
        });

        btnCloseAudioBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
                musicBox.setVisibility(View.GONE);
                pausePlay();
                mediaPlayer.stop();
            }
        });

        sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress); // Seek to the position set by the user
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setControl(View view) {
        rcvListAudio = view.findViewById(R.id.rcv_list_audio);
        listAudio = new ArrayList<>();
        audioAdapter = new AudioAdapter(iClickItemAudioListener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
        );
        rcvListAudio.setLayoutManager(layoutManager);
        rcvListAudio.setAdapter(audioAdapter);
        loadAudioFiles();
        btnDelete = view.findViewById(R.id.btn_delete_file);

        tvTitleAudio = view.findViewById(R.id.tv_title_audio);
        tvCurrentTime = view.findViewById(R.id.tv_current_time);
        tvTotalTime = view.findViewById(R.id.tv_total_time);
        sbTime = view.findViewById(R.id.sb_time);
        musicBox = view.findViewById(R.id.music_box);
        musicBox.setVisibility(View.GONE);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        btnSkipPrevious = view.findViewById(R.id.btn_skip_previous);
        btnSkipNext = view.findViewById(R.id.btn_skip_next);
        btnCloseAudioBox = view.findViewById(R.id.btn_close_play_audio);
        mediaPlayer = MyMediaPlayer.getInstanse();
    }

    private void setResourcesWithMusic(int position) {
        Audio audio = listAudio.get(position);
        MyMediaPlayer.currentIndex = position;
        tvTitleAudio.setText(listAudio.get(position).getName().split("~")[0]);

        musicBox.setVisibility(View.VISIBLE);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(requireContext(), Uri.parse(audio.getPath()));
            mediaPlayer.prepare();
            tvTotalTime.setText(convertToMMSS(String.valueOf(mediaPlayer.getDuration())));
            btnPlayPause.setImageResource(R.drawable.icon_pause);
            mediaPlayer.start();
            updateSeekBar();
//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    // The MediaPlayer is prepared, you can start playback here
//
//                    mp.start();
//
//
//
//                }
//            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    tvTotalTime.setText("00:00:00");
                    sbTime.setProgress(0);
                    btnPlayPause.setImageResource(R.drawable.icon_play);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately, for example, show an error message
        }

    }
    private Handler mHandler = new Handler();

    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            sbTime.setMax(mediaPlayer.getDuration());
            Runnable mRunnable = new Runnable() {
                @Override
                public void run() {

                    int mCurrentPosition = mediaPlayer.getCurrentPosition();
                    sbTime.setProgress(mCurrentPosition);
                    sbTime.postDelayed(this, 50);
                    tvCurrentTime.setText(convertToMMSS(String.valueOf(mCurrentPosition)));
                }
            };

            requireActivity().runOnUiThread(mRunnable);
            mHandler.postDelayed(mRunnable, 50);
        }
    }


    private void playAudio() {
        if (mediaPlayer != null) {
            btnPlayPause.setImageResource(R.drawable.icon_pause);
            mediaPlayer.start();
        }
    }


    private void playNextSong() {
        if (MyMediaPlayer.currentIndex + 1 > listAudio.size() - 1) {
            MyMediaPlayer.currentIndex = 0;
        } else {
            MyMediaPlayer.currentIndex++;
        }
        setResourcesWithMusic(MyMediaPlayer.currentIndex);
    }

    private void playPreviousSong() {
        if (MyMediaPlayer.currentIndex - 1 < 0) {
            MyMediaPlayer.currentIndex = listAudio.size() - 1;
        } else {
            MyMediaPlayer.currentIndex--;
        }
        setResourcesWithMusic(MyMediaPlayer.currentIndex);
    }

    private void pausePlay() {
        mediaPlayer.stop();
        btnPlayPause.setImageResource(R.drawable.icon_play);
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    public static String convertToMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d:%03d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                millis % 1000
        );
    }

    @NonNull
    public static ListRecordFragment newInstance() {
        ListRecordFragment fragment = new ListRecordFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAudioFiles();
    }

    private void loadAudioFiles() {
        File internalStorageDir = getContext().getExternalFilesDir("recordings");
        // Get directory path where recorded files are stored
        File[] files = internalStorageDir.listFiles();

        if (files != null) {
            listAudio.clear();
            for (File file : files) {
                Audio audio = new Audio(file.getName(), file.getAbsolutePath(), false, 0);
                listAudio.add(audio);
            }
            audioAdapter.setData(listAudio);
        }
    }

    private void sharingAudio(String filePath) {
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(requireContext(),
                "com.team18.recordapp.fileprovider",
                file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("audio/mp3");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        requireContext().startActivity(Intent.createChooser(intent, "Share file:"));
    }

    private void showDeleteConfirmationDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_file);

        Window window = dialog.getWindow();
        if (window == null) {
            Toast.makeText(requireContext(), "Window is null", Toast.LENGTH_SHORT).show();
            return;
        }

        window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;

        Button btnYes = dialog.findViewById(R.id.btn_yes);
        Button btnNo = dialog.findViewById(R.id.btn_no);

        ListView listDeleteFile = dialog.findViewById(R.id.list_delete_file);
        List<String> listFile = new ArrayList<>();
        for (Audio audio : listAudio) {
            if (audio.isCheck()) {
                listFile.add(audio.getName().split("~")[0]);
            }
        }
        ArrayAdapter<List<String>> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
        adapter.addAll(listFile);
        adapter.notifyDataSetChanged();
        listDeleteFile.setAdapter(adapter);

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCheckedItem();
                dialog.dismiss();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void deleteCheckedItem() {
        for (int i = 0; i < listAudio.size();) {
            if (listAudio.get(i).isCheck()) {
                deleteRecordedFile(i);
            }
            else {
                ++i;
            }
        }
        audioAdapter.setData(listAudio);
    }

    public void renameFile(int position) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.rename_file);

        Window window = dialog.getWindow();
        if (window == null) {
            Toast.makeText(requireContext(), "Window is null", Toast.LENGTH_SHORT).show();
            return;
        }

        window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;

        EditText edtRename = dialog.findViewById(R.id.edt_rename);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);
        edtRename.setText(listAudio.get(position).getName().replace(".mp3", "").split("~")[0]);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtRename.getText().toString().isEmpty()) {
                    edtRename.setError("Bạn chưa nhập tên file");
                } else {
                    File file = new File(listAudio.get(position).getPath());
                    String newFileName = edtRename.getText().toString() + "~" + file.getName().split("~")[1] + "~.mp3";
//                    Toast.makeText(requireContext(), newFileName, Toast.LENGTH_SHORT).show();
                    File newFile = new File(file.getParent(), newFileName);
                    if (file.renameTo(newFile)) {
                        listAudio.get(position).setName(newFileName);
                        audioAdapter.setData(listAudio);
                        dialog.dismiss();
                        loadAudioFiles();
                        Toast.makeText(requireContext(), "Đổi tên file thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Đổi tên file thất bại", Toast.LENGTH_SHORT).show();
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

        dialog.show();
    }

    private void deleteRecordedFile(int position) {
        String filePath = listAudio.get(position).getPath();
        File file = new File(filePath);
        if (file.exists() && file.delete()) {
            // Remove the file path from the list
            if (!listAudio.isEmpty()) {
                listAudio.remove(position);
            }
            audioAdapter.setData(listAudio);
        }
    }
}
