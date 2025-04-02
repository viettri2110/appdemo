package com.example.appdemo.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import com.example.appdemo.database.DatabaseHelper;

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
    private TextView totalFeeTxt, taxTxt, deliveryTxt, totalTxt, emptyCartText;
    private LinearLayout checkoutLayout;
    private static final double TAX_RATE = 0.02; // 2% tax
    private static final double DELIVERY_FEE = 30000; // 30,000 VNĐ delivery fee

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance(this);
        
        initViews();
        setupRecyclerView();
        setupListeners();
        updateCartUI();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        btnOrderNow = findViewById(R.id.btnOrderNow);
        backBtn = findViewById(R.id.backBtn);
        totalFeeTxt = findViewById(R.id.totalFeeTxt);
        taxTxt = findViewById(R.id.taxTxt);
        deliveryTxt = findViewById(R.id.deliveryTxt);
        totalTxt = findViewById(R.id.totalTxt);
        emptyCartText = findViewById(R.id.emptyCartText);
        checkoutLayout = findViewById(R.id.checkoutLayout);

        if (recyclerView == null) {
            Toast.makeText(this, "Error: RecyclerView not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void setupRecyclerView() {
        cartItems = cartManager.getCartItems();
        cartAdapter = new CartAdapter(cartItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        btnOrderNow.setOnClickListener(v -> processCheckout());
        backBtn.setOnClickListener(v -> finish());
    }

    private void updateCartUI() {
        if (cartItems.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            checkoutLayout.setVisibility(View.GONE);
            emptyCartText.setVisibility(View.VISIBLE);
            return;
        }

        recyclerView.setVisibility(View.VISIBLE);
        checkoutLayout.setVisibility(View.VISIBLE);
        emptyCartText.setVisibility(View.GONE);

        double subtotal = cartManager.getTotal();
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax + DELIVERY_FEE;

        totalFeeTxt.setText(formatCurrency(subtotal));
        taxTxt.setText(formatCurrency(tax));
        deliveryTxt.setText(formatCurrency(DELIVERY_FEE));
        totalTxt.setText(formatCurrency(total));
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.US, "%,.0f₫", amount);
    }

    private void processCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị dialog xác nhận thanh toán
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận đặt hàng")
               .setMessage("Bạn có chắc chắn muốn đặt hàng?")
               .setPositiveButton("Đặt hàng", (dialog, which) -> {
                   showPaymentMethodDialog();
               })
               .setNegativeButton("Hủy", null)
               .show();
    }

    private void showPaymentMethodDialog() {
        String[] paymentMethods = {"Thanh toán khi nhận hàng (COD)", "Thẻ ngân hàng"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn phương thức thanh toán")
               .setItems(paymentMethods, (dialog, which) -> {
                   switch (which) {
                       case 0: // COD
                           processCODPayment();
                           break;
                       case 1: // Bank card
                           startBankCardPayment();
                           break;
                   }
               })
               .show();
    }

    private void processCODPayment() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("email", "");
        double total = cartManager.getTotal() + (cartManager.getTotal() * TAX_RATE) + DELIVERY_FEE;

        long orderId = dbHelper.addOrder(userEmail, total);
        if (orderId != -1) {
            // Lưu chi tiết đơn hàng
            for (CartItem item : cartItems) {
                dbHelper.addOrderItem(
                    (int) orderId,                    // order_id 
                    item.getProduct().getId(),        // product_id
                    item.getProduct().getName(),      // product_name
                    item.getQuantity(),               // quantity  
                    item.getPrice()                   // price
                );
            }
            
            cartManager.clearCart();
            showOrderSuccessDialog();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi đặt hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void startBankCardPayment() {
        double total = cartManager.getTotal() + (cartManager.getTotal() * TAX_RATE) + DELIVERY_FEE;
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("total_amount", total);
        startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }

    private void showOrderSuccessDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Đặt hàng thành công")
            .setMessage("Cảm ơn bạn đã đặt hàng. Chúng tôi sẽ sớm liên hệ với bạn.")
            .setPositiveButton("OK", (dialog, which) -> {
                setResult(RESULT_OK);
                finish();
            })
            .setCancelable(false)
            .show();
    }

    @Override
    public void onQuantityChanged() {
        updateCartUI();
    }

    @Override
    public void onItemRemoved(int position) {
        cartManager.removeItem(position);
        cartAdapter.notifyItemRemoved(position);
        updateCartUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            showOrderSuccessDialog();
        }
    }
}