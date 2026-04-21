package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StoreInfoAdminActivity extends AppCompatActivity {

    private EditText edtShopName, edtPhone, edtEmail, edtAddress;
    private Button btnBackStoreInfo, btnSaveStoreInfo;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_info_admin);

        db = FirebaseFirestore.getInstance();

        btnBackStoreInfo = findViewById(R.id.btnBackStoreInfo);
        edtShopName = findViewById(R.id.edtShopName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtAddress = findViewById(R.id.edtAddress);
        btnSaveStoreInfo = findViewById(R.id.btnSaveStoreInfo);

        loadStoreInfo();

        btnBackStoreInfo.setOnClickListener(v -> finish());

        btnSaveStoreInfo.setOnClickListener(v -> saveStoreInfo());
    }

    private void loadStoreInfo() {
        db.collection("Store")
                .document("info")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        edtShopName.setText(documentSnapshot.getString("name"));
                        edtPhone.setText(documentSnapshot.getString("phone"));
                        edtEmail.setText(documentSnapshot.getString("email"));
                        edtAddress.setText(documentSnapshot.getString("address"));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải thông tin quán: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void saveStoreInfo() {
        String name = edtShopName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty()) {
            edtShopName.setError("Nhập tên cửa hàng");
            return;
        }

        if (phone.isEmpty()) {
            edtPhone.setError("Nhập số điện thoại");
            return;
        }

        if (address.isEmpty()) {
            edtAddress.setError("Nhập địa chỉ");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("email", email);
        data.put("address", address);

        db.collection("Store")
                .document("info")
                .set(data)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Lưu thông tin cửa hàng thành công", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}