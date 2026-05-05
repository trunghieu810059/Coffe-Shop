package com.example.myapplication;

import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEditProductActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private String docId = "";

    private EditText edtName, edtDescription, edtPrice, edtImageName;
    private Spinner spCategory;
    private Button btnSave, btnBack;

    private String oldName = "";
    private String oldCategory = "";
    private String oldDescription = "";
    private int oldPrice = 0;
    private String oldImageName = "";

    private final String[] categoryOptions = {
            "Cà phê",
            "Trà sữa",
            "Trà trái cây",
            "Matcha / Latte",
            "Nước giải khát"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        docId = getIntent().getStringExtra("docId");
        if (docId == null) docId = "";

        setupUI();
        loadOldDataIfAny();
    }

    private void setupUI() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(0xFFFFF3F7);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(20), dp(16), dp(20));
        scrollView.addView(root);

        edtName = createEditText("Tên món");
        edtDescription = createEditText("Mô tả");
        edtPrice = createEditText("Giá");
        edtPrice.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtImageName = createEditText("Tên ảnh drawable, ví dụ: matchalatte");

        spCategory = new Spinner(this);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryOptions
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.bottomMargin = dp(12);
        spCategory.setLayoutParams(spinnerParams);

        root.addView(edtName);
        root.addView(spCategory);
        root.addView(edtDescription);
        root.addView(edtPrice);
        root.addView(edtImageName);

        btnSave = new Button(this);
        btnSave.setText(docId.isEmpty() ? "Thêm sản phẩm" : "Lưu thay đổi");
        btnSave.setAllCaps(false);
        btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        btnSave.setTextColor(0xFFFFFFFF);
        btnSave.setBackgroundResource(R.drawable.bg_button_pink);

        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
        );
        saveParams.topMargin = dp(20);
        root.addView(btnSave, saveParams);

        btnBack = new Button(this);
        btnBack.setText("Quay lại");
        btnBack.setAllCaps(false);
        btnBack.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        btnBack.setTextColor(0xFF8E3B5F);
        btnBack.setBackgroundResource(R.drawable.bg_form_rounded);

        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
        );
        backParams.topMargin = dp(12);
        root.addView(btnBack, backParams);

        setContentView(scrollView);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private EditText createEditText(String hint) {
        EditText edt = new EditText(this);
        edt.setHint(hint);
        edt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        edt.setBackgroundResource(R.drawable.bg_input_rounded);
        edt.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(12);
        edt.setLayoutParams(params);

        return edt;
    }

    private void loadOldDataIfAny() {
        oldName = getIntent().getStringExtra("Name");
        oldCategory = getIntent().getStringExtra("category");
        oldDescription = getIntent().getStringExtra("description");
        oldPrice = getIntent().getIntExtra("price", 0);
        oldImageName = getIntent().getStringExtra("imageName");

        if (oldName == null) oldName = "";
        if (oldCategory == null) oldCategory = "";
        if (oldDescription == null) oldDescription = "";
        if (oldImageName == null) oldImageName = "";

        edtName.setText(oldName);
        edtDescription.setText(oldDescription);
        edtPrice.setText(oldPrice > 0 ? String.valueOf(oldPrice) : "");
        edtImageName.setText(oldImageName);

        int selectedIndex = 0;
        for (int i = 0; i < categoryOptions.length; i++) {
            if (categoryOptions[i].equalsIgnoreCase(oldCategory)) {
                selectedIndex = i;
                break;
            }
        }
        spCategory.setSelection(selectedIndex);
    }

    private void saveProduct() {
        String name = edtName.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        String description = edtDescription.getText().toString().trim();
        String priceText = edtPrice.getText().toString().trim();
        String imageName = edtImageName.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || priceText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceText);
        } catch (Exception e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (docId.isEmpty()) {
            Map<String, Object> newData = new HashMap<>();
            newData.put("Name", name);
            newData.put("category", category);
            newData.put("description", description);
            newData.put("price", price);
            newData.put("imageName", imageName);
            newData.put("rating", 0.0);
            newData.put("sold", 0);
            newData.put("imageUrl", "");

            String newDocId = makeDocId(name);

            db.collection("Store")
                    .document(newDocId)
                    .set(newData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã thêm sản phẩm", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi thêm sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );

        } else {
            Map<String, Object> updateData = new HashMap<>();

            if (!name.equals(oldName)) {
                updateData.put("Name", name);
            }
            if (!category.equals(oldCategory)) {
                updateData.put("category", category);
            }
            if (!description.equals(oldDescription)) {
                updateData.put("description", description);
            }
            if (price != oldPrice) {
                updateData.put("price", price);
            }
            if (!imageName.equals(oldImageName)) {
                updateData.put("imageName", imageName);
            }

            if (updateData.isEmpty()) {
                Toast.makeText(this, "Không có thay đổi nào để lưu", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("Store")
                    .document(docId)
                    .update(updateData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã cập nhật sản phẩm", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi cập nhật sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }
    }

    private String makeDocId(String name) {
        return name.trim()
                .replace("đ", "d")
                .replace("Đ", "D")
                .replaceAll("[^a-zA-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}