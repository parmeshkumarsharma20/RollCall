package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.rollcall.model.User;
import com.example.rollcall.utils.PreferenceManager;
import com.example.rollcall.utils.WindowInsetUtils;

public class RoleSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_ROLE = "extra_selected_role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        CardView cardStudent = findViewById(R.id.cardRoleStudent);
        CardView cardTeacher = findViewById(R.id.cardRoleTeacher);
        CardView cardAdmin = findViewById(R.id.cardRoleAdmin);

        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        if (slideUp != null) {
            if (cardStudent != null) cardStudent.startAnimation(slideUp);
            if (cardTeacher != null) cardTeacher.startAnimation(slideUp);
            if (cardAdmin != null) cardAdmin.startAnimation(slideUp);
        }

        if (cardStudent != null) {
            cardStudent.setOnClickListener(v -> animateAndSelectRole(v, User.ROLE_STUDENT));
        }
        if (cardTeacher != null) {
            cardTeacher.setOnClickListener(v -> animateAndSelectRole(v, User.ROLE_TEACHER));
        }
        if (cardAdmin != null) {
            cardAdmin.setOnClickListener(v -> animateAndSelectRole(v, User.ROLE_ADMIN));
        }
    }

    private void animateAndSelectRole(View view, String role) {
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        if (scaleUp != null && view != null) {
            view.startAnimation(scaleUp);
        }
        selectRole(role);
    }

    private void selectRole(String role) {
        PreferenceManager prefManager = new PreferenceManager(this);
        prefManager.setSelectedRole(role);

        Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
        intent.putExtra(EXTRA_SELECTED_ROLE, role);
        startActivity(intent);
    }
}
