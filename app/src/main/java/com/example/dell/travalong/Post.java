package com.example.dell.travalong;

public class Post {

    private String date,description,full_name,postimage,profileimage,time,uid;

    public Post() {

    }

    public Post(String date, String description ,String full_name ,String postimage , String profileimage,String time, String uid)
    {
        this.date = date;
        this.description=description;
        this.full_name =full_name;
        this.postimage=postimage;
        this.profileimage=profileimage;
        this.time=time;
        this.uid=uid;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getPostImage() {
        return postimage;
    }

    public String getProfileImage() {
        return profileimage;
    }

    public String getTime() {
        return time;
    }

    public String getUid() {
        return uid;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setPostImage(String postimage) {
        this.postimage = postimage;
    }

    public void setProfileImage(String profileimage) {
        this.profileimage = profileimage;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
