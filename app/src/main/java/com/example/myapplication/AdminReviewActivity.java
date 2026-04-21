package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.util.Locale;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class AdminReviewActivity extends AppCompatActivity {

    private RecyclerView rvReviews;
    private final ArrayList<Review> allReviews = new ArrayList<>();
    private final ArrayList<Review> showingReviews = new ArrayList<>();
    private AdminReviewAdapter adapter;
    private FirebaseFirestore db;
    private EditText edtSearchReview;
    private int currentStarFilter = 0;
    private TextView txtTotalReviews, txtAverageRating, txtFiveStarCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_review);
        edtSearchReview = findViewById(R.id.edtSearchReview);
        txtTotalReviews = findViewById(R.id.txtTotalReviews);
        txtAverageRating = findViewById(R.id.txtAverageRating);
        txtFiveStarCount = findViewById(R.id.txtFiveStarCount);
        rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminReviewAdapter(
                this,
                showingReviews,
                this::confirmDeleteReview,
                this::toggleHiddenReview
        );
        rvReviews.setAdapter(adapter);
        Button btnAll = findViewById(R.id.btnAll);
        Button btn1 = findViewById(R.id.btn1);
        Button btn2 = findViewById(R.id.btn2);
        Button btn3 = findViewById(R.id.btn3);
        Button btn4 = findViewById(R.id.btn4);
        Button btn5 = findViewById(R.id.btn5);

        btnAll.setOnClickListener(v -> applyFilter(0));
        btn1.setOnClickListener(v -> applyFilter(1));
        btn2.setOnClickListener(v -> applyFilter(2));
        btn3.setOnClickListener(v -> applyFilter(3));
        btn4.setOnClickListener(v -> applyFilter(4));
        btn5.setOnClickListener(v -> applyFilter(5));
        edtSearchReview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyCombinedFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        db = FirebaseFirestore.getInstance();

        loadReviews();
    }

    private void loadReviews() {
        // ✅ Collection Reviews (đúng như bạn nói đang có)
        // Nếu field thời gian của bạn tên khác createdAt, đổi tại đây.
        db.collection("Reviews")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    allReviews.clear();
                    showingReviews.clear();

                    for (var doc : snaps) {
                        Review r = new Review();
                        r.id = doc.getId();

                        String username = doc.getString("username");
                        if (username == null) username = doc.getString("userName");
                        r.username = username != null ? username : "User";

                        Double ratingD = doc.getDouble("rating");
                        if (ratingD == null) {
                            Long ratingL = doc.getLong("rating");
                            ratingD = ratingL != null ? ratingL.doubleValue() : 0.0;
                        }
                        r.rating = ratingD != null ? ratingD : 0.0;

                        String comment = doc.getString("comment");
                        if (comment == null) comment = doc.getString("content");
                        r.comment = comment != null ? comment : "";

                        Timestamp t = doc.getTimestamp("createdAt");
                        r.createdAt = t;
                        Boolean hidden = doc.getBoolean("isHidden");
                        r.isHidden = hidden != null && hidden;
                        allReviews.add(r);
                    }

                    // ✅ mặc định hiển thị tất cả
                    showingReviews.addAll(allReviews);
                    adapter.notifyDataSetChanged();
                    updateReviewStats();

                    Toast.makeText(this, "Đã tải " + allReviews.size() + " đánh giá", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải Reviews: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private void applyFilter(int star) {
        currentStarFilter = star;
        applyCombinedFilter();
    }
    private void updateReviewStats() {
        int total = allReviews.size();
        int fiveStarCount = 0;
        double sum = 0;

        for (Review r : allReviews) {
            sum += r.rating;

            int rounded = (int) Math.round(r.rating);
            if (rounded == 5) {
                fiveStarCount++;
            }
        }

        double average = total > 0 ? sum / total : 0;

        txtTotalReviews.setText("Tổng số đánh giá: " + total);
        txtAverageRating.setText("Điểm trung bình: " + String.format(Locale.getDefault(), "%.1f", average) + " ★");
        txtFiveStarCount.setText("Đánh giá 5 sao: " + fiveStarCount);
    }
    private void confirmDeleteReview(Review review) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa đánh giá này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteReview(review))
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void deleteReview(Review review) {
        if (review == null || review.id == null || review.id.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy review để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Reviews")
                .document(review.id)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();
                    loadReviews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi xóa đánh giá: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private void applyCombinedFilter() {
        showingReviews.clear();

        String keyword = edtSearchReview != null
                ? edtSearchReview.getText().toString().trim().toLowerCase(Locale.ROOT)
                : "";

        for (Review r : allReviews) {
            boolean matchStar;
            if (currentStarFilter == 0) {
                matchStar = true;
            } else {
                int rounded = (int) Math.round(r.rating);
                matchStar = rounded == currentStarFilter;
            }

            String username = r.username != null ? r.username.toLowerCase(Locale.ROOT) : "";
            String comment = r.comment != null ? r.comment.toLowerCase(Locale.ROOT) : "";

            boolean matchKeyword = keyword.isEmpty()
                    || username.contains(keyword)
                    || comment.contains(keyword);

            if (matchStar && matchKeyword) {
                showingReviews.add(r);
            }
        }

        adapter.notifyDataSetChanged();
    }
    private void toggleHiddenReview(Review review) {
        if (review == null || review.id == null || review.id.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy review", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean newValue = !review.isHidden;

        db.collection("Reviews")
                .document(review.id)
                .update("isHidden", newValue)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            this,
                            newValue ? "Đã ẩn đánh giá" : "Đã hiện lại đánh giá",
                            Toast.LENGTH_SHORT
                    ).show();
                    loadReviews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi cập nhật trạng thái review: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}