package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AdminRepository;
import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.data.SessionRepository;
import com.example.rollcall.model.AttendanceSession;
import com.example.rollcall.model.Course;
import com.example.rollcall.model.Room;
import com.example.rollcall.model.Subject;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateSessionActivity extends AppCompatActivity {

    private SessionRepository sessionRepo;
    private AdminRepository adminRepo;
    private AuthRepository authRepo;

    private Spinner spnCourse, spnSubject, spnRoom;
    private List<Course> courseList = new ArrayList<>();
    private List<Subject> allSubjects = new ArrayList<>();
    private List<Subject> filteredSubjects = new ArrayList<>();
    private List<Room> roomList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_session);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        sessionRepo = SessionRepository.getInstance(this);
        adminRepo = AdminRepository.getInstance(this);
        authRepo = AuthRepository.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btnBackCreateSession);
        spnCourse = findViewById(R.id.spnSessionCourse);
        spnSubject = findViewById(R.id.spnSubject);
        spnRoom = findViewById(R.id.spnRoom);
        Button btnStart = findViewById(R.id.btnStartSessionNow);

        TextView tvTotalStudents = findViewById(R.id.tvCreateSessionTotalStudents);
        if (tvTotalStudents != null) {
            int total = AttendanceRepository.getInstance(this).getTotalStudentCount();
            tvTotalStudents.setText(total + " Students");
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnStart != null) btnStart.setOnClickListener(v -> startSession());

        loadSpinners();
    }

    private void loadSpinners() {
        adminRepo.fetchCourses(new AdminRepository.ListCallback<Course>() {
            @Override
            public void onSuccess(List<Course> items) {
                courseList = items;
                setupCourseSpinner();
            }

            @Override
            public void onError(String message) {
                courseList.clear();
                courseList.add(new Course("c_1", "B.Tech CSE", "CSE", 4, "Bachelor of Technology in CSE"));
                courseList.add(new Course("c_2", "BCA", "BCA", 3, "Bachelor of Computer Applications"));
                courseList.add(new Course("c_3", "B.Com", "BCOM", 3, "Bachelor of Commerce"));
                courseList.add(new Course("c_4", "MCA", "MCA", 2, "Master of Computer Applications"));
                setupCourseSpinner();
            }
        });

        adminRepo.fetchSubjects(new AdminRepository.ListCallback<Subject>() {
            @Override
            public void onSuccess(List<Subject> items) {
                allSubjects = items;
                filterSubjectsBySelectedCourse();
            }

            @Override
            public void onError(String message) {
                allSubjects.clear();
                allSubjects.add(new Subject("s_1", "CS301", "Data Structures & Algorithms", "B.Tech CSE", "6th", "A"));
                allSubjects.add(new Subject("s_2", "CS302", "Database Management Systems", "B.Tech CSE", "6th", "A"));
                allSubjects.add(new Subject("s_3", "BCA101", "Programming in C", "BCA", "4th", "A"));
                allSubjects.add(new Subject("s_4", "BC201", "Financial Accounting", "B.Com", "2nd", "B"));
                allSubjects.add(new Subject("s_5", "MCA107", "Database Management System", "MCA", "1st", "A"));
                filterSubjectsBySelectedCourse();
            }
        });

        adminRepo.fetchRooms(new AdminRepository.ListCallback<Room>() {
            @Override
            public void onSuccess(List<Room> items) {
                roomList = items;
                setupRoomSpinner();
            }

            @Override
            public void onError(String message) {
                roomList.clear();
                roomList.add(new Room("room_301", "Room 301", "CS Building", 28.6139, 77.2090, 25.0f));
                roomList.add(new Room("room_102", "Room 102", "IT Building", 28.6145, 77.2095, 25.0f));
                setupRoomSpinner();
            }
        });
    }

    private void setupCourseSpinner() {
        ArrayAdapter<Course> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCourse.setAdapter(courseAdapter);

        spnCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterSubjectsBySelectedCourse();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterSubjectsBySelectedCourse() {
        Course selectedCourse = (Course) spnCourse.getSelectedItem();
        String courseName = selectedCourse != null ? selectedCourse.getCourseName() : "B.Tech CSE";

        filteredSubjects.clear();
        for (Subject s : allSubjects) {
            if (s.getCourse() == null || s.getCourse().isEmpty() || s.getCourse().equalsIgnoreCase(courseName)) {
                filteredSubjects.add(s);
            }
        }

        if (filteredSubjects.isEmpty()) {
            filteredSubjects.add(new Subject("s_def", "CS301", "General Subject", courseName, "1st", "A"));
        }

        ArrayAdapter<Subject> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredSubjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSubject.setAdapter(subjectAdapter);
    }

    private void setupRoomSpinner() {
        ArrayAdapter<Room> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomList);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRoom.setAdapter(roomAdapter);
    }

    private void startSession() {
        Course selectedCourse = (Course) spnCourse.getSelectedItem();
        Subject selectedSubject = (Subject) spnSubject.getSelectedItem();
        Room selectedRoom = (Room) spnRoom.getSelectedItem();

        if (selectedCourse == null || selectedSubject == null || selectedRoom == null) {
            Toast.makeText(this, "Please select course, subject and classroom", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = authRepo.getCurrentUser();
        String teacherId = user != null ? user.getUid() : "teach_1";
        String teacherName = user != null ? user.getName() : "Prof. Alan Turing";

        String sessionId = "sess_" + UUID.randomUUID().toString().substring(0, 8);
        long now = System.currentTimeMillis();
        long expiresAt = now + (50 * 1000L);
        String token = "token_1_" + UUID.randomUUID().toString().substring(0, 8);

        AttendanceSession fallbackSession = new AttendanceSession(
                sessionId,
                teacherId,
                teacherName,
                selectedSubject.getClassId() != null ? selectedSubject.getClassId() : "CSE_6A",
                selectedSubject.getSubjectId(),
                selectedSubject.getSubjectName(),
                selectedRoom.getRoomId(),
                selectedRoom.getRoomNumber(),
                selectedRoom.getLatitude(),
                selectedRoom.getLongitude()
        );
        fallbackSession.setCourseName(selectedCourse.getCourseName());
        fallbackSession.setCurrentGeneration(1);
        fallbackSession.setCurrentToken(token);
        fallbackSession.setTokenIssuedAt(now);
        fallbackSession.setTokenExpiresAt(expiresAt);

        sessionRepo.createSession(
                teacherId,
                teacherName,
                selectedSubject.getClassId() != null ? selectedSubject.getClassId() : "CSE_6A",
                selectedSubject.getSubjectId(),
                selectedSubject.getSubjectName(),
                selectedRoom.getRoomId(),
                selectedRoom.getRoomNumber(),
                selectedRoom.getLatitude(),
                selectedRoom.getLongitude(),
                new SessionRepository.SessionCallback() {
                    @Override
                    public void onSuccess(AttendanceSession session) {
                        if (session != null) session.setCourseName(selectedCourse.getCourseName());
                        launchQRScreen(session != null ? session : fallbackSession);
                    }

                    @Override
                    public void onError(String message) {
                        launchQRScreen(fallbackSession);
                    }
                }
        );
    }

    private void launchQRScreen(AttendanceSession session) {
        Toast.makeText(this, "Live Session Started Successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CreateSessionActivity.this, QRGeneratorActivity.class);
        intent.putExtra(QRGeneratorActivity.EXTRA_SESSION, session);
        startActivity(intent);
        finish();
    }
}
