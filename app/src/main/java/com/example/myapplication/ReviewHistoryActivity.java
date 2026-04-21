package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ReviewHistoryActivity extends AppCompatActivity {

    private Button btnBackReviewHistory;
    private TextView txtReviewHistoryTitle;
    private RecyclerView rvReviewHistory;

    private FirebaseFirestore db;
    private final ArrayList<Review> reviewList = new ArrayList<>();
    private ReviewHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_history);

        db = FirebaseFirestore.getInstance();

        btnBackReviewHistory = findViewById(R.id.btnBackReviewHistory);
        txtReviewHistoryTitle = findViewById(R.id.txtReviewHistoryTitle);
        rvReviewHistory = findViewById(R.id.rvReviewHistory);

        rvReviewHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewHistoryAdapter(this, reviewList);
        rvReviewHistory.setAdapter(adapter);

        btnBackReviewHistory.setOnClickListener(v -> finish());

        loadMyReviews();
    }

    private void loadMyReviews() {
        String username = UserSession.getUsername(this);

        if (username == null || username.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Reviews")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(snaps -> {
                    reviewList.clear();

                    for (QueryDocumentSnapshot doc : snaps) {
                        Review r = new Review();
                        r.id = doc.getId();
                        r.username = doc.getString("username");
                        r.customerName = doc.getString("customerName");
                        r.orderId = doc.getString("orderId");

                        Double ratingD = doc.getDouble("rating");
                        if (ratingD == null) {
                            Long ratingL = doc.getLong("rating");
                            ratingD = ratingL != null ? ratingL.doubleValue() : 0.0;
                        }
                        r.rating = ratingD != null ? ratingD : 0.0;

                        String comment = doc.getString("comment");
                        if (comment == null) comment = doc.getString("content");
                        r.comment = comment != null ? comment : "";

                        r.createdAt = doc.getTimestamp("createdAt");

                        Boolean hidden = doc.getBoolean("isHidden");
                        r.isHidden = hidden != null && hidden;

                        if (r.isHidden) {
                            continue;
                        }

                        r.adminNote = doc.getString("adminNote");
                        r.adminReply = doc.getString("adminReply");

                        reviewList.add(r);
                    }

                    java.util.Collections.sort(reviewList, (r1, r2) -> {
                        if (r1.createdAt == null && r2.createdAt == null) return 0;
                        if (r1.createdAt == null) return 1;
                        if (r2.createdAt == null) return -1;
                        return r2.createdAt.compareTo(r1.createdAt);
                    });

                    adapter.notifyDataSetChanged();
                    txtReviewHistoryTitle.setText("Lịch sử đánh giá của bạn (" + reviewList.size() + ")");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải lịch sử đánh giá: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}