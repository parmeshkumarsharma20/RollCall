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
import com.example.rollcall.data.AuthRepository;
import com.example.rollcall.model.Room;
import com.example.rollcall.model.User;
import com.example.rollcall.utils.WindowInsetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageRoomsActivity extends AppCompatActivity {

    private AdminRepository adminRepo;
    private AuthRepository authRepo;

    private RoomAdapter adapter;
    private List<Room> roomList = new ArrayList<>();

    private EditText etSearchRooms;
    private View layoutEmptyRooms, fabAdd;
    private RecyclerView rvRooms;

    private User currentUser;
    private boolean isStaff = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_rooms);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        View rootView = findViewById(android.R.id.content);
        WindowInsetUtils.applySystemBarInsets(rootView, null, null);

        adminRepo = AdminRepository.getInstance(this);
        authRepo = AuthRepository.getInstance(this);

        currentUser = authRepo.getCurrentUser();
        isStaff = currentUser == null || !User.ROLE_STUDENT.equalsIgnoreCase(currentUser.getRole());

        ImageButton btnBack = findViewById(R.id.btnBackManageRooms);
        fabAdd = findViewById(R.id.fabAddRoom);
        etSearchRooms = findViewById(R.id.etSearchRooms);
        layoutEmptyRooms = findViewById(R.id.layoutEmptyRooms);
        rvRooms = findViewById(R.id.rvManageRooms);

        TextView tvSubtitle = findViewById(R.id.tvRoomsSubtitle);

        if (!isStaff) {
            if (fabAdd != null) fabAdd.setVisibility(View.GONE);
            if (tvSubtitle != null) tvSubtitle.setText("Classroom GPS Locations & Verified Radius Tags");
        } else {
            if (fabAdd != null) {
                fabAdd.setVisibility(View.VISIBLE);
                fabAdd.setOnClickListener(v -> showAddEditRoomDialog(null));
            }
        }

        rvRooms.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        setupBuildingFilterChips();

        if (etSearchRooms != null) {
            etSearchRooms.addTextChangedListener(new TextWatcher() {
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

        loadRooms();
    }

    private void setupBuildingFilterChips() {
        View btnAll = findViewById(R.id.btnBuildingFilterAll);
        View btnCS = findViewById(R.id.btnBuildingFilterCS);
        View btnIT = findViewById(R.id.btnBuildingFilterIT);

        View.OnClickListener listener = v -> {
            String bld = "ALL";
            if (v.getId() == R.id.btnBuildingFilterCS) bld = "CS";
            else if (v.getId() == R.id.btnBuildingFilterIT) bld = "IT";

            if (adapter != null) {
                int visibleCount = adapter.filterBuilding(bld);
                updateEmptyState(visibleCount);
            }
        };

        if (btnAll != null) btnAll.setOnClickListener(listener);
        if (btnCS != null) btnCS.setOnClickListener(listener);
        if (btnIT != null) btnIT.setOnClickListener(listener);
    }

    private void loadRooms() {
        adminRepo.fetchRooms(new AdminRepository.ListCallback<Room>() {
            @Override
            public void onSuccess(List<Room> items) {
                roomList = items;
                setupAdapter();
            }

            @Override
            public void onError(String message) {
                roomList.clear();
                roomList.add(new Room("room_301", "Room 301", "CS Building", 28.6139, 77.2090, 25.0f));
                roomList.add(new Room("room_102", "Room 102", "IT Building", 28.6145, 77.2095, 25.0f));
                setupAdapter();
            }
        });
    }

    private void setupAdapter() {
        adapter = new RoomAdapter(roomList, isStaff, this::handleRoomClick);
        rvRooms.setAdapter(adapter);
        updateEmptyState(roomList.size());
    }

    private void handleRoomClick(Room room) {
        if (isStaff) {
            String[] options = {"Edit Room Details", "Delete Room", "Cancel"};
            new AlertDialog.Builder(this)
                    .setTitle(room.getRoomNumber() + " (" + room.getBuildingName() + ")")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            showAddEditRoomDialog(room);
                        } else if (which == 1) {
                            confirmDeleteRoom(room);
                        }
                    })
                    .show();
        } else {
            Toast.makeText(this, room.getRoomNumber() + " - " + room.getBuildingName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteRoom(Room room) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Classroom?")
                .setMessage("Are you sure you want to delete " + room.getRoomNumber() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    roomList.remove(room);
                    if (adapter != null) adapter.filterSearch(etSearchRooms != null ? etSearchRooms.getText().toString() : "");
                    Toast.makeText(ManageRoomsActivity.this, "Room deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateEmptyState(int visibleCount) {
        if (layoutEmptyRooms != null && rvRooms != null) {
            if (visibleCount == 0) {
                layoutEmptyRooms.setVisibility(View.VISIBLE);
                rvRooms.setVisibility(View.GONE);
            } else {
                layoutEmptyRooms.setVisibility(View.GONE);
                rvRooms.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showAddEditRoomDialog(Room existingRoom) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_room, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText etNumber = dialogView.findViewById(R.id.etDlgRoomNumber);
        EditText etBuilding = dialogView.findViewById(R.id.etDlgBuildingName);
        EditText etLat = dialogView.findViewById(R.id.etDlgLatitude);
        EditText etLng = dialogView.findViewById(R.id.etDlgLongitude);
        Button btnCancel = dialogView.findViewById(R.id.btnDlgCancel);
        Button btnSave = dialogView.findViewById(R.id.btnDlgSave);

        if (existingRoom != null) {
            if (etNumber != null) etNumber.setText(existingRoom.getRoomNumber());
            if (etBuilding != null) etBuilding.setText(existingRoom.getBuildingName());
            if (etLat != null) etLat.setText(String.valueOf(existingRoom.getLatitude()));
            if (etLng != null) etLng.setText(String.valueOf(existingRoom.getLongitude()));
            if (btnSave != null) btnSave.setText("Save Changes");
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String number = etNumber != null ? etNumber.getText().toString().trim() : "";
                String building = etBuilding != null ? etBuilding.getText().toString().trim() : "CS Building";
                String sLat = etLat != null ? etLat.getText().toString().trim() : "";
                String sLng = etLng != null ? etLng.getText().toString().trim() : "";

                if (number.isEmpty()) {
                    Toast.makeText(this, "Room number is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                double lat = sLat.isEmpty() ? 28.6139 : Double.parseDouble(sLat);
                double lng = sLng.isEmpty() ? 77.2090 : Double.parseDouble(sLng);

                if (existingRoom != null) {
                    existingRoom.setRoomNumber(number);
                    existingRoom.setBuildingName(building);
                    existingRoom.setLatitude(lat);
                    existingRoom.setLongitude(lng);
                    Toast.makeText(this, "Room updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Room room = new Room("room_" + System.currentTimeMillis(), number, building.isEmpty() ? "CS Building" : building, lat, lng, 25.0f);
                    adminRepo.addRoom(room, new AdminRepository.ActionCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onError(String message) {}
                    });
                    roomList.add(0, room);
                    Toast.makeText(this, "Classroom added successfully!", Toast.LENGTH_SHORT).show();
                }

                if (adapter != null) adapter.filterSearch(etSearchRooms != null ? etSearchRooms.getText().toString() : "");
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    private static class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
        private final List<Room> originalList;
        private final List<Room> displayedList;
        private final boolean isStaff;
        private final OnRoomClickListener clickListener;
        private String currentBuildingFilter = "ALL";
        private String currentQuery = "";

        public RoomAdapter(List<Room> rooms, boolean isStaff, OnRoomClickListener listener) {
            this.originalList = rooms;
            this.displayedList = new ArrayList<>(rooms);
            this.isStaff = isStaff;
            this.clickListener = listener;
        }

        public int filterBuilding(String bld) {
            this.currentBuildingFilter = bld;
            return applyFilters();
        }

        public int filterSearch(String query) {
            this.currentQuery = query.toLowerCase().trim();
            return applyFilters();
        }

        private int applyFilters() {
            displayedList.clear();
            for (Room r : originalList) {
                boolean matchesBuilding = "ALL".equalsIgnoreCase(currentBuildingFilter) ||
                        (r.getBuildingName() != null && r.getBuildingName().toLowerCase().contains(currentBuildingFilter.toLowerCase()));

                if (!matchesBuilding) continue;

                boolean matchesQuery = currentQuery.isEmpty() ||
                        (r.getRoomNumber() != null && r.getRoomNumber().toLowerCase().contains(currentQuery)) ||
                        (r.getBuildingName() != null && r.getBuildingName().toLowerCase().contains(currentQuery));

                if (matchesQuery) {
                    displayedList.add(r);
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
            Room r = displayedList.get(position);
            holder.tvName.setText(r.getRoomNumber());

            String bld = r.getBuildingName() != null ? r.getBuildingName() : "CS Building";
            String actionText = isStaff ? " • Tap to Manage ›" : "";
            holder.tvRollEmail.setText(bld + " • GPS: " + String.format(Locale.US, "%.3f, %.3f", r.getLatitude(), r.getLongitude()) + " • " + (int) r.getAllowedRadiusMeters() + "m Radius" + actionText);

            holder.tvAvatar.setText("RM");

            holder.tvBadge.setText("GEOFENCE");
            holder.tvBadge.setTextColor(holder.itemView.getContext().getColor(R.color.success_green));
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_present);

            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onRoomClick(r);
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
