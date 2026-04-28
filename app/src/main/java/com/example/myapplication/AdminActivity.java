package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private TextView tvAdminWelcome;
    private Button btnManageProducts, btnManageOrders, btnRevenue, btnReviews, btnStoreInfo, btnLogoutAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        tvAdminWelcome = findViewById(R.id.tvAdminWelcome);
        btnManageProducts = findViewById(R.id.btnManageProducts);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnRevenue = findViewById(R.id.btnRevenue);
        btnReviews = findViewById(R.id.btnReviews);
        btnStoreInfo = findViewById(R.id.btnStoreInfo);
        btnLogoutAdmin = findViewById(R.id.btnLogoutAdmin);

        String username = getIntent().getStringExtra("username");
        if (username == null || username.trim().isEmpty()) {
            username = "Admin";
        }

        tvAdminWelcome.setText("Xin chào, " + username);

        btnManageProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminProductActivity.class);
            startActivity(intent);
        });
        btnManageOrders.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminOrderActivity.class);
            startActivity(intent);
        });

        btnRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, RevenueActivity.class);
            startActivity(intent);
        });

        btnReviews.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, AdminReviewActivity.class));
        });

        btnStoreInfo.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, StoreInfoAdminActivity.class);
            startActivity(intent);
        });

        btnLogoutAdmin.setOnClickListener(v -> {
            UserSession.clear(AdminActivity.this);
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}