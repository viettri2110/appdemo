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
        Product product = (Product) intent.getSerializableExtra("product"); // Nhận sản phẩm từ Intent

        if (product != null) {
            titleTxt.setText(product.getName());
            priceTxt.setText(String.format("$%.2f", product.getPrice()));
            descriptionTxt.setText(product.getDescription());

            // Tải hình ảnh sản phẩm
            Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(productImg);

            Log.d("DetailActivity", "Description: " + product.getDescription());
        } else {
            Log.e("DetailActivity", "Product is null");
            finish(); // Đóng activity nếu sản phẩm null
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

//      addToCartBtn.setOnClickListener(v -> {
//            cartManager.addToCart(new Product(titleTxt.getText().toString(), priceTxt.getText().toString(), numberOrder));
//           Toast.makeText(this, "Added " + numberOrder + " items to cart", Toast.LENGTH_SHORT).show();
//            finish();
//       });
    }
}

