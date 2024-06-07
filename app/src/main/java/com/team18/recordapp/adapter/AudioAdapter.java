package com.team18.recordapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.team18.recordapp.Audio;

import com.team18.recordapp.R;
import com.team18.recordapp.listener.IClickItemAudioListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder>{

    private IClickItemAudioListener iClickItemAudioListener;
    private List<Audio> audioList;
    public boolean isShowCheckBox = false;

    public AudioAdapter(IClickItemAudioListener iClickItemAudioListener) {
        this.iClickItemAudioListener = iClickItemAudioListener;
    }

    public void setData(List<Audio> audioList) {
        this.audioList = audioList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
        return new AudioViewHolder(view);
    }

    private boolean isAudioSelected() {
        for (Audio audio : audioList) {
            if (audio.isCheck()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        Audio audio = audioList.get(holder.getAdapterPosition());
        if (audio != null) {
            isShowCheckBox = isAudioSelected();
            holder.tvFileName.setText(audio.getName().split("~")[0]);
//            holder.tvFileName.setText(audio.getName());
            holder.cbAudio.setChecked(audio.isCheck());
            holder.tvFileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playRecordedFile(audio.getPath());
                    iClickItemAudioListener.onClickNameAudio(audio.getPath(), holder.getAdapterPosition());
                }
            });

            holder.tvFileName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    isShowCheckBox = true;
                    audio.setCheck(true);
                    iClickItemAudioListener.onClickCheckBoxAudio(isAudioSelected());
                    return true;
                }
            });


            holder.btnEditFileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iClickItemAudioListener.onClickButtonRenameAudio(holder.getAdapterPosition());
                }
            });

            holder.cbAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    audio.setCheck(holder.cbAudio.isChecked());
                    iClickItemAudioListener.onClickCheckBoxAudio(isAudioSelected());
                }
            });


            holder.btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iClickItemAudioListener.onClickButtonShareAudio(holder.getAdapterPosition());
                }
            });


            holder.cbAudio.setVisibility(isShowCheckBox? View.VISIBLE : View.GONE);
        }
    }

    private void playRecordedFile(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {

            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
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

    @Override
    public int getItemCount() {
        if (audioList != null) {
            return audioList.size();
        }
        return 0;
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder {
        private ImageButton btnEditFileName;
        private CheckBox cbAudio;
        private TextView tvFileName;
        private ImageButton btnShare;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            btnEditFileName = itemView.findViewById(R.id.btn_rename_audio_file);
            cbAudio = itemView.findViewById(R.id.cb_audio);
            tvFileName = itemView.findViewById(R.id.tv_record_name);
            btnShare = itemView.findViewById(R.id.btn_share_audio_file);
        }
    }




}
