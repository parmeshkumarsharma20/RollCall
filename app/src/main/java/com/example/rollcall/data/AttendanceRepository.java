package com.example.rollcall.data;

import android.content.Context;

import com.example.rollcall.model.AttendanceRecord;
import com.example.rollcall.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceRepository {
    private static AttendanceRepository instance;
    private final Map<String, User> userDatabase = new HashMap<>();
    private final List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private User currentUser;

    private AttendanceRepository(Context context) {
        initDefaultUsers();
    }

    public static synchronized AttendanceRepository getInstance(Context context) {
        if (instance == null) {
            Context appContext = context != null ? context.getApplicationContext() : null;
            instance = new AttendanceRepository(appContext);
        }
        return instance;
    }

    private void initDefaultUsers() {
        // Master Admin User
        User admin = new User("admin_1", "Parmesh Kumar Sharma", "sharmaparmeshkumar8@gmail.com", "admin123", User.ROLE_ADMIN, "ADM001", "Administration");
        admin.setPhoneNumber("+918882062772");
        admin.setPassword("admin123");
        userDatabase.put(admin.getEmail().toLowerCase(), admin);
        userDatabase.put("8882062772", admin);

        // Teacher Demo User
        User teacher = new User("teach_1", "Teacher Demo", "teacher@rollcall.com", "teacher123", User.ROLE_TEACHER, "EMP101", "Computer Science");
        teacher.setPhoneNumber("+919999999999");
        teacher.setPassword("teacher123");
        userDatabase.put(teacher.getEmail().toLowerCase(), teacher);
        userDatabase.put("9999999999", teacher);

        // Student Demo User
        User student = new User("stud_555", "Student Demo", "student@rollcall.com", "student123", User.ROLE_STUDENT, "RC2024555", "Computer Science");
        student.setPhoneNumber("+915555555555");
        student.setPassword("student123");
        student.setCourse("B.Tech CSE");
        student.setSemester("1st");
        student.setSection("A");
        userDatabase.put(student.getEmail().toLowerCase(), student);
        userDatabase.put("5555555555", student);

        // Push directly to Cloud Firestore Database
        try {
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            db.collection("users").document(admin.getUid()).set(admin.toMap());
            db.collection("users").document(teacher.getUid()).set(teacher.toMap());
            db.collection("users").document(student.getUid()).set(student.toMap());
        } catch (Exception ignored) {}
    }

    public User authenticate(String email, String password) {
        if (email == null || password == null) return null;
        User user = userDatabase.get(email.trim().toLowerCase());
        if (user != null && user.getPassword().equals(password.trim())) {
            this.currentUser = user;
            return user;
        }
        return null;
    }

    public boolean registerUser(User newUser) {
        if (newUser == null || newUser.getEmail() == null) return false;
        String key = newUser.getEmail().trim().toLowerCase();
        if (userDatabase.containsKey(key)) {
            return false; // Email already exists
        }
        userDatabase.put(key, newUser);
        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userDatabase.values());
    }

    public List<User> getStudents() {
        List<User> list = new ArrayList<>();
        for (User u : userDatabase.values()) {
            if (User.ROLE_STUDENT.equals(u.getRole())) {
                list.add(u);
            }
        }
        return list;
    }

    public List<User> getTeachers() {
        List<User> list = new ArrayList<>();
        for (User u : userDatabase.values()) {
            if (User.ROLE_TEACHER.equals(u.getRole())) {
                list.add(u);
            }
        }
        return list;
    }

    public boolean markAttendance(User student, float distanceMeters) {
        if (student == null) return false;
        
        for (AttendanceRecord r : attendanceRecords) {
            if (r.getStudentId().equals(student.getId())) {
                r.setStatus("PRESENT");
                return true; // Already marked
            }
        }

        AttendanceRecord record = new AttendanceRecord(
                "rec_" + System.currentTimeMillis(),
                student.getId(),
                student.getName(),
                student.getRollNumber(),
                System.currentTimeMillis(),
                distanceMeters,
                "PRESENT"
        );
        attendanceRecords.add(record);
        return true;
    }

    public List<AttendanceRecord> getAttendanceRecords() {
        return new ArrayList<>(attendanceRecords);
    }

    public int getPresentCount() {
        int count = 0;
        for (AttendanceRecord r : attendanceRecords) {
            if ("PRESENT".equalsIgnoreCase(r.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public int getTotalStudentCount() {
        return getStudents().size();
    }

    // Subject-Specific Analytics Helpers
    public List<AttendanceRecord> getSubjectAttendanceRecords(String subjectId) {
        List<AttendanceRecord> result = new ArrayList<>();
        if (subjectId == null) return result;
        for (AttendanceRecord r : attendanceRecords) {
            if (subjectId.equalsIgnoreCase(r.getSubjectId())) {
                result.add(r);
            }
        }
        return result;
    }

    public int getStudentSubjectPresentCount(String studentId, String subjectId) {
        int count = 0;
        if (studentId == null) return count;
        for (AttendanceRecord r : attendanceRecords) {
            if (studentId.equalsIgnoreCase(r.getStudentId()) && "PRESENT".equalsIgnoreCase(r.getStatus())) {
                if (subjectId == null || subjectId.equalsIgnoreCase(r.getSubjectId())) {
                    count++;
                }
            }
        }
        return count;
    }

    public float getStudentSubjectAttendancePercent(String studentId, String subjectId, int totalClasses) {
        if (totalClasses <= 0) return 0f;
        int present = getStudentSubjectPresentCount(studentId, subjectId);
        return Math.min(100f, ((float) present / totalClasses) * 100f);
    }

    public float getStudentOverallAttendancePercent(String studentId, int totalConductedAcrossSubjects, int totalPresentAcrossSubjects) {
        if (totalConductedAcrossSubjects <= 0) return 100f;
        return Math.min(100f, ((float) totalPresentAcrossSubjects / totalConductedAcrossSubjects) * 100f);
    }
}
