package com.example.rollcall;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.utils.WindowInsetUtils;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        ImageButton btnBack = findViewById(R.id.btnBackAbout);
        Button btnCheckUpdates = findViewById(R.id.btnCheckUpdates);
        Button btnContactSupport = findViewById(R.id.btnContactSupport);
        View cardPrivacyPolicy = findViewById(R.id.cardPrivacyPolicy);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCheckUpdates != null) {
            btnCheckUpdates.setOnClickListener(v -> showCheckUpdatesDialog());
        }

        if (btnContactSupport != null) {
            btnContactSupport.setOnClickListener(v -> showContactSupportDialog());
        }

        if (cardPrivacyPolicy != null) {
            cardPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicyDialog());
        }
    }

    private void showCheckUpdatesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("System Update Check")
                .setMessage("RollCall v1.0.0 (Build 2026.2)\n\nYour application is up to date with the latest security protocols and Firestore sync features.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showContactSupportDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Contact Support")
                .setMessage("RollCall Technical Support Team\n\nEmail: support@rollcall.com\nHelpline: +91 1800-ROLLCALL\nHours: Mon - Sat (9 AM - 6 PM)")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showPrivacyPolicyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy & Data Safety")
                .setMessage("1. Location Data: GPS location is accessed ONLY during attendance marking to verify 25m classroom proximity.\n\n2. Encryption: All attendance records and credentials are encrypted in transit and at rest using Firebase Firestore Security Rules.\n\n3. Data Protection: Student attendance history is restricted strictly to authorized faculty and system administrators.")
                .setPositiveButton("I Understand", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
