package com.example.stopwatchapp;

public class Lap {
    private String time;
    private long timestamp;
    private String id;

    public Lap() {}  // Required for Firebase

    public Lap(String time, long timestamp) {
        this.time = time;
        this.timestamp = timestamp;
    }

    // Add this toString() method
    @Override
    public String toString() {
        return time; // This will display the lap time in the ListView
    }

    // Getters and setters
    public String getTime() { return time; }
    public long getTimestamp() { return timestamp; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}