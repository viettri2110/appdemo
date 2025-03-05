package com.example.appdemo.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.appdemo.R;
import com.example.appdemo.Model.Product;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private String[] imageFiles;
    private ImageView selectedImageView;
    private Context context;
    private Product[] products;

    public ImageAdapter(Context context, String[] imageFiles, ImageView selectedImageView, Product[] products) {
        this.context = context;
        this.imageFiles = imageFiles;
        this.selectedImageView = selectedImageView;
        this.products = products;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products[position];
        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(String.format("%.0f VND", product.getPrice()));

        // Load the image
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.default_product_image); // Set a default image if none
        }

        holder.itemView.setOnClickListener(v -> {
            selectedImageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                .load(product.getImageUrl())
                .into(selectedImageView);
        });
    }

    @Override
    public int getItemCount() {
        return imageFiles.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName;
        TextView txtPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
} 
