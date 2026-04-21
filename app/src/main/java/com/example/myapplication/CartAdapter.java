package com.example.myapplication;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CartAdapter extends ArrayAdapter<CartItem> {

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    private final Context context;
    private final ArrayList<CartItem> cartList;
    private final OnCartChangedListener listener;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public CartAdapter(Context context, ArrayList<CartItem> cartList, OnCartChangedListener listener) {
        super(context, 0, cartList);
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        }

        CartItem item = cartList.get(position);

        TextView txtCartName = view.findViewById(R.id.txtCartName);
        TextView txtCartTopping = view.findViewById(R.id.txtCartTopping);
        TextView txtCartQuantity = view.findViewById(R.id.txtCartQuantity);
        TextView txtCartPrice = view.findViewById(R.id.txtCartPrice);

        Button btnSizeMCart = view.findViewById(R.id.btnSizeMCart);
        Button btnSizeLCart = view.findViewById(R.id.btnSizeLCart);
        ImageButton btnMinusCart = view.findViewById(R.id.btnMinusCart);
        ImageButton btnPlusCart = view.findViewById(R.id.btnPlusCart);
        Button btnRemoveCart = view.findViewById(R.id.btnRemoveCart);

        txtCartName.setText(item.name);
        txtCartTopping.setText("Topping: " + item.topping);
        txtCartQuantity.setText(String.valueOf(item.quantity));
        txtCartPrice.setText("Thành tiền: " + formatter.format(item.totalPrice) + "đ");

        updateSizeButtonStyle(item, btnSizeMCart, btnSizeLCart);

        btnSizeMCart.setOnClickListener(v -> {
            item.size = "M";
            item.recalculatePrice();
            CartManager.mergeDuplicateItems(context);
            notifyDataSetChanged();
            listener.onCartChanged();
        });
        btnSizeLCart.setOnClickListener(v -> {
            item.size = "L";
            item.recalculatePrice();
            CartManager.mergeDuplicateItems(context);
            notifyDataSetChanged();
            listener.onCartChanged();
        });

        btnMinusCart.setOnClickListener(v -> {
            if (item.quantity > 1) {
                item.quantity--;
                item.recalculatePrice();
                CartManager.saveCart(context);
                notifyDataSetChanged();
                listener.onCartChanged();
            }
        });

        btnPlusCart.setOnClickListener(v -> {
            item.quantity++;
            item.recalculatePrice();
            CartManager.saveCart(context);
            notifyDataSetChanged();
            listener.onCartChanged();
        });

        btnRemoveCart.setOnClickListener(v -> {
            cartList.remove(position);
            CartManager.saveCart(context);
            notifyDataSetChanged();
            listener.onCartChanged();
        });

        return view;
    }

    private void updateSizeButtonStyle(CartItem item, Button btnM, Button btnL) {
        if ("M".equals(item.size)) {
            btnM.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_main))
            );
            btnM.setTextColor(Color.WHITE);

            btnL.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_light))
            );
            btnL.setTextColor(ContextCompat.getColor(context, R.color.pink_dark));
        } else {
            btnL.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_main))
            );
            btnL.setTextColor(Color.WHITE);

            btnM.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink_light))
            );
            btnM.setTextColor(ContextCompat.getColor(context, R.color.pink_dark));
        }
    }
}