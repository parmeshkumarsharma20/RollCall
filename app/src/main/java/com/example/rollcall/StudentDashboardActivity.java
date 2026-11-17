package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.AttendanceRecord;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.List;
import java.util.Locale;

public class StudentDashboardActivity extends AppCompatActivity {

    private AuthRepository authRepo;
    private AttendanceRepository attendanceRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        View bottomNav = findViewById(R.id.layoutStudentBottomNav);
        WindowInsetUtils.applySystemBarInsets(rootView, null, bottomNav);

        authRepo = AuthRepository.getInstance(this);
        attendanceRepo = AttendanceRepository.getInstance(this);

        User currentUser = authRepo.getCurrentUser();

        TextView tvAvatar = findViewById(R.id.tvStudentAvatar);
        TextView tvGreeting = findViewById(R.id.tvStudentGreeting);
        TextView tvSub = findViewById(R.id.tvStudentSub);
        ImageButton btnNotification = findViewById(R.id.btnStudentNotification);

        View cardQuickScan = findViewById(R.id.cardQuickScan);
        View cardQuickSubjects = findViewById(R.id.cardQuickSubjects);
        View cardQuickTimetable = findViewById(R.id.cardQuickTimetable);
        View cardQuickReports = findViewById(R.id.cardQuickReports);

        View navHome = findViewById(R.id.navStudentHome);
        View navScan = findViewById(R.id.navStudentScan);
        View navTimetable = findViewById(R.id.navStudentTimetable);
        View navAttendance = findViewById(R.id.navStudentAttendance);
        View navProfile = findViewById(R.id.navStudentProfile);

        if (currentUser != null) {
            String name = currentUser.getName();
            String nameToDisplay = (!TextUtils.isEmpty(name) && !"User".equalsIgnoreCase(name)) ? name : "Student User";
            if (tvGreeting != null) tvGreeting.setText("Hello, " + nameToDisplay);

            String course = currentUser.getCourse() != null ? currentUser.getCourse() : "B.Tech CSE";
            String sem = currentUser.getSemester() != null ? currentUser.getSemester() : "6th";
            if (tvSub != null) tvSub.setText("Semester " + sem + " • " + course);

            if (nameToDisplay.length() >= 2 && tvAvatar != null) {
                tvAvatar.setText(nameToDisplay.substring(0, 2).toUpperCase());
            }
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(StudentDashboardActivity.this, NotificationActivity.class)));
        }

        View.OnClickListener openScanner = v -> startActivity(new Intent(StudentDashboardActivity.this, QRScannerActivity.class));

        if (cardQuickScan != null) cardQuickScan.setOnClickListener(openScanner);
        if (navScan != null) navScan.setOnClickListener(openScanner);

        if (cardQuickSubjects != null) {
            cardQuickSubjects.setOnClickListener(v -> startActivity(new Intent(StudentDashboardActivity.this, ManageSubjectsActivity.class)));
        }

        if (cardQuickTimetable != null) {
            cardQuickTimetable.setOnClickListener(v -> startActivity(new Intent(StudentDashboardActivity.this, ManageTimetableActivity.class)));
        }

        if (cardQuickReports != null) {
            cardQuickReports.setOnClickListener(v -> startActivity(new Intent(StudentDashboardActivity.this, ReportsActivity.class)));
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                loadStudentStats();
                Toast.makeText(this, "Dashboard refreshed", Toast.LENGTH_SHORT).show();
            });
        }

        if (navTimetable != null) {
            navTimetable.setOnClickListener(v -> startActivity(new Intent(StudentDashboardActivity.this, ManageTimetableActivity.class)));
        }

        if (navAttendance != null) {
            navAttendance.setOnClickListener(v -> startActivity(new Intent(StudentDashboardActivity.this, ReportsActivity.class)));
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(StudentDashboardActivity.this, ProfileActivity.class)));
        }

        loadStudentStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudentStats();
    }

    private void loadStudentStats() {
        User currentUser = authRepo.getCurrentUser();

        TextView tvPercent = findViewById(R.id.tvStudentOverallPercent);
        ProgressBar pbOverall = findViewById(R.id.pbOverallAttendance);
        TextView tvTotal = findViewById(R.id.tvStudentTotalClasses);
        TextView tvAttended = findViewById(R.id.tvStudentAttendedClasses);
        TextView tvMissed = findViewById(R.id.tvStudentMissedClasses);
        TextView tvBadge = findViewById(R.id.tvStudentTrackBadge);

        if (currentUser == null) return;

        int totalSessions = 1;
        int attendedCount = 0;

        List<AttendanceRecord> records = attendanceRepo.getAttendanceRecords();
        if (records != null) {
            for (AttendanceRecord r : records) {
                if (r != null && r.getStudentId() != null && currentUser.getId() != null && r.getStudentId().equalsIgnoreCase(currentUser.getId())) {
                    if ("PRESENT".equalsIgnoreCase(r.getStatus())) {
                        attendedCount++;
                    }
                }
            }
        }

        if (attendedCount > 0) totalSessions = attendedCount;
        int missedCount = Math.max(0, totalSessions - attendedCount);
        float percent = totalSessions > 0 ? ((float) attendedCount / totalSessions) * 100f : 100f;

        if (tvPercent != null) tvPercent.setText(String.format(Locale.US, "%.0f%%", percent));
        if (pbOverall != null) pbOverall.setProgress((int) percent);
        if (tvTotal != null) tvTotal.setText(String.valueOf(totalSessions));
        if (tvAttended != null) tvAttended.setText(String.valueOf(attendedCount));
        if (tvMissed != null) tvMissed.setText(String.valueOf(missedCount));

        if (tvBadge != null) {
            if (percent >= 75f) {
                tvBadge.setText("ON TRACK");
                tvBadge.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.success_green));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_present);
            } else {
                tvBadge.setText("WARNING");
                tvBadge.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.error_red));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_absent);
            }
        }
    }
}
