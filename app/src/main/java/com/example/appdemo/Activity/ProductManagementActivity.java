package com.example.appdemo.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdemo.Adapter.ProductManagementAdapter;
import com.example.appdemo.database.ProductDatabaseHelper;
import com.example.appdemo.Model.Product;
import com.example.appdemo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.widget.BaseAdapter;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;

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
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private static final int PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        databaseHelper = new ProductDatabaseHelper(this);
        initViews();
        setupRecyclerView();
        loadProducts();
        setupListeners();

        imgProduct.setOnClickListener(v -> {
            openImagePicker();
        });
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
        adapter = new ProductManagementAdapter(this, products, databaseHelper, this::onProductSelected, this::onProductDelete);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadProducts() {
        products.clear();
        products.addAll(databaseHelper.getAllProducts());
        adapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        btnAdd.setOnClickListener(v -> addProduct());
        btnUpdate.setOnClickListener(v -> updateProduct());
        btnBack.setOnClickListener(v -> finish());
        btnChooseImage.setOnClickListener(v -> openImagePicker());
    }

    private void onProductDelete(Product product) {
        new AlertDialog.Builder(this)
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
            Glide.with(this).load(product.getImageUrl()).into(imgProduct);
        }

        btnAdd.setEnabled(false);
        btnUpdate.setEnabled(true);

        Log.d("DetailActivity", "Description: " + product.getDescription());
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
        String name = edtName.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String category = edtCategory.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || description.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            
            // Lưu ảnh và lấy đường dẫn
            String imagePath = saveImageToInternalStorage(selectedImageUri);
            if (imagePath == null) {
                Toast.makeText(this, "Không thể lưu hình ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            Product newProduct = new Product(0, name, price, description, imagePath, category);
            long id = databaseHelper.addProduct(newProduct);
            
            if (id != -1) {
                newProduct.setId((int) id);
                products.add(newProduct);
                adapter.notifyItemInserted(products.size() - 1);
                clearForm();
                Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể thêm sản phẩm", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
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

            AlertDialog dialog = builder.create();
            dialog.show();

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
        imgProduct.setImageResource(R.drawable.placeholder_image);
        selectedImagePath = null;
        btnAdd.setEnabled(true);
    }

    private void clearInputFields() {
        edtName.setText("");
        edtPrice.setText("");
        edtDescription.setText("");
        edtCategory.setText("");
        imgProduct.setImageResource(R.drawable.placeholder_image);
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
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }

            String imagePath = "file:///android_asset/products/" + imageFiles[position];
            Glide.with(context).load(imagePath).into(imageView);

            imageView.setOnClickListener(v -> {
                Glide.with(context).load(imagePath).into(selectedImageView);
                ((ProductManagementActivity) context).selectedImagePath = imagePath;

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

    private void openImagePicker() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13 và cao hơn
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.READ_MEDIA_IMAGES};
                requestPermissions(permissions, PERMISSION_CODE);
            } else {
                pickImageFromGallery();
            }
        } else {
            // Android 12 và thấp hơn
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_CODE);
            } else {
                pickImageFromGallery();
            }
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Quyền truy cập bị từ chối", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                // Hiển thị ảnh đã chọn
                Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(imgProduct);
                
                // Lưu đường dẫn ảnh
                selectedImagePath = selectedImageUri.toString();
                
                Log.d("ImagePicker", "Selected image URI: " + selectedImageUri);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            File directory = new File(getFilesDir(), "product_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}