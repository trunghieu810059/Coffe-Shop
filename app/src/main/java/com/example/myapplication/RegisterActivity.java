package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNewUsername, etNewPassword, etConfirmPassword;
    private MaterialButton btnCreateAccount;
    private TextView tvBackLogin;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register);

        db = FirebaseFirestore.getInstance();

        etNewUsername = findViewById(R.id.etNewUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvBackLogin = findViewById(R.id.tvBackLogin);

        btnCreateAccount.setOnClickListener(v -> registerUser());
        tvBackLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String username = etNewUsername.getText().toString().trim();
        String password = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etNewUsername.setError("Nhập username");
            return;
        }

        if (username.contains(" ")) {
            etNewUsername.setError("Username không được chứa khoảng trắng");
            return;
        }

        if (password.isEmpty()) {
            etNewPassword.setError("Nhập mật khẩu");
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Xác nhận mật khẩu");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        db.collection("Users")
                .document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "Username đã tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> user = new HashMap<>();
                    user.put("username", username);
                    user.put("password", password);
                    user.put("role", "user");
                    user.put("fullName", "");
                    user.put("phone", "");
                    user.put("address", "");
                    user.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("Users")
                            .document(username)
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi kiểm tra user: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}