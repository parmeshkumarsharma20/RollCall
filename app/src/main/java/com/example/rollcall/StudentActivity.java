package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.AttendanceRecord;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.List;

public class StudentActivity extends AppCompatActivity {

    private TextView tvStudentAvatarInitials, tvStudentName, tvStudentRoll;
    private TextView tvAttendanceStatusText, tvAttendanceDetail;
    private ImageView imgStatusIcon, imgScanIllustration;
    private Button btnScanQR, btnViewReports, btnViewTimetable;
    private ImageButton btnLogoutStudent;

    private AttendanceRepository attendanceRepo;
    private AuthRepository authRepo;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        attendanceRepo = AttendanceRepository.getInstance(this);
        authRepo = AuthRepository.getInstance(this);
        currentUser = authRepo.getCurrentUser();

        tvStudentAvatarInitials = findViewById(R.id.tvStudentAvatarInitials);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentRoll = findViewById(R.id.tvStudentRoll);

        tvAttendanceStatusText = findViewById(R.id.tvAttendanceStatusText);
        tvAttendanceDetail = findViewById(R.id.tvAttendanceDetail);
        imgStatusIcon = findViewById(R.id.imgStatusIcon);
        imgScanIllustration = findViewById(R.id.imgScanIllustration);

        btnScanQR = findViewById(R.id.btnScanQR);
        btnViewReports = findViewById(R.id.btnViewReports);
        btnViewTimetable = findViewById(R.id.btnViewTimetable);
        btnLogoutStudent = findViewById(R.id.btnLogoutStudent);

        if (currentUser != null) {
            String name = currentUser.getName() != null ? currentUser.getName() : "Student";
            String roll = currentUser.getRollNumber() != null ? currentUser.getRollNumber() : "RC2024001";
            String course = currentUser.getCourse() != null ? currentUser.getCourse() : "Computer Science";

            if (tvStudentName != null) tvStudentName.setText(name);
            if (tvStudentRoll != null) tvStudentRoll.setText("Roll No: " + roll + " • " + course);

            if (name.length() >= 2 && tvStudentAvatarInitials != null) {
                tvStudentAvatarInitials.setText(name.substring(0, 2).toUpperCase());
            }
        }

        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        if (imgScanIllustration != null && pulse != null) {
            imgScanIllustration.startAnimation(pulse);
        }

        if (btnScanQR != null) {
            btnScanQR.setOnClickListener(v -> {
                Intent intent = new Intent(StudentActivity.this, QRScannerActivity.class);
                startActivity(intent);
            });
        }

        if (btnViewReports != null) {
            btnViewReports.setOnClickListener(v -> startActivity(new Intent(StudentActivity.this, ReportsActivity.class)));
        }

        if (btnViewTimetable != null) {
            btnViewTimetable.setOnClickListener(v -> startActivity(new Intent(StudentActivity.this, ManageTimetableActivity.class)));
        }

        if (btnLogoutStudent != null) {
            btnLogoutStudent.setOnClickListener(v -> showLogoutConfirmationDialog());
        }

        updateStatusUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatusUI();
    }

    private void updateStatusUI() {
        boolean isPresent = false;
        float dist = 0;
        List<AttendanceRecord> records = attendanceRepo.getAttendanceRecords();

        if (currentUser != null) {
            for (AttendanceRecord r : records) {
                if (r.getStudentId().equals(currentUser.getId()) && "PRESENT".equalsIgnoreCase(r.getStatus())) {
                    isPresent = true;
                    dist = r.getDistanceMeters();
                    break;
                }
            }
        }

        if (isPresent) {
            if (tvAttendanceStatusText != null) {
                tvAttendanceStatusText.setText("PRESENT");
                tvAttendanceStatusText.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.success_green));
            }
            if (imgStatusIcon != null) {
                imgStatusIcon.setImageResource(R.drawable.ic_check);
                imgStatusIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.success_green));
                imgStatusIcon.setBackgroundResource(R.drawable.bg_badge_present);
            }
            if (tvAttendanceDetail != null) {
                tvAttendanceDetail.setText(String.format("Marked Present • Distance: %d meters (Room 301)", (int) dist));
            }
        } else {
            if (tvAttendanceStatusText != null) {
                tvAttendanceStatusText.setText("NOT MARKED YET");
                tvAttendanceStatusText.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.error_red));
            }
            if (imgStatusIcon != null) {
                imgStatusIcon.setImageResource(R.drawable.ic_error);
                imgStatusIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.error_red));
                imgStatusIcon.setBackgroundResource(R.drawable.bg_badge_absent);
            }
            if (tvAttendanceDetail != null) {
                tvAttendanceDetail.setText("Scan classroom QR code to verify attendance");
            }
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out of RollCall?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    authRepo.logout();
                    Toast.makeText(StudentActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(StudentActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
