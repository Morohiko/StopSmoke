package com.example.stopsmoke;

import androidx.annotation.NonNull;

/**
 * Represents a log entry for a smoked cigarette.
 */
public class CigaretteLog {
    private int id;
    private long timestamp;

    /**
     * Default constructor.
     */
    public CigaretteLog() {
    }

    /**
     * Constructor without ID (useful for inserting new logs where ID is auto-generated).
     *
     * @param timestamp The time when the cigarette was smoked, in milliseconds since epoch.
     */
    public CigaretteLog(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Constructor with all fields.
     *
     * @param id        The unique identifier for the log entry.
     * @param timestamp The time when the cigarette was smoked, in milliseconds since epoch.
     */
    public CigaretteLog(int id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    // Getter and Setter methods

    /**
     * Gets the ID of the log entry.
     *
     * @return The log entry ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the log entry.
     *
     * @param id The log entry ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the timestamp of when the cigarette was smoked.
     *
     * @return The timestamp in milliseconds since epoch.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of when the cigarette was smoked.
     *
     * @param timestamp The timestamp in milliseconds since epoch.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Provides a string representation of the CigaretteLog.
     *
     * @return A string containing the ID and formatted timestamp.
     */
    @NonNull
    @Override
    public String toString() {
        return "CigaretteLog{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                '}';
    }
}
