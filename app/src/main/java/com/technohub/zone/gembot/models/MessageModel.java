package com.technohub.zone.gembot.models;

/**
 * MessageModel represents a single chat message.
 * It can be used for storing/retrieving messages from Firebase.
 */
public class MessageModel {
    private String sender;    // "user" or "bot"
    private String message;   // Message text
    private long timestamp;   // Time of the message in milliseconds

    // Empty constructor required for Firebase deserialization
    public MessageModel() {
    }

    // Constructor with parameters
    public MessageModel(String sender, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
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
