package com.example.assignmentapp.model;

import java.util.Date;

public class CommentModel {

    private String id;
    private String user;
    private String imageUser;
    private String comic;
    private String content;
    private String commentDate;

    public CommentModel(String id, String user,String imageUser ,String comic, String content, String commentDate) {
        this.id = id;
        this.user = user;
        this.imageUser = imageUser;
        this.comic = comic;
        this.content = content;
        this.commentDate = commentDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getImageUser() {
        return imageUser;
    }

    public void setImageUser(String imageUser) {
        this.imageUser = imageUser;
    }

    public String getComic() {
        return comic;
    }

    public void setComic(String comic) {
        this.comic = comic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
    }

}
