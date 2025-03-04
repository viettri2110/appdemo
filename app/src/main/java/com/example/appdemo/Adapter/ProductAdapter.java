package com.example.appdemo.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdemo.Model.Product;
import com.example.appdemo.Manager.CartManager;
import com.example.appdemo.R;
import com.example.appdemo.Activity.DetailActivity;
import com.example.appdemo.Domain.PopularDomain;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products;
    private Context context;
    private CartManager cartManager;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.cartManager = CartManager.getInstance(context);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        
        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(String.format(Locale.US, "$%.2f", product.getPrice()));
        
        // Có thể thêm load ảnh sản phẩm nếu có
        // Glide.with(context)
        //     .load(product.getImageUrl())
        //     .into(holder.imgProduct);
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });
        
        holder.btnAddToCart.setOnClickListener(v -> {
            // Chuyển đổi Product thành PopularDomain
            PopularDomain popularProduct = new PopularDomain(
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                0,  // review count
                0,  // score
                product.getPrice()
            );
            
            cartManager.addToCart(popularProduct);
            Toast.makeText(context, 
                product.getName() + " added to cart", 
                Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPrice;
        Button btnAddToCart;

        ProductViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
} 