package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

public class ProfileActivity extends AppCompatActivity {

    private AuthRepository authRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        authRepo = AuthRepository.getInstance(this);
        User currentUser = authRepo.getCurrentUser();

        ImageButton btnBack = findViewById(R.id.btnBackProfile);
        ImageButton btnSettings = findViewById(R.id.btnSettingsProfile);
        TextView tvAvatar = findViewById(R.id.tvProfileAvatarInitials);
        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvRoleBadge = findViewById(R.id.tvProfileRoleBadge);
        TextView tvStatusSub = findViewById(R.id.tvProfileStatusSub);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        TextView tvRollLabel = findViewById(R.id.tvRollLabel);
        TextView tvRoll = findViewById(R.id.tvProfileRoll);
        TextView tvDept = findViewById(R.id.tvProfileDept);
        TextView tvAccessLevel = findViewById(R.id.tvProfileAccessLevel);
        Button btnLogout = findViewById(R.id.btnLogoutProfile);

        // Feature cards
        View cardProfileSettings = findViewById(R.id.cardProfileSettings);
        View cardProfileAnalytics = findViewById(R.id.cardProfileAnalytics);
        View cardProfileNotifications = findViewById(R.id.cardProfileNotifications);
        View cardProfileAbout = findViewById(R.id.cardProfileAbout);
        View cardChangePassword = findViewById(R.id.cardChangePassword);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, SettingsActivity.class)));
        }

        if (currentUser != null) {
            if (tvName != null) tvName.setText(currentUser.getName() != null ? currentUser.getName() : "User");
            if (tvEmail != null) tvEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "N/A");

            String role = currentUser.getRole() != null ? currentUser.getRole().toUpperCase() : "STUDENT";

            String name = currentUser.getName();
            if (name != null && name.length() >= 2 && tvAvatar != null) {
                tvAvatar.setText(name.substring(0, 2).toUpperCase());
            }

            if (User.ROLE_ADMIN.equalsIgnoreCase(role)) {
                if (tvRoleBadge != null) tvRoleBadge.setText("SYSTEM ADMINISTRATOR");
                if (tvStatusSub != null) tvStatusSub.setText("System Administrator • Master Authority");
                if (tvRollLabel != null) tvRollLabel.setText("Admin ID");
                if (tvRoll != null) tvRoll.setText(currentUser.getEmployeeId() != null ? currentUser.getEmployeeId() : "ADM-001");
                if (tvDept != null) tvDept.setText(currentUser.getDepartment() != null ? currentUser.getDepartment() : "Administration / IT");
                if (tvAccessLevel != null) tvAccessLevel.setText("Superuser Master Authority");
            } else if (User.ROLE_TEACHER.equalsIgnoreCase(role)) {
                if (tvRoleBadge != null) tvRoleBadge.setText("FACULTY TEACHER");
                if (tvStatusSub != null) tvStatusSub.setText("Faculty Educator • RollCall Teacher");
                if (tvRollLabel != null) tvRollLabel.setText("Employee ID");
                if (tvRoll != null) tvRoll.setText(currentUser.getEmployeeId() != null ? currentUser.getEmployeeId() : "EMP-101");
                if (tvDept != null) tvDept.setText(currentUser.getDepartment() != null ? currentUser.getDepartment() : "Computer Science");
                if (tvAccessLevel != null) tvAccessLevel.setText("Teacher Access");
            } else {
                if (tvRoleBadge != null) tvRoleBadge.setText("ENROLLED STUDENT");
                if (tvStatusSub != null) tvStatusSub.setText("Enrolled Student • RollCall Academic");
                if (tvRollLabel != null) tvRollLabel.setText("Roll Number");
                if (tvRoll != null) tvRoll.setText(currentUser.getRollNumber() != null ? currentUser.getRollNumber() : "RC2024001");
                if (tvDept != null) tvDept.setText(currentUser.getCourse() != null ? currentUser.getCourse() : "Computer Science");
                if (tvAccessLevel != null) tvAccessLevel.setText("Student Access");
            }
        }

        // Connect quick feature cards
        if (cardProfileSettings != null) {
            cardProfileSettings.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, SettingsActivity.class)));
        }

        if (cardProfileAnalytics != null) {
            cardProfileAnalytics.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, AnalyticsActivity.class)));
        }

        if (cardProfileNotifications != null) {
            cardProfileNotifications.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, NotificationActivity.class)));
        }

        if (cardProfileAbout != null) {
            cardProfileAbout.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, AboutActivity.class)));
        }

        if (cardChangePassword != null) {
            cardChangePassword.setOnClickListener(v -> {
                if (currentUser != null && currentUser.getEmail() != null) {
                    authRepo.sendPasswordResetEmail(currentUser.getEmail(), new AuthRepository.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(ProfileActivity.this, "Password reset link sent to " + currentUser.getEmail(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(ProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Password reset email triggered", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out of RollCall?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    authRepo.logout();
                    com.example.rollcall.data.PhoneAuthRepository.getInstance(this).logout();
                    new com.example.rollcall.utils.PreferenceManager(this).logoutUser();
                    Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
