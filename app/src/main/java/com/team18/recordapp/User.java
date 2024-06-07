package com.team18.recordapp;

public class User {
    private String id;
    private String email;
    private String password;
    private String phone;
    private String passShare;

    public User(String id, String email, String password, String phone, String passShare) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.passShare = passShare;
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassShare() {
        return passShare;
    }

    public void setPassShare(String passShare) {
        this.passShare = passShare;
    }
}
