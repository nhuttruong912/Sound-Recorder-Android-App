package com.team18.recordapp;

import java.io.File;

public class Record {

    public Record(String recordPath) {
        this.recordPath = recordPath;
        setRecordNameByPath();
    }

    private String recordPath;

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public String getRecordName() {
        return recordName;
    }

    private void setRecordNameByPath() {
        try {
            recordName = new File(recordPath).getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String recordName;

    public String getRecordPath() {
        return recordPath;
    }

    public void setRecordPath(String recordPath) {
        this.recordPath = recordPath;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    private boolean isCheck;


}
