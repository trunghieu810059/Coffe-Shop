package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProductListActivity extends AppCompatActivity {

    private ArrayList<Product> allProducts;
    private ArrayList<Product> filteredProducts;
    private ArrayList<Product> products;
    private ProductAdapter adapter;
    private FirebaseFirestore db;

    private ListView listView;
    private Button btnPrevPage, btnNextPage, btnBackWelcome;
    private LinearLayout btnOpenCart, btnOpenFavorite;
    private TextView tvPageInfo, tvProductCount;
    private EditText edtSearch;

    private int currentPage = 1;
    private final int pageSize = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.listview);

        if (findViewById(R.id.main) != null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();

        listView = findViewById(R.id.listView);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);
        tvPageInfo = findViewById(R.id.tvPageInfo);
        tvProductCount = findViewById(R.id.tvProductCount);
        edtSearch = findViewById(R.id.edtSearch);

        btnBackWelcome = findViewById(R.id.btnBackWelcome);
        btnOpenCart = findViewById(R.id.btnOpenCart);
        btnOpenFavorite = findViewById(R.id.btnOpenFavorite);

        allProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>();
        products = new ArrayList<>();

        adapter = new ProductAdapter(this, products);
        listView.setAdapter(adapter);

        btnBackWelcome.setOnClickListener(v -> finish());

        btnOpenCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, CartActivity.class);
            startActivity(intent);
        });

        btnOpenFavorite.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, FavoriteActivity.class);
            startActivity(intent);
        });

        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                showCurrentPage();
            }
        });

        btnNextPage.setOnClickListener(v -> {
            int totalPages = getTotalPages();
            if (currentPage < totalPages) {
                currentPage++;
                showCurrentPage();
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Product p = (Product) parent.getItemAtPosition(position);

            Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
            intent.putExtra("productId", p.docId);
            startActivity(intent);
        });

        addProductIfMissing("MatchaLatte", "Matcha Latte", 32000, "matchalatte");
        addProductIfMissing("NuocSuoi", "Nước Suối", 10000, "nuocsuoi");

        loadAllFromFirestore();
    }

    private Map<String, Object> createProductMap(String name, int price, String imageName) {
        Map<String, Object> product = new HashMap<>();
        product.put("Name", name);
        product.put("price", price);
        product.put("imageName", imageName);
        product.put("description", "Thức uống thơm ngon, chuẩn vị quán");
        product.put("category", "Việt Nam");
        product.put("rating", 4.8);
        product.put("sold", 120);
        return product;
    }

    private void addProductIfMissing(String docId, String name, int price, String imageName) {
        db.collection("Store").document(docId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Map<String, Object> product = createProductMap(name, price, imageName);

                        db.collection("Store").document(docId).set(product)
                                .addOnSuccessListener(aVoid -> loadAllFromFirestore())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Lỗi thêm " + name + ": " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi kiểm tra " + name + ": " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void loadAllFromFirestore() {
        db.collection("Store")
                .orderBy("Name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProducts.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String docId = doc.getId();

                        String name = doc.getString("Name");
                        if (name == null) name = docId;

                        Long priceLong = doc.getLong("price");
                        int price = (priceLong != null) ? priceLong.intValue() : 0;

                        String imageName = doc.getString("imageName");
                        int imgRes = 0;
                        if (imageName != null) {
                            imgRes = getResources().getIdentifier(imageName, "drawable", getPackageName());
                        }
                        if (imgRes == 0) imgRes = R.mipmap.ic_launcher;

                        String description = doc.getString("description");
                        if (description == null) {
                            description = "Thức uống thơm ngon, chuẩn vị quán";
                        }

                        String category = doc.getString("category");
                        if (category == null) {
                            category = "Việt Nam";
                        }

                        Double ratingDouble = doc.getDouble("rating");
                        float rating = (ratingDouble != null) ? ratingDouble.floatValue() : 4.8f;

                        Long soldLong = doc.getLong("sold");
                        int sold = (soldLong != null) ? soldLong.intValue() : 120;

                        Product product = new Product(
                                docId,
                                name,
                                description,
                                category,
                                price,
                                imgRes,
                                rating,
                                sold
                        );

                        product.isFavorite = FavoriteManager.isFavorite(this, name);
                        allProducts.add(product);
                    }

                    filterProducts(edtSearch.getText().toString().trim());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void filterProducts(String keyword) {
        filteredProducts.clear();

        if (keyword.isEmpty()) {
            filteredProducts.addAll(allProducts);
        } else {
            String searchText = keyword.toLowerCase(Locale.ROOT);

            for (Product product : allProducts) {
                String name = product.name != null ? product.name.toLowerCase(Locale.ROOT) : "";
                String description = product.description != null ? product.description.toLowerCase(Locale.ROOT) : "";
                String category = product.category != null ? product.category.toLowerCase(Locale.ROOT) : "";

                if (name.contains(searchText)
                        || description.contains(searchText)
                        || category.contains(searchText)) {
                    filteredProducts.add(product);
                }
            }
        }

        currentPage = 1;
        showCurrentPage();
        updateProductCount();
    }

    private void showCurrentPage() {
        products.clear();

        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredProducts.size());

        for (int i = start; i < end; i++) {
            products.add(filteredProducts.get(i));
        }

        adapter.notifyDataSetChanged();
        updatePageInfo();
    }

    private void updatePageInfo() {
        int totalPages = getTotalPages();
        if (totalPages == 0) totalPages = 1;

        tvPageInfo.setText("Trang " + currentPage + " / " + totalPages);

        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);

        btnPrevPage.setAlpha(currentPage > 1 ? 1f : 0.5f);
        btnNextPage.setAlpha(currentPage < totalPages ? 1f : 0.5f);
    }

    private void updateProductCount() {
        tvProductCount.setText(filteredProducts.size() + " món");
    }

    private int getTotalPages() {
        if (filteredProducts.isEmpty()) return 1;
        return (int) Math.ceil((double) filteredProducts.size() / pageSize);
    }
}