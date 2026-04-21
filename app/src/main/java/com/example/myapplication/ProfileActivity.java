package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhone, edtAddress;
    private Button btnBackProfile, btnSaveProfile;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnBackProfile = findViewById(R.id.btnBackProfile);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        db = FirebaseFirestore.getInstance();

        btnBackProfile.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        loadProfile();
    }

    private void loadProfile() {
        String username = UserSession.getUsername(this);
        if (username == null || username.trim().isEmpty()) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db.collection("Users")
                .document(username)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        edtFullName.setText(doc.getString("fullName"));
                        edtPhone.setText(doc.getString("phone"));
                        edtAddress.setText(doc.getString("address"));
                    }
                });
    }

    private void saveProfile() {
        String username = UserSession.getUsername(this);

        if (username == null || username.trim().isEmpty() || "guest".equals(username)) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("fullName", fullName);
        data.put("phone", phone);
        data.put("address", address);

        db.collection("Users")
                .document(username)
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}