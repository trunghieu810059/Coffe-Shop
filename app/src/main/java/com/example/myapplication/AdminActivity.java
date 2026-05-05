package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminActivity extends AppCompatActivity {

    private TextView tvAdminWelcome, tvOrderBadge, tvReviewBadge;
    private Button btnManageProducts, btnManageOrders, btnRevenue, btnReviews, btnStoreInfo, btnLogoutAdmin;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();
        AdminNoticeHelper.ensureCounterDocument();

        tvAdminWelcome = findViewById(R.id.tvAdminWelcome);
        tvOrderBadge = findViewById(R.id.tvOrderBadge);
        tvReviewBadge = findViewById(R.id.tvReviewBadge);

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
            AdminNoticeHelper.resetOrderNotice();
            tvOrderBadge.setVisibility(View.GONE);

            Intent intent = new Intent(AdminActivity.this, AdminOrderActivity.class);
            startActivity(intent);
        });

        btnRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, RevenueActivity.class);
            startActivity(intent);
        });

        btnReviews.setOnClickListener(v -> {
            AdminNoticeHelper.resetReviewNotice();
            tvReviewBadge.setVisibility(View.GONE);

            Intent intent = new Intent(AdminActivity.this, AdminReviewActivity.class);
            startActivity(intent);
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

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminBadges();
    }

    private void loadAdminBadges() {
        db.collection("AdminCounters")
                .document("dashboard")
                .get()
                .addOnSuccessListener(doc -> {
                    long orderCount = 0;
                    long reviewCount = 0;

                    if (doc.exists()) {
                        Long orderLong = doc.getLong("orderNoticeCount");
                        Long reviewLong = doc.getLong("reviewNoticeCount");

                        orderCount = orderLong != null ? orderLong : 0;
                        reviewCount = reviewLong != null ? reviewLong : 0;
                    }

                    updateBadge(tvOrderBadge, orderCount);
                    updateBadge(tvReviewBadge, reviewCount);

                    showPopupIfNeeded(orderCount, reviewCount);
                });
    }

    private void updateBadge(TextView badgeView, long count) {
        if (count <= 0) {
            badgeView.setVisibility(View.GONE);
        } else {
            badgeView.setVisibility(View.VISIBLE);

            if (count > 99) {
                badgeView.setText("99+");
            } else {
                badgeView.setText(String.valueOf(count));
            }
        }
    }

    private void showPopupIfNeeded(long orderCount, long reviewCount) {
        if (orderCount <= 0 && reviewCount <= 0) {
            return;
        }

        StringBuilder message = new StringBuilder();

        if (orderCount > 0) {
            message.append("• Có ").append(orderCount).append(" thông báo đơn hàng cần chú ý\n");
        }

        if (reviewCount > 0) {
            message.append("• Có ").append(reviewCount).append(" đánh giá mới cần xem");
        }

        new AlertDialog.Builder(this)
                .setTitle("Thông báo quản trị")
                .setMessage(message.toString().trim())
                .setPositiveButton("Đã hiểu", null)
                .show();
    }
}