package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ReviewStoreActivity extends AppCompatActivity {

    private Button btnBackReview, btnSubmitReview;
    private RatingBar ratingBar;
    private EditText edtReviewContent;
    private TextView txtRatingValue, txtReviewHint;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_store);

        db = FirebaseFirestore.getInstance();

        btnBackReview = findViewById(R.id.btnBackReview);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        ratingBar = findViewById(R.id.ratingBarStore);
        edtReviewContent = findViewById(R.id.edtReviewContent);
        txtRatingValue = findViewById(R.id.txtRatingValue);
        txtReviewHint = findViewById(R.id.txtReviewHint);

        boolean fromCheckout = getIntent().getBooleanExtra("fromCheckout", false);
        String username = getIntent().getStringExtra("username");
        if (username == null || username.trim().isEmpty()) {
            username = "User";
        }
        final String finalUsername = username;

        if (fromCheckout) {
            txtReviewHint.setText("Cảm ơn bạn đã mua hàng. Hãy để lại đánh giá cho cửa hàng nhé!");
        } else {
            txtReviewHint.setText("Hãy chia sẻ cảm nhận của bạn về cửa hàng.");
        }

        btnBackReview.setOnClickListener(v -> finish());

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            txtRatingValue.setText("Đánh giá của bạn: " + rating + " sao");
        });

        btnSubmitReview.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String content = edtReviewContent.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(ReviewStoreActivity.this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            if (content.isEmpty()) {
                Toast.makeText(ReviewStoreActivity.this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSubmitReview.setEnabled(false);
            btnSubmitReview.setText("Đang gửi...");

            Map<String, Object> review = new HashMap<>();
            review.put("username", finalUsername);
            review.put("rating", rating);
            review.put("content", content);
            review.put("createdAt", Timestamp.now());

            db.collection("Reviews")
                    .add(review)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(ReviewStoreActivity.this, "Gửi đánh giá thành công", Toast.LENGTH_SHORT).show();

                        btnSubmitReview.setEnabled(true);
                        btnSubmitReview.setText("Gửi đánh giá");

                        Intent intent = new Intent(ReviewStoreActivity.this, ProductListActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ReviewStoreActivity.this, "Lỗi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_LONG).show();

                        btnSubmitReview.setEnabled(true);
                        btnSubmitReview.setText("Gửi đánh giá");
                    });
        });
    }
}