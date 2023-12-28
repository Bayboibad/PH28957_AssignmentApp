package com.example.assignmentapp.model;

public class NotificationModel {
    private int _id;
    private String title;
    private String content;
    private String date;
    private String user;

    public NotificationModel(int _id,String title, String content,String date,String user) {
        this._id = _id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.user = user;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
