package com.team18.recordapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class AudioAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<Audio> arraylist;

    private class ViewHolder {
        protected CheckBox checkBox;
    }

    public AudioAdapter(Context context, int layout, List<Audio> arraylist) {
        this.context = context;
        this.layout = layout;
        this.arraylist = arraylist;
    }

    @Override
    public int getCount() {
        return arraylist.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(layout, null);

        Audio audio = arraylist.get(position);

        TextView txt = (TextView) convertView.findViewById(R.id.audioItemTxt);
        TextView path = (TextView) convertView.findViewById(R.id.audioPathTxt);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.audioItemCheckbox);

        if(audio.isCheck()) {
            checkBox.setChecked(true);
        }

        if(audio.getName() != null) {
            txt.setText(audio.getName());
            path.setText(audio.getPath());
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audio.setCheck(checkBox.isChecked());
                checkBox.setChecked(checkBox.isChecked());
            }
        });

        return convertView;
    }
}
