package com.example.myapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProduct;
    private TextView txtName, txtDescription, txtPrice, txtQuantity;
    private TextView txtServiceInfo, txtAverageRating;

    private Button btnBack, btnAddToCart, btnOrderNow;
    private ImageButton btnMinus, btnPlus;

    private Button btnSizeM, btnSizeL, btnTopTranChau, btnTopThachDua;

    private FirebaseFirestore db;

    private int quantity = 1;
    private int basePrice = 0;
    private int sizeExtra = 0;
    private int toppingExtra = 0;

    private String productName = "";
    private String selectedSize = "M";
    private String selectedTopping = "Không";

    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();

        bindViews();
        setupDefaultSelections();
        setupEvents();
        loadProductFromFirestore();
        loadReviewData();

        updateQuantityText();
        updatePriceText();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        imgProduct = findViewById(R.id.imgProduct);
        txtName = findViewById(R.id.txtName);
        txtDescription = findViewById(R.id.txtDescription);
        txtPrice = findViewById(R.id.txtPrice);
        txtQuantity = findViewById(R.id.txtQuantity);

        txtServiceInfo = findViewById(R.id.txtServiceInfo);
        txtAverageRating = findViewById(R.id.txtAverageRating);

        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnOrderNow = findViewById(R.id.btnOrderNow);

        btnSizeM = findViewById(R.id.btnSizeM);
        btnSizeL = findViewById(R.id.btnSizeL);
        btnTopTranChau = findViewById(R.id.btnTopTranChau);
        btnTopThachDua = findViewById(R.id.btnTopThachDua);
    }

    private void loadProductFromFirestore() {
        String productId = getIntent().getStringExtra("productId");

        if (productId == null || productId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("Store")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        bindProductData(doc);
                    } else {
                        db.collection("Store")
                                .whereEqualTo("Name", productId)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        bindProductData(queryDocumentSnapshots.getDocuments().get(0));
                                    } else {
                                        Toast.makeText(this, "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void bindProductData(DocumentSnapshot doc) {
        String name = doc.getString("Name");
        if (name == null || name.trim().isEmpty()) {
            name = "Sản phẩm";
        }

        String description = doc.getString("description");
        if (description == null || description.trim().isEmpty()) {
            description = "Chưa có mô tả";
        }

        Long priceLong = doc.getLong("price");
        basePrice = priceLong != null ? priceLong.intValue() : 0;

        String imageName = doc.getString("imageName");

        txtName.setText(name);
        txtDescription.setText(description);
        txtServiceInfo.setText("Phục vụ: 5 - 10 phút");

        if (imageName != null && !imageName.trim().isEmpty()) {
            int imgRes = getResources().getIdentifier(imageName, "drawable", getPackageName());
            if (imgRes != 0) {
                imgProduct.setImageResource(imgRes);
            } else {
                imgProduct.setImageResource(R.mipmap.ic_launcher);
            }
        } else {
            imgProduct.setImageResource(R.mipmap.ic_launcher);
        }

        productName = name;
        updatePriceText();
    }

    private void loadReviewData() {
        db.collection("Reviews")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        txtAverageRating.setText("Điểm trung bình: 0.0 ★");
                        return;
                    }

                    double totalRating = 0;
                    int count = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double ratingValue = doc.getDouble("rating");
                        if (ratingValue != null) {
                            totalRating += ratingValue;
                            count++;
                        }
                    }

                    double average = count > 0 ? totalRating / count : 0;
                    txtAverageRating.setText(
                            "Điểm trung bình: " + String.format(Locale.getDefault(), "%.1f", average) + " ★"
                    );
                })
                .addOnFailureListener(e ->
                        txtAverageRating.setText("Điểm trung bình: 0.0 ★")
                );
    }

    private void setupDefaultSelections() {
        selectedSize = "M";
        selectedTopping = "Không";
        sizeExtra = 0;
        toppingExtra = 0;

        setSelectedStyle(btnSizeM, true);
        setSelectedStyle(btnSizeL, false);

        setSelectedStyle(btnTopTranChau, false);
        setSelectedStyle(btnTopThachDua, false);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityText();
                updatePriceText();
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            updateQuantityText();
            updatePriceText();
        });

        btnSizeM.setOnClickListener(v -> {
            selectedSize = "M";
            sizeExtra = 0;
            setSelectedStyle(btnSizeM, true);
            setSelectedStyle(btnSizeL, false);
            updatePriceText();
        });

        btnSizeL.setOnClickListener(v -> {
            selectedSize = "L";
            sizeExtra = 5000;
            setSelectedStyle(btnSizeM, false);
            setSelectedStyle(btnSizeL, true);
            updatePriceText();
        });

        btnTopTranChau.setOnClickListener(v -> {
            selectedTopping = "Trân châu";
            toppingExtra = 3000;
            setSelectedStyle(btnTopTranChau, true);
            setSelectedStyle(btnTopThachDua, false);
            updatePriceText();
        });

        btnTopThachDua.setOnClickListener(v -> {
            selectedTopping = "Thạch dừa";
            toppingExtra = 3000;
            setSelectedStyle(btnTopTranChau, false);
            setSelectedStyle(btnTopThachDua, true);
            updatePriceText();
        });

        btnAddToCart.setOnClickListener(v -> {
            CartItem item = new CartItem(
                    productName,
                    selectedSize,
                    selectedTopping,
                    quantity,
                    basePrice
            );

            CartManager.addToCart(this, item);
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        });

        btnOrderNow.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
            intent.putExtra("productName", productName);
            intent.putExtra("size", selectedSize);
            intent.putExtra("topping", selectedTopping);
            intent.putExtra("quantity", quantity);
            intent.putExtra("unitPrice", getUnitPrice());
            startActivity(intent);
        });
    }

    private int getUnitPrice() {
        return basePrice + sizeExtra + toppingExtra;
    }

    private void updateQuantityText() {
        txtQuantity.setText(String.valueOf(quantity));
    }

    private void updatePriceText() {
        int total = getUnitPrice() * quantity;
        txtPrice.setText(formatter.format(total) + "đ");
    }

    private void setSelectedStyle(Button button, boolean selected) {
        if (selected) {
            button.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.pink_main))
            );
            button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            button.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.pink_light))
            );
            button.setTextColor(ContextCompat.getColor(this, R.color.pink_dark));
        }
    }
}