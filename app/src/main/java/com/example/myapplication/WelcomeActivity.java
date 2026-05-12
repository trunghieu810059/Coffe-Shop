package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {

    private TextView tvWelcome;

    private LinearLayout btnMenu, btnOrderHistory, btnStoreReview, btnContact, btnLogoutBox;
    private LinearLayout layoutBestSeller1, layoutBestSeller2, layoutBestSeller3;

    private TextView txtBestSellerName1, txtBestSellerPrice1, txtBestSellerSold1;
    private TextView txtBestSellerName2, txtBestSellerPrice2, txtBestSellerSold2;
    private TextView txtBestSellerName3, txtBestSellerPrice3, txtBestSellerSold3;

    private ImageView imgBestSeller1, imgBestSeller2, imgBestSeller3;

    private FirebaseFirestore db;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final ArrayList<Product> bestSellerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        db = FirebaseFirestore.getInstance();

        bindViews();
        setupActions();
        loadBestSellerProducts();
    }

    private void bindViews() {
        tvWelcome = findViewById(R.id.tvWelcome);

        btnMenu = findViewById(R.id.btnMenu);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnStoreReview = findViewById(R.id.btnStoreReview);
        btnContact = findViewById(R.id.btnContact);
        btnLogoutBox = findViewById(R.id.btnLogoutBox);

        layoutBestSeller1 = findViewById(R.id.layoutBestSeller1);
        layoutBestSeller2 = findViewById(R.id.layoutBestSeller2);
        layoutBestSeller3 = findViewById(R.id.layoutBestSeller3);

        imgBestSeller1 = findViewById(R.id.imgBestSeller1);
        imgBestSeller2 = findViewById(R.id.imgBestSeller2);
        imgBestSeller3 = findViewById(R.id.imgBestSeller3);

        txtBestSellerName1 = findViewById(R.id.txtBestSellerName1);
        txtBestSellerPrice1 = findViewById(R.id.txtBestSellerPrice1);
        txtBestSellerSold1 = findViewById(R.id.txtBestSellerSold1);

        txtBestSellerName2 = findViewById(R.id.txtBestSellerName2);
        txtBestSellerPrice2 = findViewById(R.id.txtBestSellerPrice2);
        txtBestSellerSold2 = findViewById(R.id.txtBestSellerSold2);

        txtBestSellerName3 = findViewById(R.id.txtBestSellerName3);
        txtBestSellerPrice3 = findViewById(R.id.txtBestSellerPrice3);
        txtBestSellerSold3 = findViewById(R.id.txtBestSellerSold3);
    }

    private void setupActions() {
        String username = getIntent().getStringExtra("username");
        if (username == null || username.trim().isEmpty()) {
            username = "User";
        }

        final String finalUsername = username;
        tvWelcome.setText("Xin chào, " + finalUsername);

        tvWelcome.setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, ProfileActivity.class))
        );

        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, ProductListActivity.class);
            intent.putExtra("username", finalUsername);
            startActivity(intent);
        });

        btnOrderHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Đang mở lịch sử đơn hàng...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(WelcomeActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        btnStoreReview.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, ReviewHistoryActivity.class);
            startActivity(intent);
        });

        btnContact.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, ContactActivity.class);
            startActivity(intent);
        });

        btnLogoutBox.setOnClickListener(v -> {
            UserSession.clear(WelcomeActivity.this);
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadBestSellerProducts() {
        db.collection("Store")
                .orderBy("sold", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bestSellerList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if ("info".equalsIgnoreCase(doc.getId())) {
                            continue;
                        }

                        Product product = mapDocumentToProduct(doc);
                        bestSellerList.add(product);
                    }

                    bindBestSeller(bestSellerList);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải best seller: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private Product mapDocumentToProduct(DocumentSnapshot doc) {
        String docId = doc.getId();

        String name = getStringSafe(doc, "Name", "Sản phẩm");
        String description = getStringSafe(doc, "description", "");
        String category = getStringSafe(doc, "category", "Khác");

        int price = getIntSafe(doc, "price", 0);

        String imageName = getStringSafe(doc, "imageName", "");
        String imageUrl = getStringSafe(doc, "imageUrl", "");

        int imageResId = 0;
        if (!imageName.isEmpty()) {
            imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        }
        if (imageResId == 0) {
            imageResId = R.mipmap.ic_launcher;
        }

        float rating = getFloatSafe(doc, "rating", 0f);
        int sold = getIntSafe(doc, "sold", 0);

        return new Product(
                docId,
                name,
                description,
                category,
                price,
                imageResId,
                imageName,
                imageUrl,
                rating,
                sold
        );
    }

    private void bindBestSeller(ArrayList<Product> list) {
        if (list.size() > 0) {
            Product p1 = list.get(0);
            imgBestSeller1.setImageResource(p1.imageResId);
            txtBestSellerName1.setText(p1.name);
            txtBestSellerPrice1.setText(formatter.format(p1.price) + "đ");
            txtBestSellerSold1.setText("Đã bán " + p1.sold);
            layoutBestSeller1.setOnClickListener(v -> openProductDetail(p1));
        }

        if (list.size() > 1) {
            Product p2 = list.get(1);
            imgBestSeller2.setImageResource(p2.imageResId);
            txtBestSellerName2.setText(p2.name);
            txtBestSellerPrice2.setText(formatter.format(p2.price) + "đ");
            txtBestSellerSold2.setText("Đã bán " + p2.sold);
            layoutBestSeller2.setOnClickListener(v -> openProductDetail(p2));
        }

        if (list.size() > 2) {
            Product p3 = list.get(2);
            imgBestSeller3.setImageResource(p3.imageResId);
            txtBestSellerName3.setText(p3.name);
            txtBestSellerPrice3.setText(formatter.format(p3.price) + "đ");
            txtBestSellerSold3.setText("Đã bán " + p3.sold);
            layoutBestSeller3.setOnClickListener(v -> openProductDetail(p3));
        }
    }

    private void openProductDetail(Product p) {
        Intent intent = new Intent(WelcomeActivity.this, ProductDetailActivity.class);
        intent.putExtra("productId", p.docId);
        startActivity(intent);
    }
    private int getIntSafe(DocumentSnapshot doc, String fieldName, int defaultValue) {
        Object value = doc.get(fieldName);

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    private float getFloatSafe(DocumentSnapshot doc, String fieldName, float defaultValue) {
        Object value = doc.get(fieldName);

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value instanceof String) {
            try {
                return Float.parseFloat(((String) value).trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    private String getStringSafe(DocumentSnapshot doc, String fieldName, String defaultValue) {
        String value = doc.getString(fieldName);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}