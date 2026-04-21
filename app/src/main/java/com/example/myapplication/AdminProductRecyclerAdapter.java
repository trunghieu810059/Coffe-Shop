package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class AdminProductRecyclerAdapter extends RecyclerView.Adapter<AdminProductRecyclerAdapter.VH> {

    public interface OnItemClick {
        void onClick(Product p);
    }

    public interface OnDeleteClick {
        void onDelete(Product p);
    }

    private final Context context;
    private final ArrayList<Product> list;
    private final OnItemClick onItemClick;
    private final OnDeleteClick onDeleteClick;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public AdminProductRecyclerAdapter(Context context,
                                       ArrayList<Product> list,
                                       OnItemClick onItemClick,
                                       OnDeleteClick onDeleteClick) {
        this.context = context;
        this.list = list;
        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txtName, txtPrice;
        ImageButton btnDelete;

        public VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnDelete = itemView.findViewById(R.id.btnDelete); // ✅ nút xoá
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = list.get(position);

        holder.txtName.setText(p.name);
        holder.txtPrice.setText(formatter.format(p.price) + "đ");

        // Ưu tiên imageUrl (online). Nếu không có thì dùng drawable
        if (p.imageUrl != null && !p.imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(p.imageUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(holder.img);
        } else {
            holder.img.setImageResource(p.imageResId);
        }

        // Click item -> mở edit
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(p);
        });

        // ✅ Click nút xoá -> gọi callback xoá (activity sẽ show dialog confirm)
        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteClick != null) onDeleteClick.onDelete(p);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}