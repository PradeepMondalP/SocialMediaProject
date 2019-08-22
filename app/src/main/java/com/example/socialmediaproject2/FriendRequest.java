package com.example.socialmediaproject2;

public class FriendRequest {

    String profileImage , fullName;

    public FriendRequest() {
    }

    public FriendRequest(String profileImage, String fullName) {
        this.profileImage = profileImage;
        this.fullName = fullName;
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


}
