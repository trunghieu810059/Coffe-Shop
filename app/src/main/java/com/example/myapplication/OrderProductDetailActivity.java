package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;

public class OrderProductDetailActivity extends AppCompatActivity {

    private Button btnBackOrderProduct;
    private ImageView imgOrderProduct;
    private TextView txtOrderProductName, txtOrderProductCategory, txtOrderProductDescription;
    private TextView txtOrderProductPrice, txtOrderProductSold, txtOrderProductRating, txtOrderProductStatus;

    private FirebaseFirestore db;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_product_detail);

        db = FirebaseFirestore.getInstance();

        btnBackOrderProduct = findViewById(R.id.btnBackOrderProduct);
        imgOrderProduct = findViewById(R.id.imgOrderProduct);
        txtOrderProductName = findViewById(R.id.txtOrderProductName);
        txtOrderProductCategory = findViewById(R.id.txtOrderProductCategory);
        txtOrderProductDescription = findViewById(R.id.txtOrderProductDescription);
        txtOrderProductPrice = findViewById(R.id.txtOrderProductPrice);
        txtOrderProductSold = findViewById(R.id.txtOrderProductSold);
        txtOrderProductRating = findViewById(R.id.txtOrderProductRating);
        txtOrderProductStatus = findViewById(R.id.txtOrderProductStatus);

        btnBackOrderProduct.setOnClickListener(v -> finish());

        String productName = getIntent().getStringExtra("productName");
        String orderStatus = getIntent().getStringExtra("orderStatus");

        if (productName == null || productName.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (orderStatus == null || orderStatus.trim().isEmpty()) {
            orderStatus = "paid";
        }

        txtOrderProductStatus.setText("Trạng thái đơn: " + orderStatus);

        loadProductDetail(productName);
    }

    private void loadProductDetail(String productName) {
        db.collection("Store")
                .document(productName)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String name = doc.getString("Name");
                    if (name == null) name = productName;

                    String description = doc.getString("description");
                    if (description == null) description = "Chưa có mô tả";

                    String category = doc.getString("category");
                    if (category == null) category = "Việt Nam";

                    Long priceLong = doc.getLong("price");
                    int price = priceLong != null ? priceLong.intValue() : 0;

                    Long soldLong = doc.getLong("sold");
                    int sold = soldLong != null ? soldLong.intValue() : 0;

                    Double ratingDouble = doc.getDouble("rating");
                    double rating = ratingDouble != null ? ratingDouble : 0.0;

                    String imageUrl = doc.getString("imageUrl");
                    String imageName = doc.getString("imageName");

                    txtOrderProductName.setText(name);
                    txtOrderProductCategory.setText("Danh mục / nguồn gốc: " + category);
                    txtOrderProductDescription.setText(description);
                    txtOrderProductPrice.setText("Giá hiện tại: " + formatter.format(price) + "đ");
                    txtOrderProductSold.setText("Tổng số đã bán: " + sold);
                    txtOrderProductRating.setText("Đánh giá chung: " + rating + " ★");

                    if (imageName != null && !imageName.trim().isEmpty()) {
                        int imgRes = getResources().getIdentifier(imageName, "drawable", getPackageName());
                        if (imgRes != 0) {
                            imgOrderProduct.setImageResource(imgRes);
                        } else {
                            imgOrderProduct.setImageResource(R.mipmap.ic_launcher);
                        }
                    } else {
                        imgOrderProduct.setImageResource(R.mipmap.ic_launcher);
                    }

                    // Nếu sau này bạn dùng imageUrl + thư viện load ảnh thì thay phần trên bằng Glide/Picasso.
                    // Hiện tại để ổn định trước mắt, vẫn fallback về drawable / launcher.
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}