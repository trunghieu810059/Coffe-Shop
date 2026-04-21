package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ProductAdapter extends ArrayAdapter<Product> {

    private final Context context;
    private final ArrayList<Product> productList;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public ProductAdapter(Context context, ArrayList<Product> productList) {
        super(context, 0, productList);
        this.context = context;
        this.productList = productList;
    }

    static class ViewHolder {
        ImageView img;
        TextView txtName, txtOrigin, txtSub, txtPrice;
        Button btnAddToCart, btnOrderNow;
        ImageButton btnFavorite;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);

            holder = new ViewHolder();
            holder.img = view.findViewById(R.id.img);
            holder.txtName = view.findViewById(R.id.txtName);
            holder.txtOrigin = view.findViewById(R.id.txtOrigin);
            holder.txtSub = view.findViewById(R.id.txtSub);
            holder.txtPrice = view.findViewById(R.id.txtPrice);
            holder.btnAddToCart = view.findViewById(R.id.btnAddToCart);
            holder.btnOrderNow = view.findViewById(R.id.btnOrderNow);
            holder.btnFavorite = view.findViewById(R.id.btnFavorite);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Product p = productList.get(position);

        holder.img.setImageResource(p.imageResId);
        holder.txtName.setText(p.name);
        holder.txtOrigin.setText("Nguồn gốc: " + p.category);
        holder.txtSub.setText(p.description);
        holder.txtPrice.setText(formatter.format(p.price) + "đ");

        boolean isFav = FavoriteManager.isFavorite(context, p.name);
        holder.btnFavorite.setImageResource(
                isFav ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off
        );

        holder.btnFavorite.setOnClickListener(v -> {
            if (FavoriteManager.isFavorite(context, p.name)) {
                FavoriteManager.removeFavorite(context, p.name);
                holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                p.isFavorite = false;

                Toast.makeText(
                        context,
                        "Đã bỏ " + p.name + " khỏi yêu thích",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                FavoriteManager.addFavorite(context, p);
                holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                p.isFavorite = true;

                Toast.makeText(
                        context,
                        "Đã thêm " + p.name + " vào yêu thích",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Nút Thêm -> mở chi tiết để chọn size/topping/số lượng
        holder.btnAddToCart.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", p.docId);
            context.startActivity(intent);
        });

        // Nút Đặt ngay -> đi thẳng sang thanh toán với mặc định
        holder.btnOrderNow.setOnClickListener(v -> {
            Intent intent = new Intent(context, CheckoutActivity.class);
            intent.putExtra("productName", p.name);
            intent.putExtra("size", "M");
            intent.putExtra("topping", "Không");
            intent.putExtra("quantity", 1);
            intent.putExtra("unitPrice", p.price);
            context.startActivity(intent);
        });

        // Bấm vào item hoặc ảnh -> mở chi tiết
        view.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", p.docId);
            context.startActivity(intent);
        });

        holder.img.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", p.docId);
            context.startActivity(intent);
        });

        return view;
    }
}