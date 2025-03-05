package com.example.appdemo.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdemo.Helper.ProductDatabaseHelper;
import com.example.appdemo.Model.Product;
import com.example.appdemo.R;
import com.example.appdemo.database.DatabaseHelper;

import java.util.List;

public class ProductManagementAdapter extends RecyclerView.Adapter<ProductManagementAdapter.ViewHolder> {
    private Context context;
    private List<Product> products;
    private ProductDatabaseHelper databaseHelper;
    private OnProductClickListener onClickListener;
    private OnProductDeleteListener onDeleteListener;



    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnProductDeleteListener {
        void onProductDelete(Product product);
    }

    public ProductManagementAdapter(Context context, List<Product> products,
                                    ProductDatabaseHelper databaseHelper,
                                    OnProductClickListener onClickListener,
                                    OnProductDeleteListener onDeleteListener) {
        this.context = context;
        this.products = products;
        this.databaseHelper = databaseHelper;
        this.onClickListener = onClickListener;
        this.onDeleteListener = onDeleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_management, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        
        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(String.format("%.0f VND", product.getPrice()));
        holder.txtCategory.setText(product.getCategory());

        // Load the image
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.default_product_image)
                .error(R.drawable.default_product_image)
                .into(holder.imgProductItem);
        } else {
            holder.imgProductItem.setImageResource(R.drawable.default_product_image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onProductClick(product);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteListener != null) {
                onDeleteListener.onProductDelete(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProductItem;
        TextView txtName, txtPrice, txtCategory;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProductItem = itemView.findViewById(R.id.imgProductItem);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void loadProducts() {
        products.clear();
        products.addAll(databaseHelper.getAllProducts());
        
        // Log the image URLs for debugging
        for (Product product : products) {
            Log.d("ProductManagement", "Product Image URL: " + product.getImageUrl());
        }
        
        notifyDataSetChanged();
    }
} 