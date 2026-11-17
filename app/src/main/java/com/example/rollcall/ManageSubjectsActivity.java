package com.example.rollcall;

import android.content.Intent;
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
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.Subject;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageSubjectsActivity extends AppCompatActivity {

    private AdminRepository adminRepo;
    private AuthRepository authRepo;

    private SubjectAdapter adapter;
    private List<Subject> subjectList = new ArrayList<>();

    private EditText etSearchSubjects;
    private View layoutEmptySubjects, fabAdd;
    private RecyclerView rvSubjects;

    private User currentUser;
    private boolean isStaff = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_subjects);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        adminRepo = AdminRepository.getInstance(this);
        authRepo = AuthRepository.getInstance(this);

        currentUser = authRepo.getCurrentUser();
        isStaff = currentUser == null || !User.ROLE_STUDENT.equalsIgnoreCase(currentUser.getRole());

        ImageButton btnBack = findViewById(R.id.btnBackManageSubjects);
        fabAdd = findViewById(R.id.fabAddSubject);
        etSearchSubjects = findViewById(R.id.etSearchSubjects);
        layoutEmptySubjects = findViewById(R.id.layoutEmptySubjects);
        rvSubjects = findViewById(R.id.rvManageSubjects);

        TextView tvSubtitle = findViewById(R.id.tvSubjectsSubtitle);

        if (!isStaff) {
            if (fabAdd != null) fabAdd.setVisibility(View.GONE);
            if (tvSubtitle != null) tvSubtitle.setText("Enrolled Subjects & Attendance Overview");
        } else {
            if (fabAdd != null) {
                fabAdd.setVisibility(View.VISIBLE);
                fabAdd.setOnClickListener(v -> showAddEditSubjectDialog(null));
            }
        }

        rvSubjects.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        setupCourseFilterChips();

        if (etSearchSubjects != null) {
            etSearchSubjects.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        int visibleCount = adapter.filterSearch(s.toString());
                        updateEmptyState(visibleCount);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        loadSubjects();
    }

    private void setupCourseFilterChips() {
        View btnAll = findViewById(R.id.btnSubjectFilterAll);
        View btnCSE = findViewById(R.id.btnSubjectFilterCSE);
        View btnBCA = findViewById(R.id.btnSubjectFilterBCA);
        View btnMCA = findViewById(R.id.btnSubjectFilterMCA);

        View.OnClickListener listener = v -> {
            String courseFilter = "ALL";
            if (v.getId() == R.id.btnSubjectFilterCSE) courseFilter = "CSE";
            else if (v.getId() == R.id.btnSubjectFilterBCA) courseFilter = "BCA";
            else if (v.getId() == R.id.btnSubjectFilterMCA) courseFilter = "MCA";

            if (adapter != null) {
                int visibleCount = adapter.filterCourse(courseFilter);
                updateEmptyState(visibleCount);
            }
        };

        if (btnAll != null) btnAll.setOnClickListener(listener);
        if (btnCSE != null) btnCSE.setOnClickListener(listener);
        if (btnBCA != null) btnBCA.setOnClickListener(listener);
        if (btnMCA != null) btnMCA.setOnClickListener(listener);
    }

    private void loadSubjects() {
        adminRepo.fetchSubjects(new AdminRepository.ListCallback<Subject>() {
            @Override
            public void onSuccess(List<Subject> items) {
                subjectList = items;
                setupAdapter();
            }

            @Override
            public void onError(String message) {
                subjectList.clear();
                subjectList.add(new Subject("subj_1", "CS301", "Data Structures & Algorithms", "B.Tech CSE", "6th", "CSE_6A"));
                subjectList.add(new Subject("subj_2", "CS302", "Database Management Systems", "B.Tech CSE", "6th", "CSE_6A"));
                subjectList.add(new Subject("subj_3", "BCA101", "Programming in C", "BCA", "4th", "A"));
                subjectList.add(new Subject("subj_4", "MCA107", "Database Management System", "MCA", "1st", "A"));
                setupAdapter();
            }
        });
    }

    private void setupAdapter() {
        adapter = new SubjectAdapter(subjectList, isStaff, this::handleSubjectClick);
        rvSubjects.setAdapter(adapter);
        updateEmptyState(subjectList.size());
    }

    private void handleSubjectClick(Subject subject) {
        if (isStaff) {
            String[] options = {"View Subject Attendance Analytics", "Edit Subject Details", "Delete Subject", "Cancel"};
            new AlertDialog.Builder(this)
                    .setTitle(subject.getSubjectName() + " (" + subject.getSubjectCode() + ")")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            openSubjectAnalytics(subject);
                        } else if (which == 1) {
                            showAddEditSubjectDialog(subject);
                        } else if (which == 2) {
                            confirmDeleteSubject(subject);
                        }
                    })
                    .show();
        } else {
            openSubjectAnalytics(subject);
        }
    }

    private void openSubjectAnalytics(Subject subject) {
        Intent intent = new Intent(this, AnalyticsActivity.class);
        intent.putExtra(AnalyticsActivity.EXTRA_SUBJECT, subject);
        startActivity(intent);
    }

    private void confirmDeleteSubject(Subject subject) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Subject?")
                .setMessage("Are you sure you want to delete " + subject.getSubjectName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    subjectList.remove(subject);
                    if (adapter != null) adapter.filterSearch(etSearchSubjects != null ? etSearchSubjects.getText().toString() : "");
                    Toast.makeText(ManageSubjectsActivity.this, "Subject deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateEmptyState(int visibleCount) {
        if (layoutEmptySubjects != null && rvSubjects != null) {
            if (visibleCount == 0) {
                layoutEmptySubjects.setVisibility(View.VISIBLE);
                rvSubjects.setVisibility(View.GONE);
            } else {
                layoutEmptySubjects.setVisibility(View.GONE);
                rvSubjects.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showAddEditSubjectDialog(Subject existingSubject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subject, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText etCode = dialogView.findViewById(R.id.etDlgSubjectCode);
        EditText etName = dialogView.findViewById(R.id.etDlgSubjectName);
        EditText etCourse = dialogView.findViewById(R.id.etDlgCourseSem);
        Button btnCancel = dialogView.findViewById(R.id.btnDlgCancel);
        Button btnSave = dialogView.findViewById(R.id.btnDlgSave);

        if (existingSubject != null) {
            if (etCode != null) etCode.setText(existingSubject.getSubjectCode());
            if (etName != null) etName.setText(existingSubject.getSubjectName());
            if (etCourse != null) etCourse.setText(existingSubject.getCourse());
            if (btnSave != null) btnSave.setText("Save Changes");
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String code = etCode != null ? etCode.getText().toString().trim() : "";
                String name = etName != null ? etName.getText().toString().trim() : "";
                String course = etCourse != null ? etCourse.getText().toString().trim() : "";

                if (code.isEmpty() || name.isEmpty()) {
                    Toast.makeText(this, "Subject code and name are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (existingSubject != null) {
                    existingSubject.setSubjectCode(code);
                    existingSubject.setSubjectName(name);
                    existingSubject.setCourse(course.isEmpty() ? "B.Tech CSE" : course);
                    Toast.makeText(this, "Subject details updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Subject subject = new Subject("subj_" + System.currentTimeMillis(), code, name, course.isEmpty() ? "B.Tech CSE" : course, "6th", "CSE_6A");
                    adminRepo.addSubject(subject, new AdminRepository.ActionCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onError(String message) {}
                    });
                    subjectList.add(0, subject);
                    Toast.makeText(this, "Subject added successfully!", Toast.LENGTH_SHORT).show();
                }

                if (adapter != null) adapter.filterSearch(etSearchSubjects != null ? etSearchSubjects.getText().toString() : "");
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    public interface OnSubjectClickListener {
        void onSubjectClick(Subject subject);
    }

    private static class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {
        private final List<Subject> originalList;
        private final List<Subject> displayedList;
        private final boolean isStaff;
        private final OnSubjectClickListener clickListener;
        private String currentCourseFilter = "ALL";
        private String currentQuery = "";

        public SubjectAdapter(List<Subject> subjects, boolean isStaff, OnSubjectClickListener listener) {
            this.originalList = subjects;
            this.displayedList = new ArrayList<>(subjects);
            this.isStaff = isStaff;
            this.clickListener = listener;
        }

        public int filterCourse(String courseName) {
            this.currentCourseFilter = courseName;
            return applyFilters();
        }

        public int filterSearch(String query) {
            this.currentQuery = query.toLowerCase().trim();
            return applyFilters();
        }

        private int applyFilters() {
            displayedList.clear();
            for (Subject s : originalList) {
                boolean matchesCourse = "ALL".equalsIgnoreCase(currentCourseFilter) ||
                        (s.getCourse() != null && s.getCourse().toLowerCase().contains(currentCourseFilter.toLowerCase()));

                if (!matchesCourse) continue;

                boolean matchesQuery = currentQuery.isEmpty() ||
                        (s.getSubjectName() != null && s.getSubjectName().toLowerCase().contains(currentQuery)) ||
                        (s.getSubjectCode() != null && s.getSubjectCode().toLowerCase().contains(currentQuery)) ||
                        (s.getCourse() != null && s.getCourse().toLowerCase().contains(currentQuery));

                if (matchesQuery) {
                    displayedList.add(s);
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
            Subject s = displayedList.get(position);
            holder.tvName.setText(s.getSubjectName());

            String code = s.getSubjectCode() != null ? s.getSubjectCode() : "SUBJ";
            String teacher = "Prof. Alan Turing";

            int totalClasses = 30;
            int presentClasses = 25;
            int absentClasses = Math.max(0, totalClasses - presentClasses);
            float percent = ((float) presentClasses / totalClasses) * 100f;

            String actionText = isStaff ? " • Tap to Manage ›" : " • Tap for Analytics ›";
            String details = code + " • Teacher: " + teacher + "\nTotal: " + totalClasses + " | Present: " + presentClasses + " | Absent: " + absentClasses + actionText;
            holder.tvRollEmail.setText(details);

            String initials = code.length() >= 3 ? code.substring(0, 3) : code;
            holder.tvAvatar.setText(initials.toUpperCase());

            if (percent >= 75f) {
                holder.tvBadge.setText(String.format(Locale.US, "%.1f%%", percent));
                holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.success_green));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_present);
            } else {
                holder.tvBadge.setText(String.format(Locale.US, "%.1f%%", percent));
                holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.error_red));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_absent);
            }

            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onSubjectClick(s);
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
