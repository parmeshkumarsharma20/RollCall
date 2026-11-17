package com.example.rollcall;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AdminRepository;
import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.model.Course;
import com.example.rollcall.model.Subject;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class AttendanceCorrectionActivity extends AppCompatActivity {

    private AttendanceRepository attendanceRepo;
    private AdminRepository adminRepo;

    private Spinner spnCourse, spnSubject;
    private EditText etRoll, etReason;
    private RadioButton rbPresent;

    private List<Course> courseList = new ArrayList<>();
    private List<Subject> allSubjects = new ArrayList<>();
    private List<Subject> filteredSubjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_correction);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        attendanceRepo = AttendanceRepository.getInstance(this);
        adminRepo = AdminRepository.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btnBackCorrection);
        spnCourse = findViewById(R.id.spnCorrectionCourse);
        spnSubject = findViewById(R.id.spnCorrectionSubject);
        etRoll = findViewById(R.id.etCorrectionStudentRoll);
        etReason = findViewById(R.id.etCorrectionReason);
        rbPresent = findViewById(R.id.rbStatusPresent);
        Button btnSubmit = findViewById(R.id.btnSubmitCorrection);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnSubmit != null) btnSubmit.setOnClickListener(v -> submitCorrection());

        loadDropdownData();
    }

    private void loadDropdownData() {
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

    private void submitCorrection() {
        Course selectedCourse = (Course) spnCourse.getSelectedItem();
        Subject selectedSubject = (Subject) spnSubject.getSelectedItem();
        String roll = etRoll.getText().toString().trim();
        String reason = etReason.getText().toString().trim();

        if (selectedCourse == null) {
            Toast.makeText(this, "Please select a Course", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSubject == null) {
            Toast.makeText(this, "Please select a Subject", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(roll)) {
            etRoll.setError("Student roll number required");
            etRoll.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(reason)) {
            etReason.setError("Correction reason required for audit log");
            etReason.requestFocus();
            return;
        }

        String status = rbPresent.isChecked() ? "PRESENT" : "ABSENT";
        User targetStudent = null;
        for (User u : attendanceRepo.getStudents()) {
            if (u.getRollNumber() != null && u.getRollNumber().equalsIgnoreCase(roll)) {
                targetStudent = u;
                break;
            }
        }

        if (targetStudent == null) {
            targetStudent = new User("stud_corr", "Student " + roll, "student@rollcall.com", User.ROLE_STUDENT);
            targetStudent.setRollNumber(roll);
            targetStudent.setCourse(selectedCourse.getCourseName());
        }

        attendanceRepo.markAttendance(targetStudent, 0.0f);

        String msg = "Correction Success!\nCourse: " + selectedCourse.getCourseName() +
                "\nSubject: " + selectedSubject.getSubjectCode() +
                "\nRoll: " + roll + " marked as " + status;
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }
}
