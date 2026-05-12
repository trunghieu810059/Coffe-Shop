package com.example.myapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CartActivity extends AppCompatActivity {

    private ListView cartListView;
    private TextView txtTotal;
    private Button btnCheckout, btnBack;
    private CartAdapter adapter;
    private FirebaseFirestore db;

    private LinearLayout layoutCartRecommendations;
    private LinearLayout recommendationContainer;

    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();

        cartListView = findViewById(R.id.cartListView);
        cartListView.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        txtTotal = findViewById(R.id.txtTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBackCart);
        layoutCartRecommendations = findViewById(R.id.layoutCartRecommendations);
        recommendationContainer = findViewById(R.id.recommendationContainer);

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
        loadCartRecommendations();
    }

    private void loadCartItems() {
        ArrayList<CartItem> cartList = CartManager.getCartList(this);

        if (cartList.isEmpty()) {
            cartListView.setAdapter(null);
            txtTotal.setText("Tổng tiền: 0đ");
            return;
        }

        adapter = new CartAdapter(this, cartList, () -> {
            updateTotalText();
            loadCartRecommendations();
        });
        cartListView.setAdapter(adapter);

        updateTotalText();
    }

    private void updateTotalText() {
        txtTotal.setText("Tổng tiền: " + formatter.format(CartManager.getTotalAmount(this)) + "đ");
    }

    private void loadCartRecommendations() {
        if (recommendationContainer == null) return;

        recommendationContainer.removeAllViews();

        ArrayList<CartItem> cartItems = CartManager.getCartList(this);

        if (cartItems == null || cartItems.isEmpty()) {
            if (layoutCartRecommendations != null) {
                layoutCartRecommendations.setVisibility(View.GONE);
            }
            return;
        }

        Set<String> cartFeatures = new HashSet<>();
        Set<String> productIdsInCart = new HashSet<>();
        Set<String> productNamesInCart = new HashSet<>();

        for (CartItem item : cartItems) {
            if (item.productId != null && !item.productId.trim().isEmpty()) {
                productIdsInCart.add(item.productId.trim());
            }

            if (item.name != null && !item.name.trim().isEmpty()) {
                productNamesInCart.add(item.name.trim().toLowerCase(Locale.ROOT));
            }

            addWords(cartFeatures, item.name);
            addWords(cartFeatures, item.size);
            addWords(cartFeatures, item.topping);
        }

        db.collection("Store")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<RecommendedCartProduct> results = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if ("info".equalsIgnoreCase(doc.getId())) {
                            continue;
                        }

                        if (productIdsInCart.contains(doc.getId())) {
                            continue;
                        }

                        String name = getStringSafe(doc, "Name", "");
                        if (name.trim().isEmpty()) {
                            continue;
                        }

                        if (productNamesInCart.contains(name.trim().toLowerCase(Locale.ROOT))) {
                            continue;
                        }

                        String category = getStringSafe(doc, "category", "");
                        String description = getStringSafe(doc, "description", "");
                        int price = getIntSafe(doc, "price", 0);

                        Set<String> candidateFeatures = buildFeatureSet(name, category, description, price);
                        double score = jaccardSimilarity(cartFeatures, candidateFeatures);

                        if (score > 0) {
                            results.add(new RecommendedCartProduct(doc.getId(), name, price, score));
                        }
                    }

                    Collections.sort(results, (a, b) -> Double.compare(b.score, a.score));

                    recommendationContainer.removeAllViews();
                    int limit = Math.min(3, results.size());

                    if (limit == 0) {
                        if (layoutCartRecommendations != null) {
                            layoutCartRecommendations.setVisibility(View.GONE);
                        }
                        return;
                    }

                    if (layoutCartRecommendations != null) {
                        layoutCartRecommendations.setVisibility(View.VISIBLE);
                    }

                    for (int i = 0; i < limit; i++) {
                        addCartRecommendationView(results.get(i));
                    }
                })
                .addOnFailureListener(e -> {
                    if (layoutCartRecommendations != null) {
                        layoutCartRecommendations.setVisibility(View.GONE);
                    }
                });
    }

    private Set<String> buildFeatureSet(String name, String category, String description, int price) {
        Set<String> features = new HashSet<>();

        addWords(features, name);
        addWords(features, category);
        addWords(features, description);

        if (price > 0) {
            if (price < 25000) {
                features.add("price_low");
            } else if (price <= 40000) {
                features.add("price_medium");
            } else {
                features.add("price_high");
            }
        }

        return features;
    }

    private void addWords(Set<String> features, String text) {
        if (text == null) return;

        String normalized = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim();

        if (normalized.isEmpty()) return;

        for (String word : normalized.split("\\s+")) {
            if (word.length() >= 2) {
                features.add(word);
            }
        }
    }

    private double jaccardSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;

        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);

        Set<String> union = new HashSet<>(a);
        union.addAll(b);

        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    private void addCartRecommendationView(RecommendedCartProduct product) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 8, 0, 8);

        TextView txtInfo = new TextView(this);
        txtInfo.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        txtInfo.setText(product.name + "\n" + formatter.format(product.price) + "đ");
        txtInfo.setTextColor(Color.parseColor("#333333"));
        txtInfo.setTextSize(14);

        Button btnAdd = new Button(this);
        btnAdd.setText("+ Thêm");
        btnAdd.setTextSize(13);
        btnAdd.setTextColor(Color.WHITE);
        btnAdd.setAllCaps(false);
        btnAdd.setMinWidth(0);
        btnAdd.setPadding(14, 0, 14, 0);
        btnAdd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D95B8E")));

        btnAdd.setOnClickListener(v -> {
            CartItem item = new CartItem(
                    product.productId,
                    product.name,
                    "M",
                    "Không",
                    1,
                    product.price
            );

            CartManager.addToCart(this, item);
            Toast.makeText(this, "Đã thêm " + product.name + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            loadCartItems();
            loadCartRecommendations();
        });

        row.addView(txtInfo);
        row.addView(btnAdd);
        recommendationContainer.addView(row);
    }

    private String getStringSafe(QueryDocumentSnapshot doc, String fieldName, String defaultValue) {
        Object value = doc.get(fieldName);
        if (value == null) return defaultValue;

        String text = String.valueOf(value).trim();
        return text.isEmpty() ? defaultValue : text;
    }

    private int getIntSafe(QueryDocumentSnapshot doc, String fieldName, int defaultValue) {
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

    private static class RecommendedCartProduct {
        String productId;
        String name;
        int price;
        double score;

        RecommendedCartProduct(String productId, String name, int price, double score) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.score = score;
        }
    }
}
