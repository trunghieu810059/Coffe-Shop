package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddEditProductActivity extends AppCompatActivity {

    private EditText edtName, edtDescription, edtCategory, edtPrice;
    private Button btnChooseImage, btnSaveProduct, btnDeleteProduct, btnBackAddEdit;
    private ImageView imgPreview;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri selectedImageUri = null;
    private String oldDocId = "";
    private String oldImageUrl = "";

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgPreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_product);

        edtName = findViewById(R.id.edtName);
        edtDescription = findViewById(R.id.edtDescription);
        edtCategory = findViewById(R.id.edtCategory);
        edtPrice = findViewById(R.id.edtPrice);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        btnDeleteProduct = findViewById(R.id.btnDeleteProduct);
        btnBackAddEdit = findViewById(R.id.btnBackAddEdit);
        imgPreview = findViewById(R.id.imgPreview);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        oldDocId = getIntent().getStringExtra("docId");
        if (oldDocId == null) oldDocId = "";

        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        String category = getIntent().getStringExtra("category");
        int price = getIntent().getIntExtra("price", 0);
        oldImageUrl = getIntent().getStringExtra("imageUrl");
        if (oldImageUrl == null) oldImageUrl = "";
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(oldImageUrl).into(imgPreview);
        }

        edtName.setText(name == null ? "" : name);
        edtDescription.setText(description == null ? "" : description);
        edtCategory.setText(category == null ? "" : category);
        edtPrice.setText(price == 0 ? "" : String.valueOf(price));

        btnBackAddEdit.setOnClickListener(v -> finish());

        btnChooseImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSaveProduct.setOnClickListener(v -> saveProduct());

        btnDeleteProduct.setOnClickListener(v -> deleteProduct());
    }

    private void saveProduct() {
        String name = edtName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String category = edtCategory.getText().toString().trim();
        String priceText = edtPrice.getText().toString().trim();

        if (name.isEmpty() || priceText.isEmpty()) {
            Toast.makeText(this, "Nhập tên sản phẩm và giá", Toast.LENGTH_SHORT).show();
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String docId = oldDocId.isEmpty() ? name : oldDocId;

        if (selectedImageUri != null) {
            uploadImageThenSave(docId, name, description, category, price);
        } else {
            saveProductToFirestore(docId, name, description, category, price, oldImageUrl);
        }
    }

    private void uploadImageThenSave(String docId, String name, String description, String category, int price) {
        String fileName = "product_images/" + UUID.randomUUID() + ".jpg";
        StorageReference imageRef = storage.getReference().child(fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                saveProductToFirestore(docId, name, description, category, price, uri.toString())
                        )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void saveProductToFirestore(String docId, String name, String description, String category, int price, String imageUrl) {
        Map<String, Object> product = new HashMap<>();
        product.put("Name", name);
        product.put("description", description);
        product.put("category", category);
        product.put("price", price);
        product.put("imageUrl", imageUrl == null ? "" : imageUrl);
        product.put("rating", 4.8);
        product.put("sold", 0);

        db.collection("Store").document(docId)
                .set(product)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Lưu sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lưu sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void deleteProduct() {
        String docId = oldDocId.isEmpty()
                ? edtName.getText().toString().trim()
                : oldDocId;

        if (docId.isEmpty()) {
            Toast.makeText(this, "Chưa có sản phẩm để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Store").document(docId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}