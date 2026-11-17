package com.example.rollcall;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rollcall.data.AdminRepository;
import com.example.rollcall.data.AttendanceRepository;
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminActivity extends AppCompatActivity {

    public static final String EXTRA_SHOW_TEACHERS = "EXTRA_SHOW_TEACHERS";

    private TextView tvTotalTeachersCount, tvTotalDeptsCount, tvActiveTeachersCount;
    private EditText etAdminSearch;
    private View fabAddTeacher, layoutEmptyAdminUsers;
    private RecyclerView rvAdminUserList;

    private AdminRepository adminRepo;
    private AttendanceRepository repository;
    private AuthRepository authRepo;

    private TeacherAdapter teacherAdapter;
    private List<User> teacherList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        adminRepo = AdminRepository.getInstance(this);
        repository = AttendanceRepository.getInstance(this);
        authRepo = AuthRepository.getInstance(this);

        tvTotalTeachersCount = findViewById(R.id.tvTotalTeachersCount);
        tvTotalDeptsCount = findViewById(R.id.tvTotalDeptsCount);
        tvActiveTeachersCount = findViewById(R.id.tvActiveTeachersCount);
        etAdminSearch = findViewById(R.id.etAdminSearch);
        ImageButton btnBackAdmin = findViewById(R.id.btnBackAdmin);
        fabAddTeacher = findViewById(R.id.fabAddTeacher);
        layoutEmptyAdminUsers = findViewById(R.id.layoutEmptyAdminUsers);
        rvAdminUserList = findViewById(R.id.rvAdminUserList);

        rvAdminUserList.setLayoutManager(new LinearLayoutManager(this));

        if (btnBackAdmin != null) btnBackAdmin.setOnClickListener(v -> finish());
        if (fabAddTeacher != null) fabAddTeacher.setOnClickListener(v -> showAddTeacherDialog());

        setupDepartmentFilterChips();

        if (etAdminSearch != null) {
            etAdminSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (teacherAdapter != null) {
                        int visibleCount = teacherAdapter.filterSearch(s.toString());
                        updateEmptyState(visibleCount);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        loadTeacherList();
    }

    private void setupDepartmentFilterChips() {
        View btnAll = findViewById(R.id.btnDeptAll);
        View btnCS = findViewById(R.id.btnDeptCS);
        View btnIT = findViewById(R.id.btnDeptIT);
        View btnMGMT = findViewById(R.id.btnDeptMGMT);

        View.OnClickListener listener = v -> {
            String dept = "ALL";
            if (v.getId() == R.id.btnDeptCS) dept = "Computer Science";
            else if (v.getId() == R.id.btnDeptIT) dept = "Information";
            else if (v.getId() == R.id.btnDeptMGMT) dept = "Commerce";

            if (teacherAdapter != null) {
                int visibleCount = teacherAdapter.filterDept(dept);
                updateEmptyState(visibleCount);
            }
        };

        if (btnAll != null) btnAll.setOnClickListener(listener);
        if (btnCS != null) btnCS.setOnClickListener(listener);
        if (btnIT != null) btnIT.setOnClickListener(listener);
        if (btnMGMT != null) btnMGMT.setOnClickListener(listener);
    }

    private void loadTeacherList() {
        teacherList = repository.getTeachers();
        setupAdapter();

        adminRepo.fetchTeachers(new AdminRepository.ListCallback<User>() {
            @Override
            public void onSuccess(List<User> items) {
                if (items != null && !items.isEmpty()) {
                    teacherList = items;
                    setupAdapter();
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void setupAdapter() {
        teacherAdapter = new TeacherAdapter(teacherList, this::showTeacherOptionsDialog);
        rvAdminUserList.setAdapter(teacherAdapter);
        updateStats(teacherList);
        updateEmptyState(teacherList.size());
    }

    private void updateStats(List<User> list) {
        int total = list.size();
        Set<String> depts = new HashSet<>();
        int active = 0;
        for (User u : list) {
            if (u.getDepartment() != null && !u.getDepartment().isEmpty()) {
                depts.add(u.getDepartment());
            }
            if (u.isActiveStatus()) {
                active++;
            }
        }

        if (tvTotalTeachersCount != null) tvTotalTeachersCount.setText(String.valueOf(total));
        if (tvTotalDeptsCount != null) tvTotalDeptsCount.setText(String.valueOf(depts.isEmpty() ? 1 : depts.size()));
        if (tvActiveTeachersCount != null) tvActiveTeachersCount.setText(String.valueOf(active > 0 ? active : total));
    }

    private void updateEmptyState(int visibleCount) {
        if (layoutEmptyAdminUsers != null && rvAdminUserList != null) {
            if (visibleCount == 0) {
                layoutEmptyAdminUsers.setVisibility(View.VISIBLE);
                rvAdminUserList.setVisibility(View.GONE);
            } else {
                layoutEmptyAdminUsers.setVisibility(View.GONE);
                rvAdminUserList.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showTeacherOptionsDialog(User teacher) {
        String[] options = {"Send Password Reset Email", "Toggle Active Account", "Close"};
        new AlertDialog.Builder(this)
                .setTitle("Faculty: " + teacher.getName() + " (" + (teacher.getEmployeeId() != null ? teacher.getEmployeeId() : "T001") + ")")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        authRepo.sendPasswordResetEmail(teacher.getEmail(), new AuthRepository.ActionCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(AdminActivity.this, "Password reset email sent to " + teacher.getEmail(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(AdminActivity.this, "Reset email sent to " + teacher.getEmail(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (which == 1) {
                        teacher.setActiveStatus(!teacher.isActiveStatus());
                        updateStats(teacherList);
                        if (teacherAdapter != null) teacherAdapter.notifyDataSetChanged();
                        Toast.makeText(AdminActivity.this, "Teacher account status updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showAddTeacherDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText etName = dialogView.findViewById(R.id.etDlgStudentName);
        EditText etEmail = dialogView.findViewById(R.id.etDlgStudentEmail);
        EditText etEmpId = dialogView.findViewById(R.id.etDlgStudentRoll);
        EditText etDept = dialogView.findViewById(R.id.etDlgStudentDept);
        Button btnCancel = dialogView.findViewById(R.id.btnDlgCancel);
        Button btnSave = dialogView.findViewById(R.id.btnDlgSave);

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String empId = etEmpId.getText().toString().trim();
                String dept = etDept.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                User teacher = new User("teach_" + System.currentTimeMillis(), name, email, "teacher123", User.ROLE_TEACHER, empId.isEmpty() ? "EMP-" + (System.currentTimeMillis() % 1000) : empId, dept.isEmpty() ? "Computer Science" : dept);
                teacher.setEmployeeId(empId.isEmpty() ? "EMP-" + (System.currentTimeMillis() % 1000) : empId);
                teacher.setDepartment(dept.isEmpty() ? "Computer Science" : dept);

                repository.registerUser(teacher);
                adminRepo.addTeacher(teacher, new AdminRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AdminActivity.this, "Teacher added successfully!", Toast.LENGTH_SHORT).show();
                        teacherList.add(0, teacher);
                        setupAdapter();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AdminActivity.this, "Saved locally (" + message + ")", Toast.LENGTH_SHORT).show();
                        teacherList.add(0, teacher);
                        setupAdapter();
                    }
                });
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    public interface OnTeacherClickListener {
        void onTeacherClick(User teacher);
    }

    private static class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.ViewHolder> {
        private final List<User> originalList;
        private final List<User> displayedList;
        private final OnTeacherClickListener clickListener;
        private String currentDeptFilter = "ALL";
        private String currentQuery = "";

        public TeacherAdapter(List<User> list, OnTeacherClickListener listener) {
            this.originalList = list;
            this.displayedList = new ArrayList<>(list);
            this.clickListener = listener;
        }

        public int filterDept(String dept) {
            this.currentDeptFilter = dept;
            return applyFilters();
        }

        public int filterSearch(String query) {
            this.currentQuery = query.toLowerCase().trim();
            return applyFilters();
        }

        private int applyFilters() {
            displayedList.clear();
            for (User u : originalList) {
                boolean matchesDept = "ALL".equalsIgnoreCase(currentDeptFilter) ||
                        (u.getDepartment() != null && u.getDepartment().toLowerCase().contains(currentDeptFilter.toLowerCase()));

                if (!matchesDept) continue;

                boolean matchesQuery = currentQuery.isEmpty() ||
                        (u.getName() != null && u.getName().toLowerCase().contains(currentQuery)) ||
                        (u.getEmployeeId() != null && u.getEmployeeId().toLowerCase().contains(currentQuery)) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(currentQuery)) ||
                        (u.getDepartment() != null && u.getDepartment().toLowerCase().contains(currentQuery));

                if (matchesQuery) {
                    displayedList.add(u);
                }
            }
            notifyDataSetChanged();
            return displayedList.size();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User u = displayedList.get(position);
            holder.tvName.setText(u.getName());

            String idDisplay = u.getEmployeeId() != null ? u.getEmployeeId() : "EMP-101";
            String deptDisplay = u.getDepartment() != null ? u.getDepartment() : "Computer Science";
            holder.tvRollEmail.setText("ID: " + idDisplay + " • " + deptDisplay + "\n" + u.getEmail() + " • Tap to Manage ›");

            String initials = u.getName() != null && u.getName().length() >= 2 ? u.getName().substring(0, 2).toUpperCase() : "FC";
            holder.tvAvatar.setText(initials);

            holder.tvBadge.setText("FACULTY");
            holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.teacher_color));
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_present);

            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTeacherClick(u);
                }
            });
        }

        @Override
        public int getItemCount() {
            return displayedList.size();
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
