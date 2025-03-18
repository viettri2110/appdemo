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

import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private TextView txtUsername, txtEmail;
    private Button btnLogout, btnUserManagement;
    private CardView adminControlsCard;
    private LinearLayout layoutOrders, layoutReviews, layoutEdit;
    private Button btnViewAllOrders;
    private RecyclerView recyclerRecentOrders;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    private Button btnEditProfile;
    private RecentOrdersAdapter recentOrdersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupListeners();
        loadUserData();
    }

    private void initViews() {
        txtUsername = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtEmail);
        btnLogout = findViewById(R.id.btnLogout);
        btnUserManagement = findViewById(R.id.btnUserManagement);
        adminControlsCard = findViewById(R.id.adminControlsCard);
        layoutOrders = findViewById(R.id.layoutOrders);
        layoutReviews = findViewById(R.id.layoutReviews);
        layoutEdit = findViewById(R.id.layoutEdit);
        btnViewAllOrders = findViewById(R.id.btnViewAllOrders);
        recyclerRecentOrders = findViewById(R.id.recyclerRecentOrders);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> logout());
        layoutOrders.setOnClickListener(v -> openOrderHistory());
        layoutReviews.setOnClickListener(v -> openReviews());
        btnEditProfile.setOnClickListener(v -> openEditProfile());
        layoutEdit.setOnClickListener(v -> openEditProfile());
        btnViewAllOrders.setOnClickListener(v -> openOrderHistory());
        
        // Thay đổi cách xử lý click cho nút quản lý người dùng
        btnUserManagement.setOnClickListener(v -> openUserManagement());
    }

    private void loadUserData() {
        String username = sharedPreferences.getString("username", "");
        String email = sharedPreferences.getString("email", "");
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        txtUsername.setText(username);
        txtEmail.setText(email);
        
        // Hiển thị admin controls và nút quản lý người dùng
        adminControlsCard.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        btnUserManagement.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        if (isAdmin) {
            // Thêm nút quản lý chat
            Button btnManageChats = findViewById(R.id.btnManageChats);
            btnManageChats.setVisibility(View.VISIBLE);
            btnManageChats.setOnClickListener(v -> 
                startActivity(new Intent(this, AdminChatListActivity.class))
            );
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
        Intent intent = new Intent(this, ReviewsActivity.class);
        startActivity(intent);
    }

    private void openUserManagement() {
        try {
            Intent intent = new Intent(ProfileActivity.this, UserManagementActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể mở trang quản lý người dùng", Toast.LENGTH_SHORT).show();
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
        String userId = sharedPreferences.getString("email", "");
        List<Order> recentOrders = dbHelper.getOrdersByUser(userId);
        
        recentOrdersAdapter = new RecentOrdersAdapter(this, recentOrders);
        recyclerRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecentOrders.setAdapter(recentOrdersAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        setupRecentOrders(); // Cập nhật danh sách đơn hàng khi quay lại màn hình
    }
} 