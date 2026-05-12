package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

public class AdminProductActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ImageButton btnAddProduct;
    private Spinner spCategoryFilter;
    private TextView txtProductCount;

    private FirebaseFirestore db;

    // productList: danh sách đang hiển thị trên RecyclerView
    private final ArrayList<Product> productList = new ArrayList<>();

    // allProductList: danh sách gốc lấy từ Firestore, dùng để lọc theo category
    private final ArrayList<Product> allProductList = new ArrayList<>();

    private final ArrayList<String> categoryOptions = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;
    private String selectedCategory = "Tất cả";

    private AdminProductRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_product);

        db = FirebaseFirestore.getInstance();

        bindViews();
        setupRecyclerView();
        setupCategoryFilter();
        setupActions();
        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void bindViews() {
        rvProducts = findViewById(R.id.rvProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        spCategoryFilter = findViewById(R.id.spCategoryFilter);
        txtProductCount = findViewById(R.id.txtProductCount);
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

    private void setupCategoryFilter() {
        categoryOptions.clear();
        categoryOptions.add("Tất cả");

        categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryOptions
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (spCategoryFilter != null) {
            spCategoryFilter.setAdapter(categoryAdapter);
            spCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < categoryOptions.size()) {
                        selectedCategory = categoryOptions.get(position);
                        applyCategoryFilter();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
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
                    allProductList.clear();

                    for (QueryDocumentSnapshot doc : snaps) {
                        // Store/info là thông tin cửa hàng, không phải sản phẩm
                        if ("info".equalsIgnoreCase(doc.getId())) {
                            continue;
                        }

                        Product product = mapDocumentToProduct(doc);
                        allProductList.add(product);
                    }

                    buildCategoryOptions();
                    applyCategoryFilter();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void buildCategoryOptions() {
        TreeSet<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (Product product : allProductList) {
            String category = product.category;
            if (category != null && !category.trim().isEmpty()) {
                categories.add(category.trim());
            }
        }

        categoryOptions.clear();
        categoryOptions.add("Tất cả");
        categoryOptions.addAll(categories);

        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }

        if (spCategoryFilter != null) {
            int selectedIndex = categoryOptions.indexOf(selectedCategory);
            if (selectedIndex >= 0) {
                spCategoryFilter.setSelection(selectedIndex);
            } else {
                selectedCategory = "Tất cả";
                spCategoryFilter.setSelection(0);
            }
        }
    }

    private void applyCategoryFilter() {
        productList.clear();

        for (Product product : allProductList) {
            String category = product.category != null ? product.category.trim() : "";

            if ("Tất cả".equalsIgnoreCase(selectedCategory)
                    || category.equalsIgnoreCase(selectedCategory)) {
                productList.add(product);
            }
        }

        Collections.sort(productList, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                String c1 = p1.category != null ? p1.category : "";
                String c2 = p2.category != null ? p2.category : "";

                int categoryCompare = c1.compareToIgnoreCase(c2);
                if (categoryCompare != 0) {
                    return categoryCompare;
                }

                String n1 = p1.name != null ? p1.name : "";
                String n2 = p2.name != null ? p2.name : "";
                return n1.compareToIgnoreCase(n2);
            }
        });

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (txtProductCount != null) {
            txtProductCount.setText("Tổng: " + productList.size() + " sản phẩm");
        }
    }

    private Product mapDocumentToProduct(QueryDocumentSnapshot doc) {
        String docId = doc.getId();

        String name = getStringSafe(doc, "Name", docId);
        String category = getStringSafe(doc, "category", "Khác");
        String description = getStringSafe(doc, "description", "");

        int price = getIntSafe(doc, "price", 0);

        String imageUrl = getStringSafe(doc, "imageUrl", "");
        String imageName = getStringSafe(doc, "imageName", "");

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

    private float getFloatSafe(QueryDocumentSnapshot doc, String fieldName, float defaultValue) {
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

    private String getStringSafe(QueryDocumentSnapshot doc, String fieldName, String defaultValue) {
        Object value = doc.get(fieldName);

        if (value instanceof String && !((String) value).trim().isEmpty()) {
            return ((String) value).trim();
        }

        if (value instanceof Number) {
            return String.valueOf(value);
        }

        return defaultValue;
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
