package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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
        String customerName = getIntent().getStringExtra("customerName");
        String orderId = getIntent().getStringExtra("orderId");

        if (username == null || username.trim().isEmpty()) {
            username = "User";
        }

        if (customerName == null || customerName.trim().isEmpty()) {
            customerName = username;
        }

        final String finalUsername = username;
        final String finalCustomerName = customerName;
        final String finalOrderId = orderId;

        if (fromCheckout) {
            txtReviewHint.setText("Cảm ơn bạn đã mua hàng. Hãy để lại đánh giá cho cửa hàng nhé!");
        } else {
            txtReviewHint.setText("Hãy chia sẻ cảm nhận của bạn về cửa hàng.");
        }

        btnBackReview.setOnClickListener(v -> finish());

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) ->
                txtRatingValue.setText("Đánh giá của bạn: " + rating + " sao")
        );

        btnSubmitReview.setOnClickListener(v -> submitReview(finalUsername, finalCustomerName, finalOrderId));
    }

    private void submitReview(String username, String customerName, String orderId) {
        float rating = ratingBar.getRating();
        String comment = edtReviewContent.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitReview.setEnabled(false);
        btnSubmitReview.setText("Đang gửi...");

        Map<String, Object> review = new HashMap<>();
        review.put("username", username);
        review.put("customerName", customerName);
        review.put("orderId", orderId);
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("createdAt", Timestamp.now());
        review.put("isHidden", false);
        review.put("adminNote", "");
        review.put("adminReply", "");

        db.collection("Reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Gửi đánh giá thành công", Toast.LENGTH_SHORT).show();
                    btnSubmitReview.setEnabled(true);
                    btnSubmitReview.setText("Gửi đánh giá");

                    Intent intent = new Intent(ReviewStoreActivity.this, ProductListActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubmitReview.setEnabled(true);
                    btnSubmitReview.setText("Gửi đánh giá");
                });
    }
}