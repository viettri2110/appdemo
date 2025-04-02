package com.example.appdemo.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.appdemo.database.DatabaseHelper;
import com.example.appdemo.R;
import com.example.appdemo.Adapter.RecentOrdersAdapter;
import com.example.appdemo.Model.Order;
import android.database.sqlite.SQLiteException;
import android.widget.ImageButton;

import java.util.List;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private TextView txtUsername, txtEmail;
    private Button btnLogout;
    private LinearLayout layoutOrders, layoutReviews, layoutEdit;
    private Button btnViewAllOrders;
    private RecyclerView recyclerRecentOrders;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    private Button btnEditProfile;
    private RecentOrdersAdapter recentOrdersAdapter;
    private CardView adminControls;
    private TextView txtPendingOrders, txtProcessingOrders, txtCompletedOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupListeners();
        loadUserData();
        checkAdminRights();
        setupRecentOrders();
        if (isAdmin()) {
            updateOrderStatistics();
        }
    }

    private void initViews() {
        txtUsername = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtEmail);
        btnLogout = findViewById(R.id.btnLogout);
        layoutOrders = findViewById(R.id.layoutOrders);
        layoutReviews = findViewById(R.id.layoutReviews);
        layoutEdit = findViewById(R.id.layoutEdit);
        btnViewAllOrders = findViewById(R.id.btnViewAllOrders);
        recyclerRecentOrders = findViewById(R.id.recyclerRecentOrders);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        adminControls = findViewById(R.id.adminControls);
        
        // Thống kê đơn hàng
        txtPendingOrders = findViewById(R.id.txtPendingOrders);
        txtProcessingOrders = findViewById(R.id.txtProcessingOrders);
        txtCompletedOrders = findViewById(R.id.txtCompletedOrders);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> logout());
        layoutOrders.setOnClickListener(v -> openOrderHistory());
        layoutReviews.setOnClickListener(v -> openReviews());
        layoutEdit.setOnClickListener(v -> openEditProfile());
        btnViewAllOrders.setOnClickListener(v -> openOrderHistory());
        btnEditProfile.setOnClickListener(v -> openEditProfile());

        // Nút quản lý đơn hàng cho admin
        if (isAdmin()) {
            findViewById(R.id.btnManageOrders).setOnClickListener(v -> {
                Intent intent = new Intent(this, OrderManagementActivity.class);
                startActivity(intent);
            });
        }

        // Nút quản lý người dùng cho admin
        if (isAdmin()) {
            findViewById(R.id.btnManageUsers).setOnClickListener(v -> {
                Intent intent = new Intent(this, UserManagementActivity.class);
                startActivity(intent);
            });
        }

        // Nút quản lý tin nhắn cho admin
        if (isAdmin()) {
            Button btnManageChats = findViewById(R.id.btnManageChats);
            btnManageChats.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
            });
        }
    }

    private boolean isAdmin() {
        return sharedPreferences.getBoolean("isAdmin", false);
    }

    private void updateOrderStatistics() {
        // Lấy số lượng đơn hàng theo trạng thái
        List<Order> allOrders = dbHelper.getAllOrders();
        int pending = 0, processing = 0, completed = 0;

        for (Order order : allOrders) {
            switch (order.getStatus()) {
                case "Chờ xử lý":
                    pending++;
                    break;
                case "Đang giao hàng":
                    processing++;
                    break;
                case "Đã giao":
                    completed++;
                    break;
            }
        }

        // Cập nhật UI
        txtPendingOrders.setText(String.valueOf(pending));
        txtProcessingOrders.setText(String.valueOf(processing));
        txtCompletedOrders.setText(String.valueOf(completed));
    }

    private void loadUserData() {
        String username = sharedPreferences.getString("username", "");
        String email = sharedPreferences.getString("email", "");
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        txtUsername.setText(username);
        txtEmail.setText(email);
        
        // Hiển thị admin controls nếu là admin
        adminControls.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        // Cập nhật thống kê nếu là admin
        if (isAdmin) {
            updateOrderStatistics();
        }
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setPositiveButton("Đăng xuất", (dialog, which) -> {
                // Xóa thông tin đăng nhập
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                // Chuyển về màn hình đăng nhập
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void openOrderHistory() {
        try {
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể mở lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void openReviews() {
        try {
            Intent intent = new Intent(this, MyReviewsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể mở trang đánh giá", Toast.LENGTH_SHORT).show();
        }
    }

    private void openEditProfile() {
        try {
            Log.d("ProfileActivity", "Attempting to open EditProfileActivity");
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error opening EditProfileActivity", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecentOrders() {
        try {
            String userEmail = sharedPreferences.getString("email", "");
            Log.d("ProfileActivity", "Loading orders for user: " + userEmail);

            List<Order> recentOrders = dbHelper.getOrdersByUser(userEmail);
            Log.d("ProfileActivity", "Found " + recentOrders.size() + " orders");

            // Khởi tạo adapter
            recentOrdersAdapter = new RecentOrdersAdapter(recentOrders);
            recyclerRecentOrders.setLayoutManager(new LinearLayoutManager(this));
            recyclerRecentOrders.setAdapter(recentOrdersAdapter);

            // Hiển thị thông báo nếu không có đơn hàng
            if (recentOrders.isEmpty()) {
                Toast.makeText(this, "Bạn chưa có đơn hàng nào", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error setting up orders", e);
            Toast.makeText(this, "Lỗi khi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAdminRights() {
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        
        if (isAdmin) {
            // Hiển thị phần quản lý cho admin
            adminControls.setVisibility(View.VISIBLE);
            
            // Thêm listener cho nút quản lý đơn hàng
            findViewById(R.id.btnManageOrders).setOnClickListener(v -> {
                Intent intent = new Intent(this, OrderManagementActivity.class);
                startActivity(intent);
            });
        } else {
            adminControls.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        checkAdminRights();
        setupRecentOrders(); // Chỉ cập nhật đơn hàng
    }
} 