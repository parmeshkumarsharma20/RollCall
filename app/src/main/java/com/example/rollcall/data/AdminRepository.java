package com.example.rollcall.data;

import android.content.Context;

import com.example.rollcall.model.Course;
import com.example.rollcall.model.Room;
import com.example.rollcall.model.Subject;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminRepository {
    private static AdminRepository instance;
    private final FirebaseFirestore firestore;

    public interface ListCallback<T> {
        void onSuccess(List<T> items);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    private AdminRepository(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized AdminRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AdminRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void addStudent(User student, ActionCallback callback) {
        if (student.getUid() == null || student.getUid().isEmpty()) {
            student.setUid("stud_" + UUID.randomUUID().toString().substring(0, 8));
        }
        student.setRole(User.ROLE_STUDENT);
        firestore.collection(Constants.COLL_USERS).document(student.getUid()).set(student.toMap())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void approveUser(String uid, String adminUid, ActionCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("User UID is invalid.");
            return;
        }

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("accountStatus", User.STATUS_ACTIVE);
        updates.put("activeStatus", true);
        updates.put("approvedAt", System.currentTimeMillis());
        updates.put("approvedBy", adminUid != null ? adminUid : "admin_master");

        firestore.collection(Constants.COLL_USERS).document(uid).update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void rejectUser(String uid, String adminUid, ActionCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("User UID is invalid.");
            return;
        }

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("accountStatus", User.STATUS_REJECTED);
        updates.put("activeStatus", false);

        firestore.collection(Constants.COLL_USERS).document(uid).update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addTeacher(User teacher, ActionCallback callback) {
        if (teacher.getUid() == null || teacher.getUid().isEmpty()) {
            teacher.setUid("teach_" + UUID.randomUUID().toString().substring(0, 8));
        }
        teacher.setRole(User.ROLE_TEACHER);
        firestore.collection(Constants.COLL_USERS).document(teacher.getUid()).set(teacher.toMap())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchStudents(ListCallback<User> callback) {
        firestore.collection(Constants.COLL_USERS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            u.setUid(doc.getId());
                            String role = u.getRole() != null ? u.getRole().toUpperCase() : "";
                            if ("STUDENT".equals(role)) {
                                list.add(u);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchTeachers(ListCallback<User> callback) {
        firestore.collection(Constants.COLL_USERS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            u.setUid(doc.getId());
                            String role = u.getRole() != null ? u.getRole().toUpperCase() : "";
                            if ("TEACHER".equals(role)) {
                                list.add(u);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addRoom(Room room, ActionCallback callback) {
        if (room.getRoomId() == null || room.getRoomId().isEmpty()) {
            room.setRoomId("room_" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection(Constants.COLL_ROOMS).document(room.getRoomId()).set(room)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchRooms(ListCallback<Room> callback) {
        firestore.collection(Constants.COLL_ROOMS).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Room> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Room r = doc.toObject(Room.class);
                        if (r != null) {
                            r.setRoomId(doc.getId());
                            list.add(r);
                        }
                    }
                    if (list.isEmpty()) {
                        Room defaultRoom = new Room("room_301", "Room 301", "CS Building", 28.6139, 77.2090, 25.0f);
                        list.add(defaultRoom);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addCourse(Course course, ActionCallback callback) {
        if (course.getCourseId() == null || course.getCourseId().isEmpty()) {
            course.setCourseId("c_" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection(Constants.COLL_COURSES).document(course.getCourseId()).set(course)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchCourses(ListCallback<Course> callback) {
        firestore.collection(Constants.COLL_COURSES).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Course> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Course c = doc.toObject(Course.class);
                        if (c != null) {
                            c.setCourseId(doc.getId());
                            list.add(c);
                        }
                    }
                    if (list.isEmpty()) {
                        list.add(new Course("c_1", "B.Tech CSE", "CSE", 4, "Bachelor of Technology in CSE"));
                        list.add(new Course("c_2", "BCA", "BCA", 3, "Bachelor of Computer Applications"));
                        list.add(new Course("c_3", "B.Com", "BCOM", 3, "Bachelor of Commerce"));
                        list.add(new Course("c_4", "MCA", "MCA", 2, "Master of Computer Applications"));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addSubject(Subject subject, ActionCallback callback) {
        if (subject.getSubjectId() == null || subject.getSubjectId().isEmpty()) {
            subject.setSubjectId("subj_" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection(Constants.COLL_SUBJECTS).document(subject.getSubjectId()).set(subject)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchSubjects(ListCallback<Subject> callback) {
        firestore.collection(Constants.COLL_SUBJECTS).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Subject> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Subject s = doc.toObject(Subject.class);
                        if (s != null) {
                            s.setSubjectId(doc.getId());
                            list.add(s);
                        }
                    }
                    if (list.isEmpty()) {
                        list.add(new Subject("subj_1", "CS301", "Data Structures & Algorithms", "B.Tech CSE", "6th", "CSE_6A"));
                        list.add(new Subject("subj_2", "CS302", "Database Management Systems", "B.Tech CSE", "6th", "CSE_6A"));
                        list.add(new Subject("subj_3", "CS303", "Computer Networks", "B.Tech CSE", "6th", "CSE_6A"));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
