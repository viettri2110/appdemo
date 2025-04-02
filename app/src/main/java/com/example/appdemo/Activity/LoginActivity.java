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
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
        
        // Khởi tạo Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        
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
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                progressDialog.show();
                new Thread(() -> {
                    boolean success = databaseHelper.checkUser(email, password);
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        if (success) {
                            // Lấy thông tin người dùng
                            String name = databaseHelper.getUserName(email);
                            boolean isAdmin = databaseHelper.isAdmin(email);

                            // Lưu thông tin đăng nhập
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("email", email);
                            editor.putString("name", name);
                            editor.putBoolean("isAdmin", isAdmin);
                            editor.apply();

                            // Chuyển đến màn hình tương ứng
                            Intent intent;
                            if (isAdmin) {
                                Toast.makeText(LoginActivity.this, 
                                    "Đăng nhập thành công với quyền Admin", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, 
                                    "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            }
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
        });
        
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REGISTER_REQUEST_CODE);
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            return false;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return false;
        }
        return true;
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