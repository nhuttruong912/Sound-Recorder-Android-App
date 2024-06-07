package com.team18.recordapp;

public class Audio {
    private String name;
    private  String path;
    boolean  isCheck;

    public boolean isCheck() {
        return isCheck;
    }

    public boolean setCheck(boolean check) {
        isCheck = check;
        return check;
    }

    public Audio(String name, String path, boolean isCheck, int imgId) {
        this.name = name;
        this.path = path;
        this.isCheck = isCheck;
        this.imgId = imgId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private int imgId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }
}
