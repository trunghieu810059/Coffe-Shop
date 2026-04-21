package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class ContactActivity extends AppCompatActivity {

    private TextView tvStoreName, tvStoreAddress, tvStorePhone, tvStoreEmail;
    private Button btnBackContact;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        db = FirebaseFirestore.getInstance();

        tvStoreName = findViewById(R.id.tvStoreName);
        tvStoreAddress = findViewById(R.id.tvStoreAddress);
        tvStorePhone = findViewById(R.id.tvStorePhone);
        tvStoreEmail = findViewById(R.id.tvStoreEmail);
        btnBackContact = findViewById(R.id.btnBackContact);

        btnBackContact.setOnClickListener(v -> finish());

        loadStoreInfo();
    }

    private void loadStoreInfo() {
        db.collection("Store")
                .document("info")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String address = documentSnapshot.getString("address");
                        String phone = documentSnapshot.getString("phone");
                        String email = documentSnapshot.getString("email");

                        tvStoreName.setText("Tên cửa hàng: " + (name != null ? name : ""));
                        tvStoreAddress.setText("Địa chỉ: " + (address != null ? address : ""));
                        tvStorePhone.setText("Số điện thoại: " + (phone != null ? phone : ""));
                        tvStoreEmail.setText("Email: " + (email != null ? email : ""));
                    } else {
                        tvStoreName.setText("Tên cửa hàng: Chưa có thông tin");
                        tvStoreAddress.setText("Địa chỉ: ");
                        tvStorePhone.setText("Số điện thoại: ");
                        tvStoreEmail.setText("Email: ");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải thông tin cửa hàng: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}