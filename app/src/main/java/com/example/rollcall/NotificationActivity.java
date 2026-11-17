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

import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.NotificationModel;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList = new ArrayList<>();

    private EditText etSearchNotifications;
    private View layoutEmptyNotifications;
    private RecyclerView rvNotifications;

    private AuthRepository authRepo;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        authRepo = AuthRepository.getInstance(this);
        currentUser = authRepo.getCurrentUser();

        ImageButton btnBack = findViewById(R.id.btnBackNotifications);
        Button btnClearAll = findViewById(R.id.btnClearNotifications);
        etSearchNotifications = findViewById(R.id.etSearchNotifications);
        layoutEmptyNotifications = findViewById(R.id.layoutEmptyNotifications);
        rvNotifications = findViewById(R.id.rvNotificationsList);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnClearAll != null) {
            btnClearAll.setOnClickListener(v -> {
                if (notificationList.isEmpty()) {
                    Toast.makeText(this, "No notifications to clear", Toast.LENGTH_SHORT).show();
                } else {
                    notificationList.clear();
                    setupAdapter();
                    Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
                }
            });
        }

        setupFilterChips();

        if (etSearchNotifications != null) {
            etSearchNotifications.addTextChangedListener(new TextWatcher() {
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

        loadNotifications();
    }

    private void setupFilterChips() {
        View btnAll = findViewById(R.id.btnNotifFilterAll);
        View btnAttendance = findViewById(R.id.btnNotifFilterAttendance);
        View btnWarning = findViewById(R.id.btnNotifFilterWarning);
        View btnSystem = findViewById(R.id.btnNotifFilterSystem);

        View.OnClickListener listener = v -> {
            String category = "ALL";
            if (v.getId() == R.id.btnNotifFilterAttendance) category = "ATTENDANCE";
            else if (v.getId() == R.id.btnNotifFilterWarning) category = "WARNING";
            else if (v.getId() == R.id.btnNotifFilterSystem) category = "SYSTEM";

            if (adapter != null) {
                int visibleCount = adapter.filterCategory(category);
                updateEmptyState(visibleCount);
            }
        };

        if (btnAll != null) btnAll.setOnClickListener(listener);
        if (btnAttendance != null) btnAttendance.setOnClickListener(listener);
        if (btnWarning != null) btnWarning.setOnClickListener(listener);
        if (btnSystem != null) btnSystem.setOnClickListener(listener);
    }

    private void loadNotifications() {
        notificationList.clear();
        boolean isStudent = currentUser != null && User.ROLE_STUDENT.equalsIgnoreCase(currentUser.getRole());

        if (isStudent) {
            notificationList.add(new NotificationModel("n_1", "Attendance Marked Successfully", "Your attendance for Data Structures & Algorithms (CS301) was verified via GPS geofence in Room 301.", "ATTENDANCE", "Just now", false));
            notificationList.add(new NotificationModel("n_2", "Low Attendance Alert (< 75%)", "Warning: Your attendance in Computer Networks is currently 72%. Minimum required is 75%.", "WARNING", "2h ago", false));
            notificationList.add(new NotificationModel("n_3", "Timetable Slot Rescheduled", "Mid-term DBMS Lecture on Friday has been moved from Room 102 to Room 301 (10:00 AM).", "SYSTEM", "Yesterday", true));
        } else {
            notificationList.add(new NotificationModel("n_1", "Live QR Attendance Session Active", "QR Session for CS301 (Data Structures) is active in Room 301 with auto-rotating tokens.", "ATTENDANCE", "Just now", false));
            notificationList.add(new NotificationModel("n_2", "Manual Attendance Correction Audit", "Manual correction submitted for Roll RC2024001 by Dean's medical leave authorization.", "WARNING", "1h ago", false));
            notificationList.add(new NotificationModel("n_3", "New Academic Term Synchronized", "Course programs and active semester schedules have been synced with Cloud Firestore.", "SYSTEM", "Yesterday", true));
        }

        setupAdapter();
    }

    private void setupAdapter() {
        adapter = new NotificationAdapter(notificationList, this::showNotificationDetailDialog);
        rvNotifications.setAdapter(adapter);
        updateEmptyState(notificationList.size());
    }

    private void showNotificationDetailDialog(NotificationModel item) {
        item.setRead(true);
        if (adapter != null) adapter.notifyDataSetChanged();

        new AlertDialog.Builder(this)
                .setTitle(item.getTitle())
                .setMessage(item.getMessage() + "\n\nReceived: " + item.getTime())
                .setPositiveButton("View Reports", (dialog, which) -> {
                    Intent intent = new Intent(NotificationActivity.this, ReportsActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateEmptyState(int visibleCount) {
        if (layoutEmptyNotifications != null && rvNotifications != null) {
            if (visibleCount == 0) {
                layoutEmptyNotifications.setVisibility(View.VISIBLE);
                rvNotifications.setVisibility(View.GONE);
            } else {
                layoutEmptyNotifications.setVisibility(View.GONE);
                rvNotifications.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnNotificationClickListener {
        void onNotifClick(NotificationModel item);
    }

    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private final List<NotificationModel> originalList;
        private final List<NotificationModel> displayedList;
        private final OnNotificationClickListener clickListener;
        private String currentCategory = "ALL";
        private String currentQuery = "";

        public NotificationAdapter(List<NotificationModel> list, OnNotificationClickListener listener) {
            this.originalList = list;
            this.displayedList = new ArrayList<>(list);
            this.clickListener = listener;
        }

        public int filterCategory(String cat) {
            this.currentCategory = cat;
            return applyFilters();
        }

        public int filterSearch(String query) {
            this.currentQuery = query.toLowerCase().trim();
            return applyFilters();
        }

        private int applyFilters() {
            displayedList.clear();
            for (NotificationModel item : originalList) {
                boolean matchesCategory = "ALL".equalsIgnoreCase(currentCategory) ||
                        (item.getType() != null && item.getType().equalsIgnoreCase(currentCategory));

                if (!matchesCategory) continue;

                boolean matchesQuery = currentQuery.isEmpty() ||
                        (item.getTitle() != null && item.getTitle().toLowerCase().contains(currentQuery)) ||
                        (item.getMessage() != null && item.getMessage().toLowerCase().contains(currentQuery));

                if (matchesQuery) {
                    displayedList.add(item);
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
            NotificationModel item = displayedList.get(position);
            holder.tvName.setText(item.getTitle());
            holder.tvRollEmail.setText(item.getMessage() + "\n" + item.getTime());

            if ("WARNING".equalsIgnoreCase(item.getType())) {
                holder.tvAvatar.setText("ALERT");
                holder.tvBadge.setText("WARNING");
                holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.error_red));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_absent);
            } else if ("ATTENDANCE".equalsIgnoreCase(item.getType())) {
                holder.tvAvatar.setText("LOG");
                holder.tvBadge.setText("VERIFIED");
                holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.success_green));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_present);
            } else {
                holder.tvAvatar.setText("INFO");
                holder.tvBadge.setText("SYSTEM");
                holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.primary_blue));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_present);
            }

            holder.itemView.setAlpha(item.isRead() ? 0.6f : 1.0f);

            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNotifClick(item);
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
