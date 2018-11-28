package com.example.dell.travalong;

public class FindFollowers {

    private String status,full_name, profileimage;

    public FindFollowers(){

    }

    public FindFollowers(String profileimage, String full_name, String status ){
        this.profileimage = profileimage;
        this.full_name =full_name;
        this.status = status;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getProfileImage() {
        return profileimage;
    }

    public String getStatus() {
        return status;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setProfileImage(String profileimage) {
        this.profileimage = profileimage;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
