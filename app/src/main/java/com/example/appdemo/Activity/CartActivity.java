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
    private TextView txtTotal;
    private Button btnOrderNow;
    private List<CartItem> cartItems;
    private static final int PAYMENT_REQUEST_CODE = 100;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        // Khởi tạo CartManager với context
        cartManager = CartManager.getInstance(this);
        
        initViews();
        initCartList();
        setupListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        txtTotal = findViewById(R.id.txtTotal);
        btnOrderNow = findViewById(R.id.btnOrderNow);
    }

    private void initCartList() {
        cartItems = new ArrayList<>();
        // Lấy danh sách cart items từ CartManager
        cartItems.addAll(cartManager.getCartItems());

        cartAdapter = new CartAdapter(cartItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);
        
        // Cập nhật tổng tiền
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = cartManager.getTotal();
        txtTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
        btnOrderNow.setEnabled(!cartItems.isEmpty());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh cart data when activity resumes
        if (cartAdapter != null) {
            cartItems.clear();
            cartItems.addAll(cartManager.getCartItems());
            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();
        }
    }

    private void setupListeners() {
        btnOrderNow.setOnClickListener(v -> {
            if (cartItems != null && !cartItems.isEmpty()) {
                double total = cartManager.getTotal();
                Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                intent.putExtra("total_amount", total);
                startActivityForResult(intent, PAYMENT_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onQuantityChanged() {
        updateTotalPrice();
    }

    @Override
    public void onItemRemoved(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            cartAdapter.notifyItemRemoved(position);
            updateTotalPrice();
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
            updateTotalPrice();
            finish();
        }
    }
}