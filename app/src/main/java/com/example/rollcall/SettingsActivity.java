package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.utils.ThemeManager;
import com.example.rollcall.utils.WindowInsetUtils;

public class SettingsActivity extends AppCompatActivity {

    private AuthRepository authRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        authRepo = AuthRepository.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btnBackSettings);
        RadioGroup rgTheme = findViewById(R.id.rgTheme);
        RadioButton rbSystem = findViewById(R.id.rbThemeSystem);
        RadioButton rbLight = findViewById(R.id.rbThemeLight);
        RadioButton rbDark = findViewById(R.id.rbThemeDark);

        CardView cardProfile = findViewById(R.id.cardSettingsProfile);
        CardView cardNotifications = findViewById(R.id.cardSettingsNotifications);
        CardView cardAbout = findViewById(R.id.cardAbout);
        Button btnLogout = findViewById(R.id.btnLogoutSettings);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        int currentMode = ThemeManager.getThemeMode(this);
        if (currentMode == ThemeManager.THEME_LIGHT && rbLight != null) {
            rbLight.setChecked(true);
        } else if (currentMode == ThemeManager.THEME_DARK && rbDark != null) {
            rbDark.setChecked(true);
        } else if (rbSystem != null) {
            rbSystem.setChecked(true);
        }

        if (rgTheme != null) {
            rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbThemeLight) {
                    ThemeManager.setThemeMode(SettingsActivity.this, ThemeManager.THEME_LIGHT);
                } else if (checkedId == R.id.rbThemeDark) {
                    ThemeManager.setThemeMode(SettingsActivity.this, ThemeManager.THEME_DARK);
                } else {
                    ThemeManager.setThemeMode(SettingsActivity.this, ThemeManager.THEME_SYSTEM);
                }
            });
        }

        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        if (cardNotifications != null) {
            cardNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }

        if (cardAbout != null) {
            cardAbout.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intent);
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
                    Toast.makeText(SettingsActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
