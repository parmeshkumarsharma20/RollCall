package com.example.rollcall.model;

import java.io.Serializable;

public class Room implements Serializable {
    private String roomId;
    private String roomNumber;
    private String buildingName;
    private double latitude;
    private double longitude;
    private float allowedRadiusMeters = 25.0f;
    private boolean activeStatus = true;

    public Room() {
    }

    public Room(String roomId, String roomNumber, String buildingName, double latitude, double longitude, float allowedRadiusMeters) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.buildingName = buildingName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.allowedRadiusMeters = allowedRadiusMeters;
        this.activeStatus = true;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAllowedRadiusMeters() {
        return allowedRadiusMeters;
    }

    public void setAllowedRadiusMeters(float allowedRadiusMeters) {
        this.allowedRadiusMeters = allowedRadiusMeters;
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    @Override
    public String toString() {
        return roomNumber + " (" + buildingName + ")";
    }
}
