package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.model.User;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AttendanceRepository repository = AttendanceRepository.getInstance(this);
        User user = repository.getCurrentUser();

        Intent intent;
        if (user != null) {
            if (User.ROLE_ADMIN.equals(user.getRole())) {
                intent = new Intent(this, AdminActivity.class);
            } else if (User.ROLE_TEACHER.equals(user.getRole())) {
                intent = new Intent(this, TeacherActivity.class);
            } else {
                intent = new Intent(this, StudentActivity.class);
            }
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}