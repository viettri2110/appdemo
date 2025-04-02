package com.example.appdemo.Activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdemo.R;
import com.example.appdemo.database.DatabaseHelper;

import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {
    private EditText edtCardNumber, edtCardHolder, edtPhone, edtAddress;
    private TextView txtTotal;
    private Button btnPay;
    private ProgressDialog progressDialog;
    private double totalAmount = 0;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        
        initViews();
        setupListeners();
        
        // Lấy và hiển thị tổng tiền
        totalAmount = getIntent().getDoubleExtra("total_amount", 0);
        txtTotal.setText(formatCurrency(totalAmount));
    }

    private void initViews() {
        edtCardNumber = findViewById(R.id.edtCardNumber);
        edtCardHolder = findViewById(R.id.edtCardHolder);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        txtTotal = findViewById(R.id.txtTotal);
        btnPay = findViewById(R.id.btnPay);
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý thanh toán...");
        progressDialog.setCancelable(false);

        // Format số thẻ tự động
        edtCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                String numbers = input.replaceAll("\\D", "");
                StringBuilder formatted = new StringBuilder();
                
                for (int i = 0; i < numbers.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(numbers.charAt(i));
                }
                
                if (!input.equals(formatted.toString())) {
                    edtCardNumber.setText(formatted.toString());
                    edtCardNumber.setSelection(formatted.length());
                }
            }
        });
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.US, "%,.0f₫", amount);
    }

    private boolean validateInputs() {
        if (edtPhone.getText().toString().trim().length() < 10) {
            edtPhone.setError("Vui lòng nhập số điện thoại hợp lệ");
            return false;
        }
        if (edtAddress.getText().toString().trim().isEmpty()) {
            edtAddress.setError("Vui lòng nhập địa chỉ giao hàng");
            return false;
        }
        if (edtCardNumber.getText().toString().replace(" ", "").length() < 5) {
            edtCardNumber.setError("Số thẻ không hợp lệ");
            return false;
        }
        if (edtCardHolder.getText().toString().isEmpty()) {
            edtCardHolder.setError("Vui lòng nhập tên chủ thẻ");
            return false;
        }
        return true;
    }

    private void setupListeners() {
        btnPay.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        if (!validateInputs()) return;

        progressDialog.show();
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Giả lập xử lý thanh toán
                
                String userEmail = sharedPreferences.getString("email", "");
                long orderId = dbHelper.addOrder(userEmail, totalAmount);
                
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (orderId != -1) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(PaymentActivity.this, 
                            "Có lỗi xảy ra khi lưu đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
} 