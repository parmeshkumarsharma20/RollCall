package com.example.rollcall.data;

import android.content.Context;

import com.example.rollcall.model.AcademicSession;
import com.example.rollcall.model.Course;
import com.example.rollcall.model.Section;
import com.example.rollcall.model.Semester;
import com.example.rollcall.model.TimetableSlot;
import com.example.rollcall.utils.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AcademicRepository {
    private static AcademicRepository instance;
    private final FirebaseFirestore firestore;

    public interface ListCallback<T> {
        void onSuccess(List<T> items);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    private AcademicRepository(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized AcademicRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AcademicRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void fetchCourses(ListCallback<Course> callback) {
        firestore.collection(Constants.COLL_CLASSES).get()
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
                        list.add(new Course("crs_bca", "Bachelor of Computer Applications", "BCA", 3, "UG Computer Applications"));
                        list.add(new Course("crs_bcom", "Bachelor of Commerce", "B.Com", 3, "UG Commerce"));
                        list.add(new Course("crs_bba", "Bachelor of Business Administration", "BBA", 3, "UG Business"));
                        list.add(new Course("crs_mca", "Master of Computer Applications", "MCA", 2, "PG Computer Applications"));
                        list.add(new Course("crs_mcom", "Master of Commerce", "M.Com", 2, "PG Commerce"));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addCourse(Course course, ActionCallback callback) {
        if (course.getCourseId() == null || course.getCourseId().isEmpty()) {
            course.setCourseId("crs_" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection(Constants.COLL_CLASSES).document(course.getCourseId()).set(course)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchAcademicSessions(ListCallback<AcademicSession> callback) {
        firestore.collection("academicSessions").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AcademicSession> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        AcademicSession s = doc.toObject(AcademicSession.class);
                        if (s != null) {
                            s.setSessionId(doc.getId());
                            list.add(s);
                        }
                    }
                    if (list.isEmpty()) {
                        list.add(new AcademicSession("sess_2026_27", "2026-27", true));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addAcademicSession(AcademicSession session, ActionCallback callback) {
        if (session.getSessionId() == null || session.getSessionId().isEmpty()) {
            session.setSessionId("sess_" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection("academicSessions").document(session.getSessionId()).set(session)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchSemesters(String courseId, ListCallback<Semester> callback) {
        firestore.collection("semesters")
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Semester> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Semester s = doc.toObject(Semester.class);
                        if (s != null) {
                            s.setSemesterId(doc.getId());
                            list.add(s);
                        }
                    }
                    if (list.isEmpty()) {
                        for (int i = 1; i <= 6; i++) {
                            list.add(new Semester("sem_" + i, courseId, i, "Semester " + i));
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchSections(String courseId, String semesterId, ListCallback<Section> callback) {
        firestore.collection("sections")
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Section> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Section sec = doc.toObject(Section.class);
                        if (sec != null) {
                            sec.setSectionId(doc.getId());
                            list.add(sec);
                        }
                    }
                    if (list.isEmpty()) {
                        list.add(new Section("sec_a", courseId, semesterId, "Section A"));
                        list.add(new Section("sec_b", courseId, semesterId, "Section B"));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchTimetable(String courseId, ListCallback<TimetableSlot> callback) {
        firestore.collection("timetables")
                .whereEqualTo("courseId", courseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TimetableSlot> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        TimetableSlot slot = doc.toObject(TimetableSlot.class);
                        if (slot != null) {
                            slot.setSlotId(doc.getId());
                            list.add(slot);
                        }
                    }
                    if (list.isEmpty()) {
                        list.add(new TimetableSlot("slot_1", courseId, "sem_1", "sec_a", "subj_1", "Data Structures", "teach_1", "Prof. Alan Turing", "room_301", "Room 301", "Monday", "09:00 AM", "10:00 AM"));
                        list.add(new TimetableSlot("slot_2", courseId, "sem_1", "sec_a", "subj_2", "Database Management", "teach_2", "Dr. Ada Lovelace", "room_301", "Room 301", "Monday", "10:00 AM", "11:00 AM"));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
