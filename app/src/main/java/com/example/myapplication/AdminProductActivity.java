package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminProductActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ImageButton btnAddProduct;

    private FirebaseFirestore db;
    private final ArrayList<Product> productList = new ArrayList<>();
    private AdminProductRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_product);

        db = FirebaseFirestore.getInstance();

        bindViews();
        setupRecyclerView();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void bindViews() {
        rvProducts = findViewById(R.id.rvProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminProductRecyclerAdapter(
                this,
                productList,
                this::openEditProductScreen,
                this::confirmDeleteProduct
        );

        rvProducts.setAdapter(adapter);
    }

    private void setupActions() {
        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProductActivity.this, AddEditProductActivity.class);
            intent.putExtra("docId", "");
            startActivity(intent);
        });
    }

    private void openEditProductScreen(Product product) {
        if (product == null) return;

        Intent intent = new Intent(AdminProductActivity.this, AddEditProductActivity.class);
        intent.putExtra("docId", product.docId);
        intent.putExtra("Name", product.name);
        intent.putExtra("category", product.category);
        intent.putExtra("description", product.description);
        intent.putExtra("price", product.price);
        intent.putExtra("imageName", product.imageName);
        intent.putExtra("imageUrl", product.imageUrl);
        startActivity(intent);
    }

    private void confirmDeleteProduct(Product product) {
        if (product == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Xoá sản phẩm")
                .setMessage("Bạn chắc chắn muốn xoá: " + product.name + " ?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void loadProducts() {
        db.collection("Store")
                .get()
                .addOnSuccessListener(snaps -> {
                    productList.clear();

                    for (QueryDocumentSnapshot doc : snaps) {
                        Product product = mapDocumentToProduct(doc);
                        productList.add(product);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private Product mapDocumentToProduct(QueryDocumentSnapshot doc) {
        String docId = doc.getId();

        String name = doc.getString("Name");
        if (name == null || name.trim().isEmpty()) {
            name = docId;
        }

        String category = doc.getString("category");
        if (category == null) category = "";

        String description = doc.getString("description");
        if (description == null) description = "";

        Long priceLong = doc.getLong("price");
        int price = priceLong != null ? priceLong.intValue() : 0;

        String imageUrl = doc.getString("imageUrl");
        if (imageUrl == null) imageUrl = "";

        String imageName = doc.getString("imageName");
        if (imageName == null) imageName = "";

        int imageResId = 0;
        if (!imageName.isEmpty()) {
            imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        }
        if (imageResId == 0) {
            imageResId = R.mipmap.ic_launcher;
        }

        Double ratingDouble = doc.getDouble("rating");
        float rating = ratingDouble != null ? ratingDouble.floatValue() : 0f;

        Long soldLong = doc.getLong("sold");
        int sold = soldLong != null ? soldLong.intValue() : 0;

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

    private void deleteProduct(Product product) {
        if (product == null || product.docId == null || product.docId.trim().isEmpty()) {
            Toast.makeText(this, "Không có docId để xoá", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Store")
                .document(product.docId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã xoá: " + product.name, Toast.LENGTH_SHORT).show();
                    loadProducts();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Xoá lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}