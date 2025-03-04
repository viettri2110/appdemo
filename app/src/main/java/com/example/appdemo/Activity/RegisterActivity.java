package com.example.appdemo.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdemo.Database.DatabaseHelper;
import com.example.appdemo.R;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword, edtName;
    private Button btnRegister, btnBack;
    private DatabaseHelper databaseHelper;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng ký...");
        
        initViews();
        setupListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtName = findViewById(R.id.edtName);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);

        // Kiểm tra null
        if (btnRegister == null) {
            Toast.makeText(this, "Error: btnRegister not found", Toast.LENGTH_SHORT).show();
        }
        if (btnBack == null) {
            Toast.makeText(this, "Error: btnBack not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> performRegister());
        }
        
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void performRegister() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String name = edtName.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }
        if (name.isEmpty()) {
            edtName.setError("Vui lòng nhập tên");
            return;
        }

        progressDialog.show();

        new Thread(() -> {
            long result = databaseHelper.addUser(email, password, name);
            runOnUiThread(() -> {
                progressDialog.dismiss();
                if (result != -1) {
                    Toast.makeText(RegisterActivity.this, 
                            "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("email", email);
                    resultIntent.putExtra("password", password);
                    setResult(RESULT_OK, resultIntent);
                    
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, 
                            "Email đã tồn tại", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
} 