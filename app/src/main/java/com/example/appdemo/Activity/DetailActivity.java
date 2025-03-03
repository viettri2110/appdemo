package com.example.appdemo.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appdemo.Domain.PopularDomain;
import com.example.appdemo.Helper.ManagmentCart;
import com.example.appdemo.R;

public class DetailActivity extends AppCompatActivity {
    private Button addToCartBtn;
    private TextView titleTxt, feeTxt, descriptionTxt, reviewTxt, scoreTxt;
    private ImageView picItem, backBtn;
    private PopularDomain object;
    private int numberOrder = 1;
    private ManagmentCart managementCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        managementCart= new ManagmentCart(this);

        initView();
        getBundle();
    }

    private void initView() {

            addToCartBtn=findViewById(R.id.addToCartBtn);
            feeTxt=findViewById(R.id.priceTxt);
            titleTxt=findViewById(R.id.titleTxt);
            descriptionTxt=findViewById(R.id.descriptionTxt);
            picItem=findViewById(R.id.itemPic);
            reviewTxt=findViewById(R.id.reviewTxt);
            scoreTxt=findViewById(R.id.scoreTxt);
            backBtn=findViewById(R.id.backBtn);
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
                .into(picItem);

        titleTxt.setText(object.getTitle());
        feeTxt.setText("$" + object.getPrice());
        descriptionTxt.setText(object.getDescription());
        reviewTxt.setText(object.getReview() + "");
        scoreTxt.setText(object.getScore() + "");

        addToCartBtn.setOnClickListener(view -> {
            object.setNumberinCart(numberOrder);
            managementCart.insertFood(object);
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DetailActivity.this, MainActivity.class));
            }
        });
    }

}

