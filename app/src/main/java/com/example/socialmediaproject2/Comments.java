package com.example.socialmediaproject2;

public class Comments {

    String comment ,date , fullName , profileImage , time ;
    public Comments() {
    }

    public Comments(String comment, String date, String fullName, String profileImage, String time) {
        this.comment = comment;
        this.date = date;
        this.fullName = fullName;
        this.profileImage = profileImage;
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
