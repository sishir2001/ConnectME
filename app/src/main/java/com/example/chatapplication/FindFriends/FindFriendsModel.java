package com.example.chatapplication.FindFriends;

public class FindFriendsModel {
    // similar to data class in kotlin
    private String userName;
    private String userPhoto; // uri of the user photo stored in
    private String userId;
    private boolean sendRequest;
    private boolean acceptRequest;

    public FindFriendsModel(String userName, String userPhoto, String userId, boolean sendRequest,boolean acceptRequest) {
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.userId = userId;
        this.sendRequest = sendRequest;
        this.acceptRequest = acceptRequest;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSendRequest() {
        return sendRequest;
    }

    public void setSendRequest(boolean sendRequest) {
        this.sendRequest = sendRequest;
    }

    public boolean isAcceptRequest() {
        return acceptRequest;
    }

    public void setAcceptRequest(boolean acceptRequest) {
        this.acceptRequest = acceptRequest;}
}
