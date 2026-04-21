package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private ListView cartListView;
    private TextView txtTotal;
    private Button btnCheckout, btnBack;
    private CartAdapter adapter;
    private FirebaseFirestore db;

    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();

        cartListView = findViewById(R.id.cartListView);
        txtTotal = findViewById(R.id.txtTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBackCart);

        btnBack.setOnClickListener(v -> finish());

        btnCheckout.setOnClickListener(v -> {
            if (CartManager.getCartList(this).isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("fromCart", true);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }

    private void loadCartItems() {
        ArrayList<CartItem> cartList = CartManager.getCartList(this);

        if (cartList.isEmpty()) {
            cartListView.setAdapter(null);
            txtTotal.setText("Tổng tiền: 0đ");
            return;
        }

        adapter = new CartAdapter(this, cartList, this::updateTotalText);
        cartListView.setAdapter(adapter);

        updateTotalText();
    }

    private void updateTotalText() {
        txtTotal.setText("Tổng tiền: " + formatter.format(CartManager.getTotalAmount(this)) + "đ");
    }
}