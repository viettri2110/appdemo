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
    private ProductAdapter productAdapter;
    private ArrayList<DatabaseHelper.Product> productList;
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
        setupRecyclerView();
        setupListeners();
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

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this::onProductSelected);
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerProducts.setAdapter(productAdapter);
        loadProducts();
    }

    private void loadProducts() {
        productList.clear();
        productList.addAll(dbHelper.getAllProducts());
        productAdapter.notifyDataSetChanged();
    }

    private void onProductSelected(int position) {
        selectedProductIndex = position;
        DatabaseHelper.Product product = productList.get(position);
        edtProductName.setText(product.getName());
        edtProductPrice.setText(String.valueOf(product.getPrice()));
        if (product.getImageUrl() != null) {
            // TODO: Load image
        }
    }

    private void setupListeners() {
        btnManageProducts.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProductManagementActivity.class);
            startActivity(intent);
        });

        btnManageOrders.setOnClickListener(v -> {
            // TODO: Implement Order Management
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Add product management listeners
        btnAdd.setOnClickListener(v -> addProduct());
        btnEdit.setOnClickListener(v -> editProduct());
        btnDelete.setOnClickListener(v -> deleteProduct());
        imgProduct.setOnClickListener(v -> selectImage());
    }

    private void addProduct() {
        String name = edtProductName.getText().toString().trim();
        String priceStr = edtProductPrice.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            long id = dbHelper.addProduct(name, price, null); // null for imageUrl for now
            if (id != -1) {
                loadProducts(); // Reload the list
                clearInputFields();
                Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể thêm sản phẩm", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void editProduct() {
        if (selectedProductIndex == -1) {
            Toast.makeText(this, "Vui lòng chọn sản phẩm cần sửa", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = edtProductName.getText().toString().trim();
        String priceStr = edtProductPrice.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            DatabaseHelper.Product product = productList.get(selectedProductIndex);
            if (dbHelper.updateProduct(product.getId(), name, price, product.getImageUrl())) {
                loadProducts(); // Reload the list
                clearInputFields();
                Toast.makeText(this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể cập nhật sản phẩm", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct() {
        if (selectedProductIndex == -1) {
            Toast.makeText(this, "Vui lòng chọn sản phẩm cần xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                DatabaseHelper.Product product = productList.get(selectedProductIndex);
                if (dbHelper.deleteProduct(product.getId())) {
                    loadProducts(); // Reload the list
                    clearInputFields();
                    Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Không thể xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void selectImage() {
        // TODO: Implement image selection
        Toast.makeText(this, "Tính năng chọn ảnh sẽ được cập nhật sau", Toast.LENGTH_SHORT).show();
    }

    private void clearInputFields() {
        edtProductName.setText("");
        edtProductPrice.setText("");
        imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        selectedProductIndex = -1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // Reload user data when returning to this screen
    }

    private class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
        private ArrayList<DatabaseHelper.Product> products;
        private OnProductClickListener listener;

        public ProductAdapter(ArrayList<DatabaseHelper.Product> products, OnProductClickListener listener) {
            this.products = products;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product_simple, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DatabaseHelper.Product product = products.get(position);
            holder.txtName.setText(product.getName());
            holder.txtPrice.setText(String.format("%.0f VND", product.getPrice()));
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtName, txtPrice;

            ViewHolder(View itemView) {
                super(itemView);
                txtName = itemView.findViewById(R.id.txtName);
                txtPrice = itemView.findViewById(R.id.txtPrice);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onProductClick(position);
                    }
                });
            }
        }
    }
} 