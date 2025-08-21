package com.technohub.zone.gembot.models;

public class MessageModel {
    private String sender;
    private String message;
    private long timestamp;

    // Constructor
    public MessageModel(String sender, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Empty constructor (needed for Firebase or serialization)
    public MessageModel() {
    }

    // Getters
    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
