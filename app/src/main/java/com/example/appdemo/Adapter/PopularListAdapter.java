package com.example.appdemo.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.example.appdemo.Activity.DetailActivity;
import com.example.appdemo.Model.Product;
import com.example.appdemo.R;

import java.io.File;
import java.util.List;

public class PopularListAdapter extends RecyclerView.Adapter<PopularListAdapter.ViewHolder> {
    private List<Product> items;
    private Context context;

    public PopularListAdapter(List<Product> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_pop_list, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product item = items.get(position);
        holder.titleTxt.setText(item.getName());
        holder.feeTxt.setText(String.format("%.0f VND", item.getPrice()));
        holder.ScoreTxt.setText("4.5"); // Default score for now

        // Load image from path or resource
        Log.d("PopularListAdapter", "Loading image for product: " + item.getName() + " with imageUrl: " + item.getImageUrl());
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.pic);
        } else {
            holder.pic.setImageResource(R.drawable.placeholder_image);
        }

        // Thiết lập sự kiện nhấp chuột
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("product", item); // Chuyển sản phẩm đến DetailActivity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, feeTxt, ScoreTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            feeTxt = itemView.findViewById(R.id.feeTxt);
            ScoreTxt = itemView.findViewById(R.id.scoreTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}
