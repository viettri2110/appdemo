package com.example.appdemo.Manager;

import android.content.Context;
import android.util.Log;
import com.example.appdemo.Model.CartItem;
import com.example.appdemo.Model.Product;
import com.example.appdemo.Domain.PopularDomain;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;
    private Context context;

    private CartManager(Context context) {
        this.context = context.getApplicationContext();
        cartItems = new ArrayList<>();
        loadCartFromStorage(); // Load saved cart data
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public void addToCart(Product product) {
        Log.d("CartManager", "Adding product to cart: " + product.getName());
        
        boolean found = false;
        for (CartItem item : cartItems) {
            if (item.getProductName().equals(product.getName())) {
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                Log.d("CartManager", "Increased quantity for: " + product.getName());
                break;
            }
        }

        if (!found) {
            CartItem newItem = new CartItem(
                product.getName(),
                product.getPrice(),
                1,
                product.getImageUrl()
            );
            cartItems.add(newItem);
            Log.d("CartManager", "Added new item to cart: " + product.getName());
        }

        saveCartToStorage();
    }

    public void addToCart(PopularDomain product) {
        Log.d("CartManager", "Adding product to cart: " + product.getTitle());
        
        boolean found = false;
        for (CartItem item : cartItems) {
            if (item.getProductName().equals(product.getTitle())) {
                item.setQuantity(item.getQuantity() + product.getNumberinCart());
                found = true;
                Log.d("CartManager", "Increased quantity for: " + product.getTitle());
                break;
            }
        }

        if (!found) {
            CartItem newItem = new CartItem(
                product.getTitle(),
                product.getPrice(),
                product.getNumberinCart(),
                product.getPicUrl()
            );
            cartItems.add(newItem);
            Log.d("CartManager", "Added new item to cart: " + product.getTitle());
        }

        Log.d("CartManager", "Total items in cart: " + cartItems.size());
        saveCartToStorage();
    }

    public void addToCart(Product product, int quantity) {
        // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
        CartItem existingItem = null;
        for (CartItem item : cartItems) {
            if (item.getProductName().equals(product.getName())) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            // Nếu sản phẩm đã có trong giỏ hàng, tăng số lượng
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            Log.d("CartManager", "Updated quantity for: " + product.getName() + 
                  " to " + existingItem.getQuantity());
        } else {
            // Nếu sản phẩm chưa có trong giỏ hàng, thêm mới
            CartItem newItem = new CartItem(
                product.getName(),
                product.getPrice(),
                quantity,
                product.getImageUrl()
            );
            cartItems.add(newItem);
            Log.d("CartManager", "Added new item to cart: " + product.getName() + 
                  " with quantity " + quantity);
        }

        saveCartToStorage();
    }

    public List<CartItem> getCartItems() {
        // Log để debug
        Log.d("CartManager", "Getting cart items. Size: " + cartItems.size());
        for (CartItem item : cartItems) {
            Log.d("CartManager", "Cart item: " + item.getProductName() + ", Quantity: " + item.getQuantity());
        }
        return cartItems;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < cartItems.size()) {
            CartItem removedItem = cartItems.remove(position);
            Log.d("CartManager", "Removed item: " + removedItem.getProductName());
        }
    }

    public void clearCart() {
        cartItems.clear();
        Log.d("CartManager", "Cart cleared");
    }

    public int getItemCount() {
        return cartItems.size();
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    private void saveCartToStorage() {
        // Implement saving cart to storage (SharedPreferences or SQLite)
        Log.d("CartManager", "Saving cart with " + cartItems.size() + " items");
    }

    private void loadCartFromStorage() {
        // Implement loading cart from storage
        Log.d("CartManager", "Loading cart from storage");
    }

    private void saveCart() {
        // Implementation of saving cart to storage
    }

    private void notifyCartChanged() {
        // Implementation of notifying cart changed
    }
} 