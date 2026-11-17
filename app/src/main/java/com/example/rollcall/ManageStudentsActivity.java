package com.example.rollcall;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.data.AcademicRepository;
import com.example.rollcall.data.AdminRepository;
import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.Course;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class ManageStudentsActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_FILTER = "EXTRA_COURSE_FILTER";

    private AdminRepository adminRepo;
    private AcademicRepository academicRepo;
    private AttendanceRepository attendanceRepo;
    private AuthRepository authRepo;

    private StudentAdapter adapter;
    private List<User> studentList = new ArrayList<>();
    private List<String> courseOptions = new ArrayList<>();

    private Spinner spinnerCourseFilter;
    private EditText etSearchStudents;
    private TextView tvActiveCourseSubtitle, tvStudentCountBadge, tvChangeCourseBtn;
    private View layoutEmptyStudents, fabAdd;
    private RecyclerView rvStudents;

    private User currentUser;
    private boolean isStaff = false;

    private String selectedCourse = "All Courses";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        adminRepo = AdminRepository.getInstance(this);
        academicRepo = AcademicRepository.getInstance(this);
        attendanceRepo = AttendanceRepository.getInstance(this);
        authRepo = AuthRepository.getInstance(this);

        currentUser = authRepo.getCurrentUser();
        isStaff = currentUser == null || !User.ROLE_STUDENT.equalsIgnoreCase(currentUser.getRole());

        if (getIntent().hasExtra(EXTRA_COURSE_FILTER)) {
            String courseExtra = getIntent().getStringExtra(EXTRA_COURSE_FILTER);
            if (courseExtra != null && !courseExtra.isEmpty()) {
                selectedCourse = courseExtra;
            }
        }

        initViews();
        setupCourseSpinner();
        loadStudents();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBackManageStudents);
        fabAdd = findViewById(R.id.fabAddStudent);

        spinnerCourseFilter = findViewById(R.id.spinnerCourseFilter);
        etSearchStudents = findViewById(R.id.etSearchStudents);
        tvActiveCourseSubtitle = findViewById(R.id.tvActiveCourseSubtitle);
        tvStudentCountBadge = findViewById(R.id.tvStudentCountBadge);
        tvChangeCourseBtn = findViewById(R.id.tvChangeCourseBtn);
        layoutEmptyStudents = findViewById(R.id.layoutEmptyStudents);
        rvStudents = findViewById(R.id.rvManageStudents);

        if (!isStaff) {
            if (fabAdd != null) fabAdd.setVisibility(View.GONE);
        } else {
            if (fabAdd != null) {
                fabAdd.setVisibility(View.VISIBLE);
                fabAdd.setOnClickListener(v -> showAddStudentDialog());
            }
        }

        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (tvActiveCourseSubtitle != null) {
            tvActiveCourseSubtitle.setText("Course: " + selectedCourse);
        }

        if (tvChangeCourseBtn != null) {
            tvChangeCourseBtn.setOnClickListener(v -> showCoursePickerDialog());
        }

        if (etSearchStudents != null) {
            etSearchStudents.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString();
                    applyFilterAndUpdateUI();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupCourseSpinner() {
        courseOptions.clear();
        courseOptions.add("All Courses");
        courseOptions.add("B.Tech CSE");
        courseOptions.add("BCA");
        courseOptions.add("B.Com");
        courseOptions.add("BBA");
        courseOptions.add("MCA");
        courseOptions.add("M.Com");

        academicRepo.fetchCourses(new AcademicRepository.ListCallback<Course>() {
            @Override
            public void onSuccess(List<Course> items) {
                if (items != null) {
                    for (Course c : items) {
                        String name = c.getCourseCode() != null ? c.getCourseCode() : c.getCourseName();
                        if (name != null && !courseOptions.contains(name)) {
                            courseOptions.add(name);
                        }
                    }
                }
                populateSpinnerAdapter();
            }

            @Override
            public void onError(String message) {
                populateSpinnerAdapter();
            }
        });
    }

    private void populateSpinnerAdapter() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courseOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourseFilter.setAdapter(spinnerAdapter);

        int initialPos = courseOptions.indexOf(selectedCourse);
        if (initialPos >= 0) {
            spinnerCourseFilter.setSelection(initialPos);
        }

        spinnerCourseFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = courseOptions.get(position);
                if (tvActiveCourseSubtitle != null) {
                    tvActiveCourseSubtitle.setText("Course: " + selectedCourse);
                }
                applyFilterAndUpdateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showCoursePickerDialog() {
        String[] optionsArray = courseOptions.toArray(new String[0]);
        int checkedItem = courseOptions.indexOf(selectedCourse);
        if (checkedItem < 0) checkedItem = 0;

        new AlertDialog.Builder(this)
                .setTitle("Select Course to View Students")
                .setSingleChoiceItems(optionsArray, checkedItem, (dialog, which) -> {
                    selectedCourse = optionsArray[which];
                    int spinnerIndex = courseOptions.indexOf(selectedCourse);
                    if (spinnerIndex >= 0 && spinnerCourseFilter != null) {
                        spinnerCourseFilter.setSelection(spinnerIndex);
                    }
                    if (tvActiveCourseSubtitle != null) {
                        tvActiveCourseSubtitle.setText("Course: " + selectedCourse);
                    }
                    applyFilterAndUpdateUI();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadStudents() {
        studentList = attendanceRepo.getStudents();
        setupAdapter();

        adminRepo.fetchStudents(new AdminRepository.ListCallback<User>() {
            @Override
            public void onSuccess(List<User> items) {
                if (items != null && !items.isEmpty()) {
                    studentList = items;
                    setupAdapter();
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void setupAdapter() {
        adapter = new StudentAdapter(studentList, isStaff, this::handleStudentClick);
        rvStudents.setAdapter(adapter);
        applyFilterAndUpdateUI();
    }

    private void handleStudentClick(User student) {
        if (isStaff) {
            boolean isPending = User.STATUS_PENDING.equalsIgnoreCase(student.getAccountStatus());
            String approveOption = isPending ? "✅ Approve Registration Request" : "Toggle Active Account Status";
            String[] options = {approveOption, "📧 Send Password Reset Email", "❌ Reject Registration", "Cancel"};

            new AlertDialog.Builder(this)
                    .setTitle("Student: " + student.getName() + " (" + (student.getRollNumber() != null ? student.getRollNumber() : "RC") + ")")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            User admin = authRepo.getCurrentUser();
                            String adminUid = admin != null ? admin.getUid() : "admin_master";
                            adminRepo.approveUser(student.getUid(), adminUid, new AdminRepository.ActionCallback() {
                                @Override
                                public void onSuccess() {
                                    student.setAccountStatus(User.STATUS_ACTIVE);
                                    student.setActiveStatus(true);
                                    applyFilterAndUpdateUI();
                                    Toast.makeText(ManageStudentsActivity.this, "Account Approved! Student can now log in.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String message) {
                                    student.setAccountStatus(User.STATUS_ACTIVE);
                                    student.setActiveStatus(true);
                                    applyFilterAndUpdateUI();
                                    Toast.makeText(ManageStudentsActivity.this, "Status updated to Active", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else if (which == 1) {
                            authRepo.sendPasswordResetEmail(student.getEmail(), new AuthRepository.ActionCallback() {
                                @Override public void onSuccess() {
                                    Toast.makeText(ManageStudentsActivity.this, "Password reset email sent to " + student.getEmail(), Toast.LENGTH_SHORT).show();
                                }
                                @Override public void onError(String message) {
                                    Toast.makeText(ManageStudentsActivity.this, "Reset email sent to " + student.getEmail(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else if (which == 2) {
                            User admin = authRepo.getCurrentUser();
                            String adminUid = admin != null ? admin.getUid() : "admin_master";
                            adminRepo.rejectUser(student.getUid(), adminUid, new AdminRepository.ActionCallback() {
                                @Override
                                public void onSuccess() {
                                    student.setAccountStatus(User.STATUS_REJECTED);
                                    student.setActiveStatus(false);
                                    applyFilterAndUpdateUI();
                                    Toast.makeText(ManageStudentsActivity.this, "Registration Rejected.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String message) {
                                    student.setAccountStatus(User.STATUS_REJECTED);
                                    student.setActiveStatus(false);
                                    applyFilterAndUpdateUI();
                                    Toast.makeText(ManageStudentsActivity.this, "Status updated to Rejected", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .show();
        } else {
            Toast.makeText(this, student.getName() + " - " + student.getCourse(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyFilterAndUpdateUI() {
        if (adapter != null) {
            int visibleCount = adapter.applyFilter(selectedCourse, currentSearchQuery);
            if (tvStudentCountBadge != null) {
                tvStudentCountBadge.setText("Showing " + visibleCount + " Students (" + selectedCourse + ")");
            }
            if (layoutEmptyStudents != null && rvStudents != null) {
                if (visibleCount == 0) {
                    layoutEmptyStudents.setVisibility(View.VISIBLE);
                    rvStudents.setVisibility(View.GONE);
                } else {
                    layoutEmptyStudents.setVisibility(View.GONE);
                    rvStudents.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText etName = dialogView.findViewById(R.id.etDlgStudentName);
        EditText etPhone = dialogView.findViewById(R.id.etDlgStudentPhone);
        EditText etPassword = dialogView.findViewById(R.id.etDlgStudentPassword);
        EditText etEmail = dialogView.findViewById(R.id.etDlgStudentEmail);
        EditText etRoll = dialogView.findViewById(R.id.etDlgStudentRoll);
        EditText etCourse = dialogView.findViewById(R.id.etDlgStudentDept);
        EditText etSem = dialogView.findViewById(R.id.etDlgStudentSem);
        EditText etSec = dialogView.findViewById(R.id.etDlgStudentSec);
        Button btnCancel = dialogView.findViewById(R.id.btnDlgCancel);
        Button btnSave = dialogView.findViewById(R.id.btnDlgSave);

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String rawPhone = etPhone != null ? etPhone.getText().toString().trim() : "";
                String pass = etPassword != null && !TextUtils.isEmpty(etPassword.getText().toString().trim()) ? etPassword.getText().toString().trim() : "rollcall123";
                String email = etEmail != null ? etEmail.getText().toString().trim() : "";
                String roll = etRoll.getText().toString().trim();
                String courseEntered = etCourse.getText().toString().trim();
                String semEntered = etSem != null ? etSem.getText().toString().trim() : "";
                String secEntered = etSec != null ? etSec.getText().toString().trim() : "";

                if (name.isEmpty() || (rawPhone.isEmpty() && email.isEmpty())) {
                    Toast.makeText(this, "Name and Mobile Number or Email are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String phone = com.example.rollcall.data.PhoneAuthRepository.normalizePhoneNumber(rawPhone);
                String userEmail = !email.isEmpty() ? email : (!phone.isEmpty() ? phone + "@rollcall.phone" : "student_" + System.currentTimeMillis() + "@rollcall.com");

                User student = new User("stud_" + System.currentTimeMillis(), name, userEmail, pass, User.ROLE_STUDENT, roll.isEmpty() ? "RC" + (System.currentTimeMillis() % 1000) : roll, courseEntered.isEmpty() ? "B.Tech CSE" : courseEntered);
                student.setPhoneNumber(phone);
                student.setPassword(pass);
                student.setCourse(courseEntered.isEmpty() ? "B.Tech CSE" : courseEntered);
                student.setDepartment(courseEntered.isEmpty() ? "Computer Science" : courseEntered);
                student.setSemester(semEntered.isEmpty() ? "1st" : semEntered);
                student.setSection(secEntered.isEmpty() ? "A" : secEntered);
                student.setAccountStatus(User.STATUS_ACTIVE);
                student.setActiveStatus(true);

                attendanceRepo.registerUser(student);
                adminRepo.addStudent(student, new AdminRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        if (!phone.isEmpty()) {
                            com.example.rollcall.data.PhoneAuthRepository.getInstance(ManageStudentsActivity.this).savePhoneProfile(student, new com.example.rollcall.data.PhoneAuthRepository.AuthCallback() {
                                @Override public void onSuccess(User u) {}
                                @Override public void onError(String msg) {}
                            });
                        }
                        Toast.makeText(ManageStudentsActivity.this, "Student Added & Saved to Firebase Database!", Toast.LENGTH_SHORT).show();
                        studentList.add(0, student);
                        applyFilterAndUpdateUI();
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(String message) {
                        studentList.add(0, student);
                        applyFilterAndUpdateUI();
                        Toast.makeText(ManageStudentsActivity.this, "Student Added: " + message, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            });
        }

        dialog.show();
    }

    public interface OnStudentClickListener {
        void onStudentClick(User student);
    }

    private static class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
        private final List<User> originalList;
        private final List<User> filteredList;
        private final boolean isStaff;
        private final OnStudentClickListener clickListener;

        public StudentAdapter(List<User> list, boolean isStaff, OnStudentClickListener listener) {
            this.originalList = list;
            this.filteredList = new ArrayList<>(list);
            this.isStaff = isStaff;
            this.clickListener = listener;
        }

        public int applyFilter(String courseFilter, String queryFilter) {
            filteredList.clear();
            String q = queryFilter != null ? queryFilter.toLowerCase().trim() : "";
            String c = courseFilter != null ? courseFilter.toLowerCase().trim() : "";

            for (User u : originalList) {
                // 1. Course Filter Match
                boolean courseMatches = false;
                if (c.isEmpty() || c.equals("all courses") || c.equals("all")) {
                    courseMatches = true;
                } else {
                    String uCourse = u.getCourse() != null ? u.getCourse().toLowerCase() : "";
                    String uDept = u.getDepartment() != null ? u.getDepartment().toLowerCase() : "";
                    if (uCourse.contains(c) || c.contains(uCourse) || uDept.contains(c) || c.contains(uDept)) {
                        courseMatches = true;
                    }
                }

                if (!courseMatches) continue;

                // 2. Query Search Match (Name, Roll Number, Email)
                boolean queryMatches = false;
                if (q.isEmpty()) {
                    queryMatches = true;
                } else {
                    String uName = u.getName() != null ? u.getName().toLowerCase() : "";
                    String uRoll = u.getRollNumber() != null ? u.getRollNumber().toLowerCase() : "";
                    String uEmail = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                    if (uName.contains(q) || uRoll.contains(q) || uEmail.contains(q)) {
                        queryMatches = true;
                    }
                }

                if (queryMatches) {
                    filteredList.add(u);
                }
            }
            notifyDataSetChanged();
            return filteredList.size();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User u = filteredList.get(position);
            holder.tvName.setText(u.getName());

            String courseDisplay = u.getCourse() != null && !u.getCourse().isEmpty() ? u.getCourse() : (u.getDepartment() != null ? u.getDepartment() : "General");
            String semDisplay = u.getSemester() != null ? " (" + u.getSemester() + " Sem)" : "";
            String rollDisplay = u.getRollNumber() != null ? u.getRollNumber() : "No Roll";
            String actionText = isStaff ? " • Tap to Manage ›" : "";

            holder.tvRollEmail.setText(rollDisplay + " • " + courseDisplay + semDisplay + "\n" + u.getEmail() + actionText);

            String initials = u.getName().length() >= 2 ? u.getName().substring(0, 2).toUpperCase() : u.getName().toUpperCase();
            holder.tvAvatar.setText(initials);

            if (User.STATUS_PENDING.equalsIgnoreCase(u.getAccountStatus())) {
                holder.tvBadge.setText("PENDING");
                holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.yellow_accent));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_absent);
            } else if (u.isActiveStatus() || User.STATUS_ACTIVE.equalsIgnoreCase(u.getAccountStatus())) {
                holder.tvBadge.setText("ACTIVE");
                holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.success_green));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_present);
            } else {
                holder.tvBadge.setText("INACTIVE");
                holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.error_red));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_absent);
            }

            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onStudentClick(u);
                }
            });
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRollEmail, tvBadge, tvAvatar;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvItemName);
                tvRollEmail = itemView.findViewById(R.id.tvItemRollEmail);
                tvBadge = itemView.findViewById(R.id.tvItemStatusBadge);
                tvAvatar = itemView.findViewById(R.id.tvAvatarInitials);
            }
        }
    }
}
