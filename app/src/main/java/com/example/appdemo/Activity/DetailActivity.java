package com.example.appdemo.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.app.AlertDialog;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdemo.Model.Product;
import com.example.appdemo.R;
import com.example.appdemo.Manager.CartManager;
import com.example.appdemo.database.DatabaseHelper;
import com.example.appdemo.Adapter.PopularListAdapter;
import com.example.appdemo.database.ProductDatabaseHelper;
import com.example.appdemo.utils.SpacingItemDecoration;
import com.example.appdemo.Model.Review;
import com.example.appdemo.Adapter.ReviewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.database.sqlite.SQLiteException;

public class DetailActivity extends AppCompatActivity {
    private TextView titleTxt, priceTxt, descriptionTxt, numberOrderTxt;
    private ImageView productImg, plusBtn, minusBtn,backBtn;
    private AppCompatButton addToCartBtn;
    private int numberOrder = 1;
    private CartManager cartManager;
    private Product currentProduct;
    private ImageButton btnFavorite;
    private DatabaseHelper dbHelper;
    private boolean isFavorite;
    private String userEmail;
    private RecyclerView relatedProductsRecyclerView;
    private PopularListAdapter relatedProductsAdapter;
    private List<Product> relatedProducts;
    private ProductDatabaseHelper productDb;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviews;
    private RatingBar ratingBar;
    private TextView ratingCount;
    private Button writeReviewBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        cartManager = CartManager.getInstance(this);

        initView();
        getBundle();
        setupListeners();

        btnFavorite = findViewById(R.id.btnFavorite);
        dbHelper = new DatabaseHelper(this);
        
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userEmail = sharedPreferences.getString("email", "");
        
        // Kiểm tra trạng thái yêu thích
        isFavorite = dbHelper.isFavorite(userEmail, currentProduct.getId());
        updateFavoriteButton();
        
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Khởi tạo RecyclerView cho sản phẩm liên quan
        setupRelatedProducts();

        // Khởi tạo views cho phần review
        setupReviews();
    }

    private void initView() {
        backBtn=findViewById(R.id.backBtn);
        titleTxt = findViewById(R.id.titleTxt);
        priceTxt = findViewById(R.id.priceTxt);
        descriptionTxt = findViewById(R.id.descriptionTxt);
        productImg = findViewById(R.id.productImg);
        numberOrderTxt = findViewById(R.id.numberOrderTxt);
        plusBtn = findViewById(R.id.plusBtn);
        minusBtn = findViewById(R.id.minusBtn);
        addToCartBtn = findViewById(R.id.addToCartBtn);
    }

    private void getBundle() {
        Intent intent = getIntent();
        currentProduct = (Product) intent.getSerializableExtra("product");

        if (currentProduct != null) {
            titleTxt.setText(currentProduct.getName());
            priceTxt.setText(String.format("%,.0f VNĐ", currentProduct.getPrice()));
            descriptionTxt.setText(currentProduct.getDescription());

            // Tải hình ảnh sản phẩm
            Glide.with(this)
                    .load(currentProduct.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(productImg);

            Log.d("DetailActivity", "Description: " + currentProduct.getDescription());
        }
    }

    private void setupListeners() {
        plusBtn.setOnClickListener(v -> {
            numberOrder++;
            numberOrderTxt.setText(String.valueOf(numberOrder));
        });

        minusBtn.setOnClickListener(v -> {
            if (numberOrder > 1) {
                numberOrder--;
                numberOrderTxt.setText(String.valueOf(numberOrder));
            }
        });
        backBtn.setOnClickListener(v -> finish());

        addToCartBtn.setOnClickListener(v -> {
            if (currentProduct != null) {
                // Thêm sản phẩm vào giỏ hàng với số lượng đã chọn
                for (int i = 0; i < numberOrder; i++) {
                    cartManager.addToCart(currentProduct);
                }
                
                Toast.makeText(DetailActivity.this, 
                    "Added " + numberOrder + " item(s) to cart", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFavorite() {
        if (isFavorite) {
            dbHelper.removeFromFavorites(userEmail, currentProduct.getId());
            Toast.makeText(this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
            Log.d("DetailActivity", "Removed from favorites - Product: " + currentProduct.getName() + ", ID: " + currentProduct.getId());
        } else {
            dbHelper.addToFavorites(userEmail, currentProduct.getId());
            Toast.makeText(this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
            Log.d("DetailActivity", "Added to favorites - Product: " + currentProduct.getName() + ", ID: " + currentProduct.getId());
        }
        isFavorite = !isFavorite;
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        btnFavorite.setImageResource(isFavorite ? 
            R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        Log.d("DetailActivity", "Updated favorite button - isFavorite: " + isFavorite);
    }

    private void setupRelatedProducts() {
        relatedProductsRecyclerView = findViewById(R.id.relatedProductsRecyclerView);
        relatedProducts = new ArrayList<>();
        productDb = new ProductDatabaseHelper(this);

        // Lấy sản phẩm hiện tại từ currentProduct đã có
        if (currentProduct != null) {
            // Log để debug
            Log.d("DetailActivity", "Current product category: " + currentProduct.getCategory());
            
            relatedProducts = productDb.getRelatedProducts(
                currentProduct.getCategory(), 
                currentProduct.getId()
            );
            
            // Log số lượng sản phẩm liên quan
            Log.d("DetailActivity", "Found " + relatedProducts.size() + " related products");
        }

        // Setup RecyclerView
        relatedProductsAdapter = new PopularListAdapter(relatedProducts);
        
        // Hiển thị theo dạng ngang
        LinearLayoutManager layoutManager = new LinearLayoutManager(
            this, 
            LinearLayoutManager.HORIZONTAL, 
            false
        );
        relatedProductsRecyclerView.setLayoutManager(layoutManager);
        relatedProductsRecyclerView.setAdapter(relatedProductsAdapter);

        // Thêm khoảng cách giữa các item
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        relatedProductsRecyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
    }

    private void setupReviews() {
        ratingBar = findViewById(R.id.ratingBar);
        ratingCount = findViewById(R.id.ratingCount);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);

        reviews = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviews);
        reviewsRecyclerView.setAdapter(reviewAdapter);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load reviews từ database
        loadReviews();

        writeReviewBtn.setOnClickListener(v -> {
            if (userEmail.isEmpty()) {
                Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }
            showReviewDialog();
        });
    }

    private void loadReviews() {
        // Xóa dữ liệu mẫu cũ
        reviews.clear();
        
        // Load reviews từ database
        if (currentProduct != null) {
            try {
                reviews.addAll(dbHelper.getProductReviews(currentProduct.getId()));
                Log.d("DetailActivity", "Loaded " + reviews.size() + " reviews");
            } catch (SQLiteException e) {
                Log.e("DetailActivity", "Error loading reviews: " + e.getMessage());
                Toast.makeText(this, "Không thể tải đánh giá", Toast.LENGTH_SHORT).show();
            }
        }
        
        reviewAdapter.notifyDataSetChanged();
        updateRatingDisplay();
    }

    private void updateRatingDisplay() {
        if (reviews.isEmpty()) {
            ratingBar.setRating(0);
            ratingCount.setText("(0 đánh giá)");
            return;
        }

        float avgRating = 0;
        for (Review review : reviews) {
            avgRating += review.getRating();
        }
        avgRating /= reviews.size();

        ratingBar.setRating(avgRating);
        ratingCount.setText(String.format("(%d đánh giá)", reviews.size()));
    }

    private void showReviewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_write_review, null);
        builder.setView(dialogView);

        RatingBar dialogRatingBar = dialogView.findViewById(R.id.dialogRatingBar);
        EditText reviewContent = dialogView.findViewById(R.id.reviewContent);

        AlertDialog dialog = builder.setTitle("Viết đánh giá")
            .setPositiveButton("Gửi", null) // Set null để xử lý click riêng
            .setNegativeButton("Hủy", null)
            .create();

        dialog.show();

        // Xử lý nút Gửi
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            float rating = dialogRatingBar.getRating();
            String content = reviewContent.getText().toString().trim();
            
            if (rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo review mới
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDate = sdf.format(new Date());
            
            Review newReview = new Review(
                0, // ID sẽ được tự động tạo bởi SQLite
                currentProduct.getId(),
                userEmail,
                getUserName(),
                rating,
                content,
                currentDate
            );

            // Lưu vào database
            long result = dbHelper.addReview(newReview);
            if (result != -1) {
                Toast.makeText(this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                // Cập nhật lại danh sách đánh giá
                loadReviews();
            } else {
                Toast.makeText(this, "Không thể gửi đánh giá", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("name", "Người dùng");
    }
}

