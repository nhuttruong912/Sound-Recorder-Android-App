package com.team18.recordapp.listener;

import android.media.MediaPlayer;

public interface IClickItemAudioListener {
    void onClickNameAudio(String audioPath, int position);
    void onClickButtonRenameAudio(int position);
    void onClickCheckBoxAudio(boolean isChecked);
    void onClickButtonShareAudio(int position);
}
