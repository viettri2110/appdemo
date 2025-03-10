package com.example.appdemo.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdemo.Adapter.ProductAdapter;
import com.example.appdemo.Adapter.PopularListAdapter;
import com.example.appdemo.database.ProductDatabaseHelper;
import com.example.appdemo.Model.Product;
import com.example.appdemo.R;
import com.example.appdemo.Manager.CartManager;
import com.example.appdemo.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewPopular;
    private ProductAdapter productAdapter;
    private List<Product> popularProducts;
    private ProductDatabaseHelper databaseHelper;
    private FloatingActionButton fabManageProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.view1);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Khởi tạo databaseHelper
        databaseHelper = new ProductDatabaseHelper(this);

        // Lấy danh sách sản phẩm
        popularProducts = databaseHelper.getAllProducts();

        // Khởi tạo adapter với context và danh sách sản phẩm
        productAdapter = new ProductAdapter(this, popularProducts);
        recyclerView.setAdapter(productAdapter);

        initViews();
        initBottomNavigation();
        setupListeners();
    }

    private void initViews() {
        recyclerViewPopular = findViewById(R.id.view1);
        fabManageProducts = findViewById(R.id.fabManageProducts);
    }

    private void setupListeners() {
        fabManageProducts.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProductManagementActivity.class);
            startActivity(intent);
        });

        productAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProductList();
    }

    private void refreshProductList() {
        if (popularProducts != null && productAdapter != null) {
            popularProducts.clear();
            popularProducts.addAll(databaseHelper.getAllProducts());
            Log.d("MainActivity", "Loaded products: " + popularProducts.toString());
            productAdapter.notifyDataSetChanged();
        }
    }

    private void initBottomNavigation() {
        LinearLayout homeBtn = findViewById(R.id.homeBtn);
        LinearLayout cartBtn = findViewById(R.id.cartBtn);
        LinearLayout profileBtn = findViewById(R.id.profileBtn);

        profileBtn.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class))
        );

        homeBtn.setOnClickListener(view -> {
            refreshProductList();
        });

        cartBtn.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, CartActivity.class))
        );
    }
}