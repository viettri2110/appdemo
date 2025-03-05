package com.example.appdemo.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.app.AlertDialog;
import android.widget.GridView;
import android.content.Context;
import android.widget.BaseAdapter;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdemo.Adapter.ProductManagementAdapter;
import com.example.appdemo.Helper.ProductDatabaseHelper;
import com.example.appdemo.Model.Product;
import com.example.appdemo.R;
import com.example.appdemo.database.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity {
    private ProductDatabaseHelper databaseHelper;
    private EditText edtName, edtPrice, edtDescription, edtCategory;
    private ImageView imgProduct;
    private Button btnChooseImage, btnAdd, btnUpdate;
    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private ProductManagementAdapter adapter;
    private List<Product> products;
    private String selectedImagePath;
    private Product selectedProduct;
    private static final int IMAGE_PICK_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        databaseHelper = new ProductDatabaseHelper(this);
        initViews();
        setupRecyclerView();
        loadProducts();
        setupListeners();
    }

    private void initViews() {
        edtName = findViewById(R.id.edtName);
        edtPrice = findViewById(R.id.edtPrice);
        edtDescription = findViewById(R.id.edtDescription);
        edtCategory = findViewById(R.id.edtCategory);
        imgProduct = findViewById(R.id.imgProduct);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnAdd = findViewById(R.id.btnAdd);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupRecyclerView() {
        products = new ArrayList<>();
        adapter = new ProductManagementAdapter(this, products, databaseHelper , this::onProductSelected, this::onProductDelete);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadProducts() {
        products.clear();
        products.addAll(databaseHelper.getAllProducts());
        
        // Log the image URLs for debugging
        for (Product product : products) {
            Log.d("ProductManagement", "Product Image URL: " + product.getImageUrl());
        }
        
        adapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        btnChooseImage.setOnClickListener(v -> selectImage());
        
        btnAdd.setOnClickListener(v -> {
            if (validateInput()) {
                addProduct();
            }
        });

        btnUpdate.setOnClickListener(v -> {
            if (validateInput() && selectedProduct != null) {
                updateProduct();
            }
        });

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void onProductDelete(Product product) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                databaseHelper.deleteProduct(product.getId());
                loadProducts();
                Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void onProductSelected(Product product) {
        selectedProduct = product;
        edtName.setText(product.getName());
        edtPrice.setText(String.valueOf(product.getPrice()));
        edtDescription.setText(product.getDescription());
        edtCategory.setText(product.getCategory());
        
        if (product.getImageUrl() != null) {
            Glide.with(this)
                .load(product.getImageUrl())
                .into(imgProduct);
        }
        
        btnAdd.setEnabled(false);
        btnUpdate.setEnabled(true);
    }

    private void selectImage() {
        showImagePickerDialog();
    }

    private boolean validateInput() {
        if (edtName.getText().toString().isEmpty()) {
            edtName.setError("Name is required");
            return false;
        }
        if (edtPrice.getText().toString().isEmpty()) {
            edtPrice.setError("Price is required");
            return false;
        }
        return true;
    }

    private void addProduct() {
        String name = edtName.getText().toString();
        double price = Double.parseDouble(edtPrice.getText().toString());
        String description = edtDescription.getText().toString();
        String category = edtCategory.getText().toString();

        if (selectedImagePath == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product(0, name, price, description, selectedImagePath, category);
        long id = databaseHelper.addProduct(product);
        if (id > 0) {
            product.setId((int) id);
            products.add(product);
            adapter.notifyItemInserted(products.size() - 1);
            clearForm();
            Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProduct() {
        selectedProduct.setName(edtName.getText().toString());
        selectedProduct.setPrice(Double.parseDouble(edtPrice.getText().toString()));
        selectedProduct.setDescription(edtDescription.getText().toString());
        selectedProduct.setCategory(edtCategory.getText().toString());
        if (selectedImagePath != null) {
            selectedProduct.setImageUrl(selectedImagePath);
        }

        int result = databaseHelper.updateProduct(selectedProduct);
        if (result > 0) {
            int position = products.indexOf(selectedProduct);
            adapter.notifyItemChanged(position);
            clearForm();
            selectedProduct = null;
            btnAdd.setEnabled(true);
            btnUpdate.setEnabled(false);
            Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImagePickerDialog() {
        try {
            String[] imageFiles = getAssets().list("products");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_picker, null);
            builder.setView(dialogView);

            GridView gridView = dialogView.findViewById(R.id.gridViewImages);
            ImageAdapter imageAdapter = new ImageAdapter(this, imageFiles, imgProduct);
            gridView.setAdapter(imageAdapter);

            // Create and show the dialog
            AlertDialog dialog = builder.create();
            dialog.show();

            // Optionally, dismiss the dialog when an image is selected in the adapter
            imageAdapter.setOnImageSelectedListener(() -> dialog.dismiss());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading images", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        edtName.setText("");
        edtPrice.setText("");
        edtDescription.setText("");
        edtCategory.setText("");
        imgProduct.setImageResource(R.drawable.default_product_image); // Set to default image
        selectedImagePath = null;
        btnAdd.setEnabled(true);
    }

    public static class ImageAdapter extends BaseAdapter {
        private String[] imageFiles;
        private ImageView selectedImageView;

        private Context context;
        private OnImageSelectedListener onImageSelectedListener;

        public ImageAdapter(Context context, String[] imageFiles, ImageView selectedImageView) {
            this.context = context;
            this.imageFiles = imageFiles;
            this.selectedImageView = selectedImageView;
        }

        @Override
        public int getCount() {
            return imageFiles.length;
        }

        @Override
        public Object getItem(int position) {
            return imageFiles[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200)); // Set size
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }

            String imagePath = "file:///android_asset/products/" + imageFiles[position];
            Glide.with(context)
                .load(imagePath)
                .into(imageView);

            imageView.setOnClickListener(v -> {
                Glide.with(context)
                    .load(imagePath)
                    .into(((ProductManagementActivity) context).imgProduct); // Update the selected image view

                // Set the selected image path in the activity
                ((ProductManagementActivity) context).selectedImagePath = imagePath; // Set the selected image path

                // Optionally dismiss the dialog if you want to close it after selection
                if (onImageSelectedListener != null) {
                    onImageSelectedListener.onImageSelected();
                }
            });

            return imageView;
        }

        public void setOnImageSelectedListener(OnImageSelectedListener onImageSelectedListener) {
            this.onImageSelectedListener = onImageSelectedListener;
        }
    }

    public interface OnImageSelectedListener {
        void onImageSelected();
    }
}