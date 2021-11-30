package com.example.ConnectMe.Chat;

public class ChatListModel {
    private String userId;
    private String userName;
    private String userPhoto;
    private String lastMessage;
    private String lastMessageTime;
    private String unseenCount;

    public ChatListModel(String userId, String userName, String userPhoto, String lastMessage, String lastMessageTime, String unseenCount) {
        this.userId = userId;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unseenCount = unseenCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getUnseenCount() {
        return unseenCount;
    }

    public void setUnseenCount(String unseenCount) {
        this.unseenCount = unseenCount;
    }
}
