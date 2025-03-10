package com.example.appdemo.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdemo.Helper.ChangeNumberItemsListener;
import com.example.appdemo.Helper.ManagmentCart;
import com.example.appdemo.R;
import com.example.appdemo.Adapter.CartAdapter;
import com.example.appdemo.Model.CartItem;
import com.example.appdemo.Manager.CartManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemChangeListener {
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private Button btnOrderNow;
    private List<CartItem> cartItems;
    private static final int PAYMENT_REQUEST_CODE = 100;
    private CartManager cartManager;
    private ImageView backBtn;
    private double tax;
    private TextView totalFeeTxt, taxTxt, deliveryTxt, totalTxt,txtTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance(this);
        
        initViews();
        initCartList();
        setupListeners();
        calculateCart();
    }

    private void calculateCart() {
        double percentTax = 0.02;
        double delivery = 10;
        
        double itemTotal = Math.round(cartManager.getTotal() * 100.0) / 100.0;
        tax = Math.round((itemTotal * percentTax) * 100.0) / 100.0;
        double total = Math.round((itemTotal + tax + delivery) * 100.0) / 100.0;

        totalFeeTxt.setText(String.format("$%.2f", itemTotal));
        taxTxt.setText(String.format("$%.2f", tax));
        deliveryTxt.setText(String.format("$%.2f", delivery));
        totalTxt.setText(String.format("$%.2f", total));
        txtTotal.setText(String.format("$%.2f", total));
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        totalFeeTxt = findViewById(R.id.totalFeeTxt);
        taxTxt = findViewById(R.id.taxTxt);
        deliveryTxt = findViewById(R.id.deliveryTxt);
        totalTxt = findViewById(R.id.totalTxt);
        btnOrderNow = findViewById(R.id.btnOrderNow);
        backBtn = findViewById(R.id.backBtn);
        txtTotal=findViewById(R.id.txtTotal);

        if (recyclerView == null) {
            Toast.makeText(this, "Error: RecyclerView not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initCartList() {
        cartItems = new ArrayList<>();
        cartItems.addAll(cartManager.getCartItems());

        cartAdapter = new CartAdapter(cartItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);
        
        calculateCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cartAdapter != null) {
            cartItems.clear();
            cartItems.addAll(cartManager.getCartItems());
            cartAdapter.notifyDataSetChanged();
            calculateCart();
        }
    }

    private void setupListeners() {
        btnOrderNow.setOnClickListener(v -> {
            if (cartItems != null && !cartItems.isEmpty()) {
                double total = cartManager.getTotal() + tax + 10;
                Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                intent.putExtra("total_amount", total);
                startActivityForResult(intent, PAYMENT_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
        });
        
        backBtn.setOnClickListener(v -> finish());
    }

    @Override
    public void onQuantityChanged() {
        calculateCart();
    }

    @Override
    public void onItemRemoved(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartManager.removeItem(position);
            cartItems.remove(position);
            cartAdapter.notifyItemRemoved(position);
            calculateCart();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
            cartManager.clearCart();
            cartItems.clear();
            cartAdapter.notifyDataSetChanged();
            calculateCart();
            finish();
        }
    }
}