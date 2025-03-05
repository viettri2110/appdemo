package com.example.appdemo.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdemo.Adapter.PopularListAdapter;
import com.example.appdemo.Helper.ProductDatabaseHelper;
import com.example.appdemo.Model.Product;
import com.example.appdemo.R;
import com.example.appdemo.Manager.CartManager;
import com.example.appdemo.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PopularListAdapter adapterPopular;
    private RecyclerView recyclerViewPopular;
    private CartManager cartManager;
    private ProductDatabaseHelper dbHelper;
    private List<Product> productList;
    private FloatingActionButton fabManageProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dbHelper = new ProductDatabaseHelper(this);
        cartManager = CartManager.getInstance(this);

        initViews();
        initRecyclerView();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProductList();
    }

    private void initRecyclerView() {
        productList = new ArrayList<>();
        recyclerViewPopular.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        adapterPopular = new PopularListAdapter(productList);
        recyclerViewPopular.setAdapter(adapterPopular);
        refreshProductList();
    }

    private void refreshProductList() {
        if (productList != null && adapterPopular != null) {
            productList.clear();
            productList.addAll(dbHelper.getAllProducts());
            Log.d("MainActivity", "Loaded products: " + productList.toString());
            adapterPopular.notifyDataSetChanged();
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