package com.example.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {
    private RecyclerView cartRecyclerView;
    private TextView totalPriceTextView;
    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initViews();
        loadCartItems();
        calculateCart();
    }

    private void initViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
    }

    private void loadCartItems() {
        // Load cart items from database/storage
        // cartItems = ...
    }

    private void calculateCart() {
        // Cache total price view to avoid multiple findViewById calls
        if (totalPriceTextView == null) {
            totalPriceTextView = findViewById(R.id.totalPriceTextView);
        }

        // Early return with default value
        if (cartItems == null || cartItems.isEmpty()) {
            totalPriceTextView.setText(formatPrice(0));
            return;
        }

        // Calculate total using for loop instead of stream for better performance
        double total = 0;
        for (CartItem item : cartItems) {
            if (item != null) {
                total += calculateItemTotal(item);
            }
        }

        // Apply discounts
        total = applyDiscounts(total);
        
        // Update UI on main thread
        runOnUiThread(() -> totalPriceTextView.setText(formatPrice(total)));
    }

    // Extract price calculation logic for single item
    private double calculateItemTotal(CartItem item) {
        double itemPrice = item.getPrice();
        int quantity = Math.max(0, item.getQuantity()); // Ensure non-negative quantity
        return itemPrice * quantity;
    }

    // Format price consistently
    private String formatPrice(double price) {
        return String.format(Locale.US, "$%.2f", Math.max(0, price));
    }

    // Implement discount logic
    private double applyDiscounts(double total) {
        double discountedTotal = total;
        
        // Add discount rules here
        if (total >= 1000) {
            discountedTotal *= 0.9; // 10% discount for orders over $1000
        } else if (total >= 500) {
            discountedTotal *= 0.95; // 5% discount for orders over $500
        }
        
        return discountedTotal;
    }
}

// Separate class file
class CartItem {
    private String id;
    private String name;
    private double price;
    private int quantity;
    
    // Constructor, getters and setters
}