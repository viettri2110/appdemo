package com.example.appdemo.Activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdemo.database.DatabaseHelper;
import com.example.appdemo.R;
import android.content.Intent;

public class EditProfileActivity extends AppCompatActivity {
    private EditText edtName, edtEmail, edtCurrentPassword, edtNewPassword;
    private Button btnSave, btnBack, btnDelete;
    private DatabaseHelper databaseHelper;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật...");

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void loadUserData() {
        String email = sharedPreferences.getString("email", "");
        edtEmail.setText(email);
        edtEmail.setEnabled(false); // Email không thể thay đổi

        // Load tên người dùng từ database
        String name = databaseHelper.getUserName(email);
        if (name != null) {
            edtName.setText(name);
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> updateProfile());
        btnBack.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> deleteAccount());
    }

    private void updateProfile() {
        String name = edtName.getText().toString().trim();
        String currentPassword = edtCurrentPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError("Vui lòng nhập tên");
            return;
        }

        if (!currentPassword.isEmpty() && newPassword.isEmpty()) {
            edtNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return;
        }

        progressDialog.show();

        new Thread(() -> {
          boolean success;
          if (currentPassword.isEmpty()) {
             // Chỉ cập nhật tên
               success = databaseHelper.updateUserName(email, name);
         } else {
              // Kiểm tra mật khẩu hiện tại
              if (!databaseHelper.checkUser(email, currentPassword)) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                       edtCurrentPassword.setError("Mật khẩu không đúng");
                    });
                   return;
               }
              // Cập nhật cả tên và mật khẩu
                success = databaseHelper.updateUser(email, name, newPassword);
            }
            runOnUiThread(() -> {
                progressDialog.dismiss();
                if (success) {
                    Toast.makeText(EditProfileActivity.this,
                            "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this,
                            "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void deleteAccount() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Xóa tài khoản");
        builder.setMessage("Bạn có chắc chắn muốn xóa tài khoản? Hành động này không thể hoàn tác.");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            progressDialog.setMessage("Đang xóa tài khoản...");
            progressDialog.show();

            new Thread(() -> {
                String email = edtEmail.getText().toString().trim();
                boolean success = databaseHelper.deleteUser(email);

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (success) {
                        // Xóa thông tin đăng nhập
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        Toast.makeText(EditProfileActivity.this,
                                "Tài khoản đã được xóa", Toast.LENGTH_SHORT).show();

                        // Chuyển về màn hình đăng nhập
                        finishAffinity();
                        startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
                    } else {
                        Toast.makeText(EditProfileActivity.this,
                                "Không thể xóa tài khoản", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}