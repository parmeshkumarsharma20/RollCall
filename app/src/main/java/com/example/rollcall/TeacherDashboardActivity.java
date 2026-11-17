package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.List;
import java.util.Locale;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvTeacherAvatar, tvTeacherGreeting;
    private TextView tvTeacherStudentsCount, tvTeacherAttendancePercent;
    private AttendanceRepository attendanceRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        View rootView = findViewById(android.R.id.content);
        View bottomNav = findViewById(R.id.layoutTeacherBottomNav);
        WindowInsetUtils.applySystemBarInsets(rootView, null, bottomNav);

        attendanceRepo = AttendanceRepository.getInstance(this);

        initViews();
        setupTeacherHeader();
        setupClickListeners();
        loadTeacherDashboardStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeacherDashboardStats();
    }

    private void initViews() {
        tvTeacherAvatar = findViewById(R.id.tvTeacherAvatar);
        tvTeacherGreeting = findViewById(R.id.tvTeacherGreeting);
        tvTeacherStudentsCount = findViewById(R.id.tvTeacherStudentsCount);
        tvTeacherAttendancePercent = findViewById(R.id.tvTeacherAttendancePercent);
    }

    private void setupTeacherHeader() {
        AuthRepository authRepo = AuthRepository.getInstance(this);
        User currentUser = authRepo.getCurrentUser();

        if (currentUser != null) {
            String name = currentUser.getName() != null ? currentUser.getName() : "Faculty";
            if (tvTeacherGreeting != null) {
                tvTeacherGreeting.setText("Good Morning, " + name);
            }
            if (name.length() >= 2 && tvTeacherAvatar != null) {
                tvTeacherAvatar.setText(name.substring(0, 2).toUpperCase());
            }
        }
    }

    private void loadTeacherDashboardStats() {
        com.example.rollcall.data.AdminRepository.getInstance(this).fetchStudents(new com.example.rollcall.data.AdminRepository.ListCallback<User>() {
            @Override
            public void onSuccess(List<User> items) {
                int totalStudents = items != null && !items.isEmpty() ? items.size() : attendanceRepo.getTotalStudentCount();
                int presentCount = attendanceRepo.getPresentCount();

                if (tvTeacherStudentsCount != null) {
                    tvTeacherStudentsCount.setText(String.valueOf(totalStudents));
                }

                if (tvTeacherAttendancePercent != null) {
                    float percent = totalStudents > 0 ? ((float) presentCount / totalStudents) * 100f : 0f;
                    tvTeacherAttendancePercent.setText(String.format(Locale.US, "%.0f%%", percent));
                }
            }

            @Override
            public void onError(String message) {
                int totalStudents = attendanceRepo.getTotalStudentCount();
                int presentCount = attendanceRepo.getPresentCount();

                if (tvTeacherStudentsCount != null) {
                    tvTeacherStudentsCount.setText(String.valueOf(totalStudents));
                }

                if (tvTeacherAttendancePercent != null) {
                    float percent = totalStudents > 0 ? ((float) presentCount / totalStudents) * 100f : 0f;
                    tvTeacherAttendancePercent.setText(String.format(Locale.US, "%.0f%%", percent));
                }
            }
        });
    }

    private void setupClickListeners() {
        ImageButton btnNotification = findViewById(R.id.btnTeacherNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        }

        Button btnStartAttendance = findViewById(R.id.btnStartAttendance);
        if (btnStartAttendance != null) {
            btnStartAttendance.setOnClickListener(v -> startActivity(new Intent(this, CreateSessionActivity.class)));
        }

        View cardTeacherClasses = findViewById(R.id.cardTeacherClasses);
        if (cardTeacherClasses != null) {
            cardTeacherClasses.setOnClickListener(v -> startActivity(new Intent(this, ManageSubjectsActivity.class)));
        }

        View cardTeacherRoster = findViewById(R.id.cardTeacherRoster);
        if (cardTeacherRoster != null) {
            cardTeacherRoster.setOnClickListener(v -> startActivity(new Intent(this, AttendanceListActivity.class)));
        }

        View cardTeacherTimetable = findViewById(R.id.cardTeacherTimetable);
        if (cardTeacherTimetable != null) {
            cardTeacherTimetable.setOnClickListener(v -> startActivity(new Intent(this, ManageTimetableActivity.class)));
        }

        View cardTeacherReports = findViewById(R.id.cardTeacherReports);
        if (cardTeacherReports != null) {
            cardTeacherReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        }

        View cardTeacherCorrection = findViewById(R.id.cardTeacherCorrection);
        if (cardTeacherCorrection != null) {
            cardTeacherCorrection.setOnClickListener(v -> startActivity(new Intent(this, AttendanceCorrectionActivity.class)));
        }

        // Bottom Navigation Bar
        View navHome = findViewById(R.id.navTeacherHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                loadTeacherDashboardStats();
                Toast.makeText(this, "Faculty Dashboard Refreshed", Toast.LENGTH_SHORT).show();
            });
        }

        View navSessions = findViewById(R.id.navTeacherSessions);
        if (navSessions != null) {
            navSessions.setOnClickListener(v -> startActivity(new Intent(this, CreateSessionActivity.class)));
        }

        View navReports = findViewById(R.id.navTeacherReports);
        if (navReports != null) {
            navReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        }

        View navProfile = findViewById(R.id.navTeacherProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
    }
}
