package com.example.socialmediaproject2;

public class FindFriends {

    public String profileImage , fullName , status ;
    public FindFriends() {
    }

    public FindFriends(String profileImage, String fullName, String status) {
        this.profileImage = profileImage;
        this.fullName = fullName;
        this.status = status;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
