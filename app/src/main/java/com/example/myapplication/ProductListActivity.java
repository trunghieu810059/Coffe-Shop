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

    private final ArrayList<Product> allProducts = new ArrayList<>();
    private final ArrayList<Product> filteredProducts = new ArrayList<>();
    private final ArrayList<Product> products = new ArrayList<>();

    private ProductAdapter adapter;
    private FirebaseFirestore db;

    private ListView listView;
    private Button btnPrevPage, btnNextPage, btnBackWelcome;
    private LinearLayout btnOpenCart, btnOpenFavorite;
    private TextView tvPageInfo, tvProductCount;
    private EditText edtSearch;

    private Button btnCatAll, btnCatCoffee, btnCatMilkTea, btnCatFruitTea, btnCatMatcha, btnCatDrink;

    private int currentPage = 1;
    private final int pageSize = 4;

    private String selectedCategory = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.listview);

        setupInsets();
        db = FirebaseFirestore.getInstance();

        bindViews();
        setupListView();
        setupButtons();

        addProductIfMissing("MatchaLatte", "Matcha Latte", 32000, "matchalatte", "Matcha / Latte");
        addProductIfMissing("NuocSuoi", "Nước Suối", 10000, "nuocsuoi", "Nước giải khát");

        loadAllFromFirestore();
    }

    private void setupInsets() {
        if (findViewById(R.id.main) != null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void bindViews() {
        listView = findViewById(R.id.listView);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);
        tvPageInfo = findViewById(R.id.tvPageInfo);
        tvProductCount = findViewById(R.id.tvProductCount);
        edtSearch = findViewById(R.id.edtSearch);

        btnBackWelcome = findViewById(R.id.btnBackWelcome);
        btnOpenCart = findViewById(R.id.btnOpenCart);
        btnOpenFavorite = findViewById(R.id.btnOpenFavorite);

        btnCatAll = findViewById(R.id.btnCatAll);
        btnCatCoffee = findViewById(R.id.btnCatCoffee);
        btnCatMilkTea = findViewById(R.id.btnCatMilkTea);
        btnCatFruitTea = findViewById(R.id.btnCatFruitTea);
        btnCatMatcha = findViewById(R.id.btnCatMatcha);
        btnCatDrink = findViewById(R.id.btnCatDrink);
    }

    private void setupListView() {
        adapter = new ProductAdapter(this, products);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Product p = (Product) parent.getItemAtPosition(position);
            Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
            intent.putExtra("productId", p.docId);
            startActivity(intent);
        });
    }

    private void setupButtons() {
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
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnCatAll.setOnClickListener(v -> selectCategory("Tất cả"));
        btnCatCoffee.setOnClickListener(v -> selectCategory("Cà phê"));
        btnCatMilkTea.setOnClickListener(v -> selectCategory("Trà sữa"));
        btnCatFruitTea.setOnClickListener(v -> selectCategory("Trà trái cây"));
        btnCatMatcha.setOnClickListener(v -> selectCategory("Matcha / Latte"));
        btnCatDrink.setOnClickListener(v -> selectCategory("Nước giải khát"));

        updateCategoryUI();
    }

    private void selectCategory(String category) {
        selectedCategory = category;
        currentPage = 1;
        updateCategoryUI();
        applyFilters();
    }

    private void updateCategoryUI() {
        resetCategoryStyle(btnCatAll);
        resetCategoryStyle(btnCatCoffee);
        resetCategoryStyle(btnCatMilkTea);
        resetCategoryStyle(btnCatFruitTea);
        resetCategoryStyle(btnCatMatcha);
        resetCategoryStyle(btnCatDrink);

        if ("Tất cả".equals(selectedCategory)) setSelectedCategoryStyle(btnCatAll);
        if ("Cà phê".equals(selectedCategory)) setSelectedCategoryStyle(btnCatCoffee);
        if ("Trà sữa".equals(selectedCategory)) setSelectedCategoryStyle(btnCatMilkTea);
        if ("Trà trái cây".equals(selectedCategory)) setSelectedCategoryStyle(btnCatFruitTea);
        if ("Matcha / Latte".equals(selectedCategory)) setSelectedCategoryStyle(btnCatMatcha);
        if ("Nước giải khát".equals(selectedCategory)) setSelectedCategoryStyle(btnCatDrink);
    }

    private void resetCategoryStyle(Button button) {
        button.setBackgroundResource(R.drawable.bg_chip_outline);
        button.setTextColor(getResources().getColor(R.color.pink_dark));
    }

    private void setSelectedCategoryStyle(Button button) {
        button.setBackgroundResource(R.drawable.bg_button_pink);
        button.setTextColor(getResources().getColor(android.R.color.white));
    }

    private Map<String, Object> createProductMap(String name, int price, String imageName, String category) {
        Map<String, Object> product = new HashMap<>();
        product.put("Name", name);
        product.put("price", price);
        product.put("imageName", imageName);
        product.put("description", "Thức uống thơm ngon, chuẩn vị quán");
        product.put("category", category);
        product.put("rating", 4.8);
        product.put("sold", 120);
        product.put("imageUrl", "");
        return product;
    }

    private void addProductIfMissing(String docId, String name, int price, String imageName, String category) {
        db.collection("Store")
                .document(docId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Map<String, Object> product = createProductMap(name, price, imageName, category);

                        db.collection("Store")
                                .document(docId)
                                .set(product)
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
                        if ("info".equalsIgnoreCase(doc.getId())) {
                            continue;
                        }

                        Product product = mapDocumentToProduct(doc);
                        product.isFavorite = FavoriteManager.isFavorite(this, product.name);
                        allProducts.add(product);
                    }

                    applyFilters();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private Product mapDocumentToProduct(QueryDocumentSnapshot doc) {
        String docId = doc.getId();

        String name = getStringSafe(doc, "Name", docId);
        int price = getIntSafe(doc, "price", 0);

        String imageName = getStringSafe(doc, "imageName", "");

        int imgRes = 0;
        if (!imageName.isEmpty()) {
            imgRes = getResources().getIdentifier(imageName, "drawable", getPackageName());
        }
        if (imgRes == 0) {
            imgRes = R.mipmap.ic_launcher;
        }

        String imageUrl = getStringSafe(doc, "imageUrl", "");
        String description = getStringSafe(doc, "description", "Thức uống thơm ngon, chuẩn vị quán");

        String firestoreCategory = getStringSafe(doc, "category", "");
        String normalizedCategory = normalizeCategory(name, firestoreCategory);

        float rating = getFloatSafe(doc, "rating", 4.8f);
        int sold = getIntSafe(doc, "sold", 120);

        return new Product(
                docId,
                name,
                description,
                normalizedCategory,
                price,
                imgRes,
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

    private String normalizeCategory(String name, String firestoreCategory) {
        if (firestoreCategory != null) {
            String c = firestoreCategory.trim();

            if (c.equalsIgnoreCase("Cà phê")) return "Cà phê";
            if (c.equalsIgnoreCase("Trà sữa")) return "Trà sữa";
            if (c.equalsIgnoreCase("Trà trái cây")) return "Trà trái cây";
            if (c.equalsIgnoreCase("Matcha / Latte")) return "Matcha / Latte";
            if (c.equalsIgnoreCase("Nước giải khát")) return "Nước giải khát";
        }

        String n = name.toLowerCase(Locale.ROOT);

        if (n.contains("cafe") || n.contains("cà phê") || n.contains("bac xiu") || n.contains("bạc xỉu")) {
            return "Cà phê";
        }
        if (n.contains("trà sữa") || n.contains("milk tea")) {
            return "Trà sữa";
        }
        if (n.contains("trà đào") || n.contains("trà chanh") || n.contains("trà tắc") || n.contains("trà vải")) {
            return "Trà trái cây";
        }
        if (n.contains("matcha") || n.contains("latte")) {
            return "Matcha / Latte";
        }
        if (n.contains("nước") || n.contains("soda") || n.contains("chanh") || n.contains("cam")) {
            return "Nước giải khát";
        }

        return "Nước giải khát";
    }

    private void applyFilters() {
        filteredProducts.clear();

        String keyword = edtSearch.getText().toString().trim().toLowerCase(Locale.ROOT);

        for (Product product : allProducts) {
            String name = product.name != null ? product.name.toLowerCase(Locale.ROOT) : "";
            String description = product.description != null ? product.description.toLowerCase(Locale.ROOT) : "";
            String category = product.category != null ? product.category.toLowerCase(Locale.ROOT) : "";

            boolean matchKeyword = keyword.isEmpty()
                    || name.contains(keyword)
                    || description.contains(keyword)
                    || category.contains(keyword);

            boolean matchCategory = selectedCategory.equals("Tất cả")
                    || selectedCategory.equalsIgnoreCase(product.category);

            if (matchKeyword && matchCategory) {
                filteredProducts.add(product);
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