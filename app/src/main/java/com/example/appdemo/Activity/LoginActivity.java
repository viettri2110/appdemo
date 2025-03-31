package com.example.appdemo.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdemo.database.DatabaseHelper;
import com.example.appdemo.R;

public class LoginActivity extends AppCompatActivity {
    private static final int REGISTER_REQUEST_CODE = 100;
    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnRegister;
    private DatabaseHelper databaseHelper;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
        
        // Khởi tạo Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        
        initViews();
        setupListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        
        // Thêm forgot password text
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        if (forgotPasswordText != null) {
            forgotPasswordText.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REGISTER_REQUEST_CODE);
        });
    }

    private void performLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Kiểm tra validation
        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        progressDialog.show();

        // Tạo thread mới để thực hiện truy vấn database
        new Thread(() -> {
            // Kiểm tra đăng nhập
            final boolean success = databaseHelper.checkUser(email, password);
            
            // Chạy trên UI thread để cập nhật giao diện
            runOnUiThread(() -> {
                progressDialog.dismiss();
                if (success) {
                    // Lưu trạng thái đăng nhập vào SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("email", email);
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    // Chuyển đến MainActivity
                    Toast.makeText(LoginActivity.this, 
                        "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, 
                        "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REGISTER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String email = data.getStringExtra("email");
            String password = data.getStringExtra("password");
            
            if (email != null) edtEmail.setText(email);
            if (password != null) edtPassword.setText(password);
            
            Toast.makeText(this, "Vui lòng đăng nhập với tài khoản vừa tạo", 
                Toast.LENGTH_SHORT).show();
        }
    }
}