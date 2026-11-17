package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.rollcall.data.AdminRepository;
import com.example.rollcall.data.PhoneAuthRepository;
import com.example.rollcall.model.Course;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class PhoneProfileSetupActivity extends AppCompatActivity {

    private String rawPhone;
    private String selectedRole;

    private EditText etPhoneVerifiedNumber, etName, etRoll, etSection;
    private Spinner spnCourse, spnSemester;
    private View layoutCourseSelector, layoutSemSecRow;
    private Button btnSave;

    private PhoneAuthRepository phoneAuthRepo;
    private AdminRepository adminRepo;

    private List<String> courseNames = new ArrayList<>();
    private List<String> semesterNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_profile_setup);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        phoneAuthRepo = PhoneAuthRepository.getInstance(this);
        adminRepo = AdminRepository.getInstance(this);

        rawPhone = getIntent().getStringExtra("extra_phone");
        selectedRole = getIntent().getStringExtra("extra_role");
        if (selectedRole == null) selectedRole = User.ROLE_STUDENT;

        etPhoneVerifiedNumber = findViewById(R.id.etPhoneVerifiedNumber);
        etName = findViewById(R.id.etPhoneName);
        etRoll = findViewById(R.id.etPhoneRoll);
        etSection = findViewById(R.id.etPhoneSection);

        spnCourse = findViewById(R.id.spnPhoneCourse);
        spnSemester = findViewById(R.id.spnPhoneSemester);

        layoutCourseSelector = findViewById(R.id.layoutPhoneCourseSelector);
        layoutSemSecRow = findViewById(R.id.layoutPhoneSemSecRow);

        btnSave = findViewById(R.id.btnSavePhoneProfile);

        CardView cardPhoneProfile = findViewById(R.id.cardPhoneProfile);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        if (cardPhoneProfile != null && slideUp != null) {
            cardPhoneProfile.startAnimation(slideUp);
        }

        if (rawPhone != null && etPhoneVerifiedNumber != null) {
            etPhoneVerifiedNumber.setText(PhoneAuthRepository.normalizePhoneNumber(rawPhone));
        }

        setupSemestersSpinner();
        loadCoursesFromBackend();

        boolean isTeacher = User.ROLE_TEACHER.equalsIgnoreCase(selectedRole);
        if (isTeacher) {
            TextView tvSub = findViewById(R.id.tvPhoneProfileSub);
            if (tvSub != null) tvSub.setText("Set up your verified faculty phone account");
            if (layoutCourseSelector != null) layoutCourseSelector.setVisibility(View.GONE);
            if (layoutSemSecRow != null) layoutSemSecRow.setVisibility(View.GONE);
            if (etRoll != null) etRoll.setHint("Employee ID *");
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfileAndLaunchDashboard());
        }
    }

    private void setupSemestersSpinner() {
        semesterNames.clear();
        semesterNames.add("1st");
        semesterNames.add("2nd");
        semesterNames.add("3rd");
        semesterNames.add("4th");
        semesterNames.add("5th");
        semesterNames.add("6th");
        semesterNames.add("7th");
        semesterNames.add("8th");

        ArrayAdapter<String> semAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesterNames);
        semAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spnSemester != null) spnSemester.setAdapter(semAdapter);
    }

    private void loadCoursesFromBackend() {
        courseNames.clear();
        adminRepo.fetchCourses(new AdminRepository.ListCallback<Course>() {
            @Override
            public void onSuccess(List<Course> items) {
                for (Course c : items) {
                    if (c.getCourseName() != null) courseNames.add(c.getCourseName());
                }
                if (courseNames.isEmpty()) {
                    courseNames.add("B.Tech CSE");
                    courseNames.add("BCA");
                    courseNames.add("B.Com");
                    courseNames.add("MCA");
                }
                populateCourseSpinner();
            }

            @Override
            public void onError(String message) {
                courseNames.add("B.Tech CSE");
                courseNames.add("BCA");
                courseNames.add("B.Com");
                courseNames.add("MCA");
                populateCourseSpinner();
            }
        });
    }

    private void populateCourseSpinner() {
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseNames);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spnCourse != null) spnCourse.setAdapter(courseAdapter);
    }

    private void saveProfileAndLaunchDashboard() {
        String name = etName.getText().toString().trim();
        String roll = etRoll.getText().toString().trim();
        String sec = etSection.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Full name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(roll)) {
            etRoll.setError("Enrollment / Roll / Employee ID is required");
            etRoll.requestFocus();
            return;
        }

        String phone = PhoneAuthRepository.normalizePhoneNumber(rawPhone);
        String uid = "ph_u_" + System.currentTimeMillis();

        User newUser = new User(uid, name, phone + "@rollcall.phone", selectedRole);
        newUser.setPhoneNumber(phone);
        newUser.setAccountStatus(User.STATUS_ACTIVE);
        newUser.setActiveStatus(true);

        boolean isTeacher = User.ROLE_TEACHER.equalsIgnoreCase(selectedRole);

        if (isTeacher) {
            newUser.setEmployeeId(roll);
            newUser.setDepartment("Computer Science");
        } else {
            newUser.setRollNumber(roll);
            newUser.setSection(!TextUtils.isEmpty(sec) ? sec : "A");
            if (spnCourse != null && spnCourse.getSelectedItem() != null) {
                newUser.setCourse(spnCourse.getSelectedItem().toString());
                newUser.setDepartment(spnCourse.getSelectedItem().toString());
            } else {
                newUser.setCourse("B.Tech CSE");
                newUser.setDepartment("Computer Science");
            }
            if (spnSemester != null && spnSemester.getSelectedItem() != null) {
                newUser.setSemester(spnSemester.getSelectedItem().toString());
            } else {
                newUser.setSemester("1st");
            }
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving Profile...");

        phoneAuthRepo.savePhoneProfile(newUser, new PhoneAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(PhoneProfileSetupActivity.this, "Profile Saved! Welcome, " + user.getName(), Toast.LENGTH_SHORT).show();
                Intent intent;
                if (User.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
                    intent = new Intent(PhoneProfileSetupActivity.this, TeacherDashboardActivity.class);
                } else {
                    intent = new Intent(PhoneProfileSetupActivity.this, StudentDashboardActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                btnSave.setEnabled(true);
                btnSave.setText("SAVE PROFILE & OPEN DASHBOARD 🚀");
                Toast.makeText(PhoneProfileSetupActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
