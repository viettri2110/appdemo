package com.example.appdemo.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appdemo.Model.Product;
import com.example.appdemo.R;
import com.example.appdemo.Manager.CartManager;

public class DetailActivity extends AppCompatActivity {
    private TextView titleTxt, priceTxt, descriptionTxt, numberOrderTxt;
    private ImageView productImg, plusBtn, minusBtn,backBtn;
    private Button addToCartBtn;
    private int numberOrder = 1;
    private CartManager cartManager;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        cartManager = CartManager.getInstance(this);

        initView();
        getBundle();
        setupListeners();
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
}

