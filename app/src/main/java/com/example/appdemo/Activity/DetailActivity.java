package com.example.appdemo.Activity;

import android.annotation.SuppressLint;
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
import com.example.appdemo.Domain.PopularDomain;
import com.example.appdemo.Manager.CartManager;
import com.example.appdemo.R;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    private TextView titleTxt, priceTxt, descriptionTxt, numberOrderTxt;
    private ImageView productImg, plusBtn, minusBtn;
    private Button addToCartBtn;
    private PopularDomain object;
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
        titleTxt = findViewById(R.id.titleTxt);
        priceTxt = findViewById(R.id.priceTxt);
        descriptionTxt = findViewById(R.id.descriptionTxt);
        numberOrderTxt = findViewById(R.id.numberOrderTxt);
        productImg = findViewById(R.id.productImg);
        plusBtn = findViewById(R.id.plusBtn);
        minusBtn = findViewById(R.id.minusBtn);
        addToCartBtn = findViewById(R.id.addToCartBtn);
    }

    private void getBundle() {
        object = (PopularDomain) getIntent().getSerializableExtra("object");
        if (object == null) {
            Log.e("DetailActivity", "PopularDomain is null");
            finish(); // Close activity if object is null
            return;
        }
        int drawableResourceId = this.getResources().getIdentifier(object.getPicUrl(), "drawable", this.getPackageName());

        Glide.with(this)
                .load(drawableResourceId)
                .into(productImg);

        titleTxt.setText(object.getTitle());
        priceTxt.setText("$" + object.getPrice());
        descriptionTxt.setText(object.getDescription());
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

        addToCartBtn.setOnClickListener(v -> {
            object.setNumberinCart(numberOrder);
            cartManager.addToCart(object);
            Toast.makeText(this, "Added " + numberOrder + " items to cart", 
                Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

