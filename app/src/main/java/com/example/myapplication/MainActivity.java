package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }


    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Nhập username");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Nhập mật khẩu");
            return;
        }

        db.collection("Users")
                .document(username)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(MainActivity.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String savedPassword = doc.getString("password");
                    String role = doc.getString("role");

                    if (savedPassword == null || !savedPassword.equals(password)) {
                        Toast.makeText(MainActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserSession.saveUsername(MainActivity.this, username);
                    Toast.makeText(MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                    if ("admin".equalsIgnoreCase(role)) {
                        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    }

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this, "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

}