package com.example.appdemo.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdemo.Adapter.PopularListAdapter;
import com.example.appdemo.Domain.PopularDomain;
import com.example.appdemo.R;
import com.example.appdemo.Manager.CartManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterPopular;
    private RecyclerView recyclerViewPopular;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo CartManager
        cartManager = CartManager.getInstance(this);

        initRecyclerView();
        initBottomNavigation();
    }

    private void initBottomNavigation() {
        LinearLayout homeBtn = findViewById(R.id.homeBtn);
        LinearLayout cartBtn = findViewById(R.id.cartBtn);

        homeBtn.setOnClickListener(view -> 
            startActivity(new Intent(MainActivity.this, MainActivity.class))
        );
        
        cartBtn.setOnClickListener(view -> 
            startActivity(new Intent(MainActivity.this, CartActivity.class))
        );
    }

    private void initRecyclerView() {
        ArrayList<PopularDomain> items = new ArrayList<>();
        
        items.add(new PopularDomain(
            "M1",
            "Discover the new MacBook Pro 13 featuring the\n" +
            "powerful M2 chip. This cutting-edge laptop\n" +
            "redefines performance and portability. With its \n" +
            "Sleek design and advanced technology, the\n" +
            "MacBook Pro 13 M2 chip is your ultimate\n" +
            "companion for productivity, creativity, and\n" +
            "entertainment. Experience seamless multitasking, \n" +
            "stunning visuals on the Retina display, and\n" +
            "enhanced security with Touch ID. Take your\n" +
            "computing experience to the next level with the\n" +
            "MacBook Pro 13 M2 chip",
            "pic1",
            25,
            4,
            400
        ));

        items.add(new PopularDomain(
            "Ma2p",
            "Discover the new MacBook Pro 13 featuring the\n" +
            "powerful M2 chip. This cutting-edge laptop\n" +
            "redefines performance and portability. With its \n" +
            "Sleek design and advanced technology, the\n" +
            "MacBook Pro 13 M2 chip is your ultimate\n" +
            "companion for productivity, creativity, and\n" +
            "entertainment. Experience seamless multitasking, \n" +
            "stunning visuals on the Retina display, and\n" +
            "enhanced security with Touch ID. Take your\n" +
            "computing experience to the next level with the\n" +
            "Ps-5",
            "pic2",
            10,
            4.5,
            500
        ));

        items.add(new PopularDomain(
            "M43",
            "D14",
            "pic4",
            13,
            4.2,
            800
        ));

        recyclerViewPopular = findViewById(R.id.view1);
        recyclerViewPopular.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        adapterPopular = new PopularListAdapter(items);
        recyclerViewPopular.setAdapter(adapterPopular);
    }
}