package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

public class PendingApprovalActivity extends AppCompatActivity {

    private AuthRepository authRepo;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approval);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        authRepo = AuthRepository.getInstance(this);
        currentUser = authRepo.getCurrentUser();

        TextView tvName = findViewById(R.id.tvPendingName);
        TextView tvEmailRole = findViewById(R.id.tvPendingEmailRole);
        Button btnRefresh = findViewById(R.id.btnRefreshStatus);
        Button btnLogout = findViewById(R.id.btnLogoutPending);

        if (currentUser != null) {
            if (tvName != null) tvName.setText(currentUser.getName() != null ? currentUser.getName() : "User Account");
            if (tvEmailRole != null) {
                String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";
                String role = currentUser.getRole() != null ? currentUser.getRole() : "Student";
                tvEmailRole.setText(email + " • " + role);
            }
        }

        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> checkApprovalStatus());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                authRepo.logout();
                Intent intent = new Intent(PendingApprovalActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void checkApprovalStatus() {
        if (currentUser == null || currentUser.getUid() == null) {
            Toast.makeText(this, "Session invalid. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Checking Master Admin approval...", Toast.LENGTH_SHORT).show();

        authRepo.fetchUserProfile(currentUser.getUid(), currentUser.getRole(), new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                if (User.STATUS_ACTIVE.equalsIgnoreCase(user.getAccountStatus()) || user.isActiveStatus()) {
                    Toast.makeText(PendingApprovalActivity.this, "Account Approved! Welcome, " + user.getName(), Toast.LENGTH_SHORT).show();
                    Intent intent;
                    if (User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
                        intent = new Intent(PendingApprovalActivity.this, AdminDashboardActivity.class);
                    } else if (User.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
                        intent = new Intent(PendingApprovalActivity.this, TeacherDashboardActivity.class);
                    } else {
                        intent = new Intent(PendingApprovalActivity.this, StudentDashboardActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(PendingApprovalActivity.this, "Status: Pending Master Admin Review", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(PendingApprovalActivity.this, "Status: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
