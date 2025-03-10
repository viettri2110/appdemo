package com.example.appdemo.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdemo.database.DatabaseHelper;
import com.example.appdemo.R;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private TextView txtUsername, txtEmail;
    private Button btnManageProducts, btnManageOrders, btnEditProfile, btnLogout;
    private Button btnAdd, btnEdit, btnDelete;
    private CardView adminControlsCard;
    private SharedPreferences sharedPreferences;
    private EditText edtProductName, edtProductPrice;
    private ImageView imgProduct;
    private RecyclerView recyclerProducts;
    private int selectedProductIndex = -1;
    private DatabaseHelper dbHelper;

    // Move interface outside of ProductAdapter
    private interface OnProductClickListener {
        void onProductClick(int position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        initViews();
        loadUserData();
    }

    private void initViews() {
        txtUsername = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtEmail);
        btnManageProducts = findViewById(R.id.btnManageProducts);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        adminControlsCard = findViewById(R.id.adminControlsCard);
        edtProductName = findViewById(R.id.edtProductName);
        edtProductPrice = findViewById(R.id.edtProductPrice);
        imgProduct = findViewById(R.id.imgProduct);
        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        recyclerProducts = findViewById(R.id.recyclerProducts);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
    }

    private void loadUserData() {
        String username = sharedPreferences.getString("username", "");
        String email = sharedPreferences.getString("email", "");
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        txtUsername.setText(username);
        txtEmail.setText(email);
        
        // Hiển thị admin controls nếu user là admin
        adminControlsCard.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }







} 