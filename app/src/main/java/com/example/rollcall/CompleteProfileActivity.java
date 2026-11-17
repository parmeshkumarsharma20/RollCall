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
import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.Course;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CompleteProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER = "extra_user_to_complete";

    private EditText etName, etEmail, etRoll, etSection, etDepartment;
    private Spinner spnCourse, spnSemester;
    private View layoutCourseSelector, layoutSemSecRow, tilSetupDepartment;
    private Button btnSave;

    private AuthRepository authRepo;
    private AdminRepository adminRepo;
    private User user;

    private List<String> courseNames = new ArrayList<>();
    private List<String> semesterNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        authRepo = AuthRepository.getInstance(this);
        adminRepo = AdminRepository.getInstance(this);

        user = (User) getIntent().getSerializableExtra(EXTRA_USER);
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null && fUser != null) {
            String role = User.ROLE_STUDENT;
            String name = fUser.getDisplayName() != null ? fUser.getDisplayName() : "";
            String email = fUser.getEmail() != null ? fUser.getEmail() : "";
            user = new User(fUser.getUid(), name, email, role);
        } else if (user == null) {
            user = authRepo.getCurrentUser();
        }

        etName = findViewById(R.id.etSetupName);
        etEmail = findViewById(R.id.etSetupEmail);
        etRoll = findViewById(R.id.etSetupRoll);
        etSection = findViewById(R.id.etSetupSection);
        etDepartment = findViewById(R.id.etSetupDepartment);

        spnCourse = findViewById(R.id.spnSetupCourse);
        spnSemester = findViewById(R.id.spnSetupSemester);

        layoutCourseSelector = findViewById(R.id.layoutCourseSelector);
        layoutSemSecRow = findViewById(R.id.layoutSemSecRow);
        tilSetupDepartment = findViewById(R.id.tilSetupDepartment);

        btnSave = findViewById(R.id.btnSaveProfileSetup);

        CardView cardCompleteProfile = findViewById(R.id.cardCompleteProfile);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        if (cardCompleteProfile != null && slideUp != null) {
            cardCompleteProfile.startAnimation(slideUp);
        }

        setupSemestersSpinner();
        loadCoursesFromBackend();

        if (user != null) {
            TextView tvTitle = findViewById(R.id.tvProfilePromptTitle);
            TextView tvSub = findViewById(R.id.tvProfilePromptSub);

            boolean isTeacher = User.ROLE_TEACHER.equalsIgnoreCase(user.getRole());
            if (isTeacher) {
                if (tvTitle != null) tvTitle.setText("Complete Faculty Profile");
                if (tvSub != null) tvSub.setText("Set up your employee ID & department details");
                if (layoutCourseSelector != null) layoutCourseSelector.setVisibility(View.GONE);
                if (layoutSemSecRow != null) layoutSemSecRow.setVisibility(View.GONE);
                if (tilSetupDepartment != null) tilSetupDepartment.setVisibility(View.VISIBLE);
                if (etRoll != null) etRoll.setHint("Employee ID *");
            } else {
                if (tvTitle != null) tvTitle.setText("Complete Student Profile");
                if (tvSub != null) tvSub.setText("Configure your enrolled degree program & semester details");
            }

            if (etName != null) {
                String existingName = user.getName();
                if (existingName != null && !existingName.isEmpty() && !"Google User".equalsIgnoreCase(existingName) && !"User".equalsIgnoreCase(existingName)) {
                    etName.setText(existingName);
                } else {
                    etName.setText("");
                }
            }
            if (etEmail != null) {
                String emailToSet = !TextUtils.isEmpty(user.getEmail()) ? user.getEmail() : (fUser != null && !TextUtils.isEmpty(fUser.getEmail()) ? fUser.getEmail() : "");
                if (!TextUtils.isEmpty(emailToSet)) {
                    etEmail.setText(emailToSet);
                    etEmail.setEnabled(false);
                } else {
                    etEmail.setText("");
                    etEmail.setEnabled(true);
                }
            }
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
        String emailEntered = etEmail.getText().toString().trim();
        String roll = etRoll.getText().toString().trim();
        String sec = etSection.getText().toString().trim();
        String dept = etDepartment.getText().toString().trim();

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

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            String uid = fUser != null ? fUser.getUid() : "stud_" + System.currentTimeMillis();
            String userEmail = !TextUtils.isEmpty(emailEntered) ? emailEntered : (fUser != null && fUser.getEmail() != null ? fUser.getEmail() : "");
            user = new User(uid, name, userEmail, User.ROLE_STUDENT);
        }

        if (fUser != null && (user.getUid() == null || user.getUid().isEmpty())) {
            user.setUid(fUser.getUid());
        }

        user.setName(name);
        if (!TextUtils.isEmpty(emailEntered)) {
            user.setEmail(emailEntered);
        }

        boolean isTeacher = User.ROLE_TEACHER.equalsIgnoreCase(user.getRole());

        if (isTeacher) {
            user.setEmployeeId(roll);
            user.setDepartment(!TextUtils.isEmpty(dept) ? dept : "Computer Science");
        } else {
            user.setRollNumber(roll);
            user.setSection(!TextUtils.isEmpty(sec) ? sec : "A");
            if (spnCourse != null && spnCourse.getSelectedItem() != null) {
                user.setCourse(spnCourse.getSelectedItem().toString());
                user.setDepartment(spnCourse.getSelectedItem().toString());
            } else {
                user.setCourse("B.Tech CSE");
                user.setDepartment("Computer Science");
            }
            if (spnSemester != null && spnSemester.getSelectedItem() != null) {
                user.setSemester(spnSemester.getSelectedItem().toString());
            } else {
                user.setSemester("6th");
            }
        }

        user.setAccountStatus(User.STATUS_ACTIVE);
        user.setActiveStatus(true);

        btnSave.setEnabled(false);
        btnSave.setText("Saving Profile to Firebase...");

        String docId = user.getUid() != null ? user.getUid() : (fUser != null ? fUser.getUid() : "u_" + System.currentTimeMillis());
        user.setUid(docId);

        FirebaseFirestore.getInstance().collection("users").document(docId).set(user.toMap())
                .addOnSuccessListener(aVoid -> completeSaveAndNavigate())
                .addOnFailureListener(e -> completeSaveAndNavigate());
    }

    private void completeSaveAndNavigate() {
        authRepo.setCurrentUser(user);
        AttendanceRepository.getInstance(this).registerUser(user);
        AttendanceRepository.getInstance(this).setCurrentUser(user);

        Toast.makeText(this, "Profile Saved to Firebase! Welcome, " + user.getName(), Toast.LENGTH_SHORT).show();

        Intent intent;
        if (User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            intent = new Intent(CompleteProfileActivity.this, AdminDashboardActivity.class);
        } else if (User.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
            intent = new Intent(CompleteProfileActivity.this, TeacherDashboardActivity.class);
        } else {
            intent = new Intent(CompleteProfileActivity.this, StudentDashboardActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
