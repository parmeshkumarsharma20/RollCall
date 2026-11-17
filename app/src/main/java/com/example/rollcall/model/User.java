package com.example.rollcall.model;

import java.io.Serializable;

public class User implements Serializable {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_PENDING = "pending_approval";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_SUSPENDED = "suspended";

    private String uid;
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private String role;
    private boolean activeStatus = true;
    private String accountStatus = STATUS_ACTIVE;
    private long createdAt = System.currentTimeMillis();
    private long approvedAt = 0;
    private String approvedBy = "";

    private String profileImage;
    private String rollNumber;
    private String department;
    private String course;
    private String semester;
    private String section;
    private String employeeId;

    public User() {
    }

    public User(String uid, String name, String email, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.activeStatus = true;
        this.accountStatus = ROLE_ADMIN.equalsIgnoreCase(role) ? STATUS_ACTIVE : STATUS_PENDING;
    }

    public User(String id, String name, String email, String password, String role, String rollNumber, String department) {
        this.uid = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.rollNumber = rollNumber;
        this.department = department;
        this.activeStatus = true;
        this.accountStatus = ROLE_ADMIN.equalsIgnoreCase(role) ? STATUS_ACTIVE : STATUS_PENDING;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return uid;
    }

    public void setId(String id) {
        this.uid = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getAccountStatus() {
        return accountStatus != null ? accountStatus : (activeStatus ? STATUS_ACTIVE : STATUS_SUSPENDED);
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
        this.activeStatus = STATUS_ACTIVE.equalsIgnoreCase(accountStatus);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(long approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("uid", uid != null ? uid : "");
        map.put("name", name != null ? name : "");
        map.put("email", email != null ? email : "");
        map.put("phoneNumber", phoneNumber != null ? phoneNumber : "");
        map.put("password", password != null ? password : "");
        map.put("role", role != null ? role : "STUDENT");
        map.put("accountStatus", getAccountStatus());
        map.put("activeStatus", activeStatus);
        map.put("rollNumber", rollNumber != null ? rollNumber : "");
        map.put("department", department != null ? department : "");
        map.put("course", course != null ? course : "");
        map.put("semester", semester != null ? semester : "");
        map.put("section", section != null ? section : "");
        map.put("employeeId", employeeId != null ? employeeId : "");
        map.put("createdAt", createdAt);
        return map;
    }
}
