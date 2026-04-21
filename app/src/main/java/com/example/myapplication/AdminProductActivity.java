package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.UUID;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;

public class AdminProductActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private FirebaseFirestore db;

    private final ArrayList<Product> productList = new ArrayList<>();
    private AdminProductRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_product);
        ImageButton btnAddProduct = findViewById(R.id.btnAddProduct);
        btnAddProduct.setOnClickListener(v -> {
            Intent i = new Intent(AdminProductActivity.this, AddEditProductActivity.class);
            // docId = null nghĩa là chế độ THÊM MỚI
            i.putExtra("docId", "");
            startActivity(i);
        });
        rvProducts = findViewById(R.id.rvProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminProductRecyclerAdapter(
                this,
                productList,
                product -> {
                    // Mở màn sửa chi tiết
                    Intent i = new Intent(AdminProductActivity.this, AddEditProductActivity.class);
                    i.putExtra("docId", product.docId);
                    i.putExtra("Name", product.name);
                    i.putExtra("category", product.category);
                    i.putExtra("description", product.description);
                    i.putExtra("price", product.price);
                    i.putExtra("imageUrl", product.imageUrl);
                    startActivity(i);
                },
                product -> {
                    // ✅ Xoá có xác nhận
                    new androidx.appcompat.app.AlertDialog.Builder(AdminProductActivity.this)
                            .setTitle("Xoá sản phẩm")
                            .setMessage("Bạn chắc chắn muốn xoá: " + product.name + " ?")
                            .setPositiveButton("Xoá", (d, w) -> deleteProduct(product))
                            .setNegativeButton("Huỷ", null)
                            .show();
                }
        );

        rvProducts.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        //migrateDrawableImagesToStorage(); // up ảnh lên storage (không đc)
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        db.collection("Store")
                .get()
                .addOnSuccessListener(snaps -> {
                    productList.clear();

                    for (QueryDocumentSnapshot doc : snaps) {
                        String docId = doc.getId();

                        String name = doc.getString("Name");
                        if (name == null) name = docId;

                        String category = doc.getString("category");
                        if (category == null) category = "";

                        String description = doc.getString("description");
                        if (description == null) description = "";

                        Long priceLong = doc.getLong("price");
                        int price = priceLong != null ? priceLong.intValue() : 0;

                        String imageUrl = doc.getString("imageUrl");
                        if (imageUrl == null) imageUrl = "";

                        // fallback drawable theo imageName (nếu chưa có imageUrl)
                        String imageName = doc.getString("imageName");
                        int imgRes = 0;
                        if (imageName != null && !imageName.isEmpty()) {
                            imgRes = getResources().getIdentifier(imageName, "drawable", getPackageName());
                        }
                        if (imgRes == 0) imgRes = R.mipmap.ic_launcher;

                        Double ratingD = doc.getDouble("rating");
                        float rating = ratingD != null ? ratingD.floatValue() : 0f;

                        Long soldL = doc.getLong("sold");
                        int sold = soldL != null ? soldL.intValue() : 0;

                        Product p = new Product(docId, name, description, category, price, imgRes, rating, sold);
                        p.imageUrl = imageUrl;

                        productList.add(p);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private void migrateDrawableImagesToStorage() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        db.collection("Store")
                .get()
                .addOnSuccessListener(snaps -> {
                    for (QueryDocumentSnapshot doc : snaps) {
                        String docId = doc.getId();

                        // Nếu đã có imageUrl thì bỏ qua
                        String currentUrl = doc.getString("imageUrl");
                        if (currentUrl != null && !currentUrl.trim().isEmpty()) continue;

                        String imageName = doc.getString("imageName");
                        if (imageName == null || imageName.trim().isEmpty()) {
                            imageName = guessImageNameByDocId(docId);
                        }
                        if (imageName == null || imageName.trim().isEmpty()) continue;
                        int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                        if (resId == 0) continue;

                        try {
                            InputStream is = getResources().openRawResource(resId);

                            // Đặt tên file trên Storage (ổn định theo docId)
                            String safeDocId = docId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
                            String path = "products/" + safeDocId + "/" + imageName + ".webp";

                            StorageReference ref = storage.getReference().child(path);

                            ref.putStream(is)
                                    .addOnSuccessListener(t ->
                                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                                String url = uri.toString();
                                                db.collection("Store").document(docId)
                                                        .update("imageUrl", url)
                                                        .addOnSuccessListener(v -> {
                                                            Log.d("MIGRATE", "OK " + docId + " -> " + url);
                                                            loadProducts(); // ✅ reload để list ưu tiên imageUrl
                                                        })
                                                        .addOnFailureListener(e ->
                                                                Log.e("MIGRATE", "Update Firestore lỗi: " + e.getMessage())
                                                        );
                                            })
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e("MIGRATE", "Upload lỗi " + docId + ": " + e.getMessage())
                                    );

                        } catch (Exception e) {
                            Log.e("MIGRATE", "Lỗi mở drawable: " + e.getMessage());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("MIGRATE", "Lỗi load Store: " + e.getMessage()));
    }
    private String guessImageNameByDocId(String docId) {
        if (docId == null) return "";

        String s = docId.toLowerCase();

        if (s.contains("cafe sữa") || s.contains("cafe sua")) return "milkafe";
        if (s.contains("cafe den")) return "blackcoffee";
        if (s.contains("nước chanh") || s.contains("nuoc chanh")) return "nuocchanh";
        if (s.contains("dưa hấu") || s.contains("dua hau")) return "watermelon";
        if (s.contains("coconut") || s.contains("coco")) return "coconut";

        return "";
    }
    private void deleteProduct(Product p) {
        if (p == null || p.docId == null || p.docId.trim().isEmpty()) {
            Toast.makeText(this, "Không có docId để xoá", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Store")
                .document(p.docId)
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Đã xoá: " + p.name, Toast.LENGTH_SHORT).show();
                    loadProducts();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Xoá lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
