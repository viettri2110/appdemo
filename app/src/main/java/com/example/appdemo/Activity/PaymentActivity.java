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
    private EditText edtCardNumber, edtExpiry, edtCvv, edtCardHolder;
    private TextView txtSubtotal, txtTotal;
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

        // Nhận số tiền từ CartActivity
        if (getIntent().hasExtra("total_amount")) {
            totalAmount = getIntent().getDoubleExtra("total_amount", 0);
        }

        initViews();
        setupListeners();
        setupCardNumberFormatter();
        setupExpiryDateFormatter();
        updatePrices();
    }

    private void initViews() {
        edtCardNumber = findViewById(R.id.edtCardNumber);
        edtExpiry = findViewById(R.id.edtExpiry);
        edtCvv = findViewById(R.id.edtCvv);
        edtCardHolder = findViewById(R.id.edtCardHolder);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtTotal = findViewById(R.id.txtTotal);
        btnPay = findViewById(R.id.btnPay);
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing payment...");
    }

    private void setupListeners() {
        btnPay.setOnClickListener(v -> performPayment());
    }

    private void setupCardNumberFormatter() {
        edtCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Format card number in groups of 4
                String text = s.toString().replace(" ", "");
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < text.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(text.charAt(i));
                }
                if (!s.toString().equals(formatted.toString())) {
                    edtCardNumber.setText(formatted.toString());
                    edtCardNumber.setSelection(formatted.length());
                }
            }
        });
    }

    private void setupExpiryDateFormatter() {
        edtExpiry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 2 && !s.toString().contains("/")) {
                    s.append("/");
                    edtExpiry.setSelection(s.length());
                }
            }
        });
    }

    private void updatePrices() {
        // Format số tiền với 2 chữ số thập phân
        String formattedAmount = String.format(Locale.US, "%.2f", totalAmount);
        txtSubtotal.setText("$" + formattedAmount);
        txtTotal.setText("$" + formattedAmount);
    }

    private void performPayment() {
        if (!validateInputs()) {
            return;
        }

        progressDialog.show();

        new Thread(() -> {
            try {
                Thread.sleep(2000); // Giả lập xử lý thanh toán
                
                // Lưu đơn hàng vào database
                String userId = sharedPreferences.getString("email", "");
                long orderId = dbHelper.addOrder(userId, totalAmount);

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (orderId != -1) {
                        Toast.makeText(PaymentActivity.this, 
                            "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
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

    private boolean validateInputs() {
        if (edtCardNumber.getText().toString().replace(" ", "").length() != 10) {
            edtCardNumber.setError("Invalid card number");
            return false;
        }
        if (edtExpiry.getText().toString().length() != 5) {
            edtExpiry.setError("Invalid expiry date");
            return false;
        }
        if (edtCvv.getText().toString().length() != 3) {
            edtCvv.setError("Invalid CVV");
            return false;
        }
        if (edtCardHolder.getText().toString().isEmpty()) {
            edtCardHolder.setError("Required");
            return false;
        }
        return true;
    }
} 