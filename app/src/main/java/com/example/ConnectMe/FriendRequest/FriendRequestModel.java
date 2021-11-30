package com.example.ConnectMe.FriendRequest;

// Basically a data class to store members
public class FriendRequestModel {
    private String userName;
    private String photoName; // String with the name of the photo stored in Firebase Storage
    private String userID;

    public FriendRequestModel(String userName, String photoName, String userID) {
        this.userName = userName;
        this.photoName = photoName;
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
