package com.example.chatapplication.Messages;

public class MessagesModel {
    private String message;
    private String message_from;
    private String message_id;
    private String timestamp;
    private String type;

    public MessagesModel() {
    }

    public MessagesModel(String message, String message_from, String message_id, String timestamp, String type) {
        this.message = message;
        this.message_from = message_from;
        this.message_id = message_id;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_from() {
        return message_from;
    }

    public void setMessage_from(String message_from) {
        this.message_from = message_from;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
