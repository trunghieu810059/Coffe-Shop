package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class AdminProductAdapter extends ArrayAdapter<Product> {

    private final Context context;
    private final ArrayList<Product> productList;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public AdminProductAdapter(Context context, ArrayList<Product> productList) {
        super(context, 0, productList);
        this.context = context;
        this.productList = productList;
    }

    static class ViewHolder {
        ImageView img;
        TextView txtName, txtOrigin, txtSub, txtPrice;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);

            holder = new ViewHolder();
            holder.img = view.findViewById(R.id.img);
            holder.txtName = view.findViewById(R.id.txtName);
            holder.txtOrigin = view.findViewById(R.id.txtOrigin);
            holder.txtSub = view.findViewById(R.id.txtSub);
            holder.txtPrice = view.findViewById(R.id.txtPrice);

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

        return view;
    }
}