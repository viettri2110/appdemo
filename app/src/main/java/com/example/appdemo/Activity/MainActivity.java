package com.example.appdemo.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewPopular;
    private ProductAdapter productAdapter;
    private List<Product> popularProducts;
    private ProductDatabaseHelper databaseHelper;
    private FloatingActionButton fabManageProducts;
    private EditText searchEditText;
    private List<Product> allProducts; // Lưu trữ toàn bộ danh sách sản phẩm
    private LinearLayout laptopCategory, phoneCategory, headphoneCategory, gamingCategory, viewAllCategory;
    private static final int POPULAR_PRODUCT_LIMIT = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.view1);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        searchEditText = findViewById(R.id.editTextText3);

        // Khởi tạo databaseHelper
        databaseHelper = new ProductDatabaseHelper(this);

        // Lấy danh sách sản phẩm và sắp xếp theo giá
        allProducts = databaseHelper.getAllProducts();
        loadPopularProducts();

        // Khởi tạo adapter với context và danh sách sản phẩm
        productAdapter = new ProductAdapter(this, popularProducts);
        recyclerView.setAdapter(productAdapter);

        setupSearchView();
        initViews();
        checkAdminRights();
        initBottomNavigation();
        setupListeners();
        setupCategories();
    }

    private void initViews() {
        recyclerViewPopular = findViewById(R.id.view1);
        fabManageProducts = findViewById(R.id.fabManageProducts);
        
        // Kiểm tra quyền admin và ẩn/hiện nút quản lý sản phẩm
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        fabManageProducts.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void checkAdminRights() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
        
        if (fabManageProducts != null) {
            fabManageProducts.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }
    }

    private void setupListeners() {
        fabManageProducts.setOnClickListener(v -> {
            // Kiểm tra lại quyền admin trước khi mở ProductManagement
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);
            
            if (isAdmin) {
                Intent intent = new Intent(MainActivity.this, ProductManagementActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            }
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

    private void setupSearchView() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterProducts(s.toString());
            }
        });
    }

    private void loadPopularProducts() {
        // Sắp xếp sản phẩm theo giá giảm dần
        Collections.sort(allProducts, (p1, p2) -> 
            Double.compare(p2.getPrice(), p1.getPrice()));

        // Lấy 10 sản phẩm đầu tiên (đắt nhất)
        popularProducts = new ArrayList<>();
        for (int i = 0; i < Math.min(POPULAR_PRODUCT_LIMIT, allProducts.size()); i++) {
            popularProducts.add(allProducts.get(i));
        }
    }

    private void filterProducts(String query) {
        popularProducts.clear();
        
        if (query.isEmpty()) {
            // Khi xóa query, hiển thị lại 10 sản phẩm đắt nhất
            loadPopularProducts();
        } else {
            String lowerQuery = query.toLowerCase();
            for (Product product : allProducts) {
                if (product.getName().toLowerCase().contains(lowerQuery) ||
                    (product.getDescription() != null && 
                     product.getDescription().toLowerCase().contains(lowerQuery)) ||
                    (product.getCategory() != null && 
                     product.getCategory().toLowerCase().contains(lowerQuery))) {
                    popularProducts.add(product);
                }
            }
        }
        
        if (popularProducts.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm phù hợp", Toast.LENGTH_SHORT).show();
        }
        
        productAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProductList();
        checkAdminRights();
    }

    private void refreshProductList() {
        if (popularProducts != null && productAdapter != null) {
            allProducts = databaseHelper.getAllProducts();
            
            // Nếu đang ở chế độ tìm kiếm, giữ nguyên kết quả tìm kiếm
            if (searchEditText != null && !searchEditText.getText().toString().isEmpty()) {
                filterProducts(searchEditText.getText().toString());
            } else {
                // Ngược lại, load lại danh sách popular products
                loadPopularProducts();
            }
            
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

    private void setupCategories() {
        // Khởi tạo các view category
        laptopCategory = findViewById(R.id.laptopCategory);
        phoneCategory = findViewById(R.id.phoneCategory);
        headphoneCategory = findViewById(R.id.headphoneCategory);
        gamingCategory = findViewById(R.id.gamingCategory);
        viewAllCategory = findViewById(R.id.viewAllCategory);

        View laptopBtn = findViewById(R.id.laptopCategoryBtn);
        View phoneBtn = findViewById(R.id.phoneCategoryBtn);
        View headphoneBtn = findViewById(R.id.headphoneCategoryBtn);
        View gamingBtn = findViewById(R.id.gamingCategoryBtn);
        View viewAllBtn = findViewById(R.id.viewAllCategoryBtn);

        // Setup click listeners cho cả LinearLayout và Button
        View.OnClickListener laptopListener = v -> openCategoryProducts("Laptop");
        View.OnClickListener phoneListener = v -> openCategoryProducts("Phone");
        View.OnClickListener headphoneListener = v -> openCategoryProducts("HeadPhone");
        View.OnClickListener gamingListener = v -> openCategoryProducts("Gaming");
        View.OnClickListener viewAllListener = v -> openCategoryProducts("All");

        // Gán listeners cho cả container và button
        laptopCategory.setOnClickListener(laptopListener);
        laptopBtn.setOnClickListener(laptopListener);

        phoneCategory.setOnClickListener(phoneListener);
        phoneBtn.setOnClickListener(phoneListener);

        headphoneCategory.setOnClickListener(headphoneListener);
        headphoneBtn.setOnClickListener(headphoneListener);

        gamingCategory.setOnClickListener(gamingListener);
        gamingBtn.setOnClickListener(gamingListener);

        viewAllCategory.setOnClickListener(viewAllListener);
        viewAllBtn.setOnClickListener(viewAllListener);
    }

    private void openCategoryProducts(String category) {
        Intent intent = new Intent(this, CategoryProductActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }
}