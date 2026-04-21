package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RevenueActivity extends AppCompatActivity {

    private TextView txtRevenueTotal;
    private TextView txtDeliveredCount, txtCancelledCount, txtPreparingCount, txtBestSeller, txtTotalOrders;

    private RecyclerView rvRevenueOrders;
    private Button btnToday, btn7Days, btnThisMonth;

    private final ArrayList<Order> orders = new ArrayList<>();
    private RevenueOrderAdapter adapter;

    private FirebaseFirestore db;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    // filter mặc định: 7 ngày
    private int filterMode = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_revenue);

        txtRevenueTotal = findViewById(R.id.txtRevenueTotal);
        txtDeliveredCount = findViewById(R.id.txtDeliveredCount);
        txtCancelledCount = findViewById(R.id.txtCancelledCount);
        txtPreparingCount = findViewById(R.id.txtPreparingCount);
        txtBestSeller = findViewById(R.id.txtBestSeller);
        txtTotalOrders = findViewById(R.id.txtTotalOrders);

        rvRevenueOrders = findViewById(R.id.rvRevenueOrders);
        btnToday = findViewById(R.id.btnToday);
        btn7Days = findViewById(R.id.btn7Days);
        btnThisMonth = findViewById(R.id.btnThisMonth);

        rvRevenueOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RevenueOrderAdapter(this, orders);
        rvRevenueOrders.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        btnToday.setOnClickListener(v -> {
            filterMode = 1;
            loadDashboard();
        });

        btn7Days.setOnClickListener(v -> {
            filterMode = 7;
            loadDashboard();
        });

        btnThisMonth.setOnClickListener(v -> {
            filterMode = 30;
            loadDashboard();
        });

        loadDashboard();
    }

    private void loadDashboard() {
        Date start = getStartDate(filterMode);
        Timestamp startTimestamp = new Timestamp(start);

        loadDeliveredOrdersAndRevenue(startTimestamp);
        loadPreparingCount();
        loadCancelledCount();
        loadTotalOrders();
        loadBestSeller();
    }
    private void loadTotalOrders() {
        db.collection("Orders")
                .get()
                .addOnSuccessListener(snaps -> {
                    txtTotalOrders.setText(String.valueOf(snaps.size()));
                })
                .addOnFailureListener(e ->
                        txtTotalOrders.setText("0")
                );
    }

    private void loadDeliveredOrdersAndRevenue(Timestamp startTimestamp) {
        db.collection("Orders")
                .whereEqualTo("status", "DELIVERED")
                .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    orders.clear();

                    long totalRevenue = 0;
                    int deliveredCount = 0;

                    for (QueryDocumentSnapshot doc : snaps) {
                        Order o = new Order();
                        o.orderId = doc.getId();

                        Long finalL = doc.getLong("finalAmount");
                        o.finalAmount = finalL != null ? finalL : 0;

                        Long totalL = doc.getLong("totalAmount");
                        o.totalAmount = totalL != null ? totalL : 0;

                        totalRevenue += o.finalAmount;
                        deliveredCount++;

                        orders.add(o);
                    }

                    adapter.notifyDataSetChanged();

                    txtDeliveredCount.setText(String.valueOf(deliveredCount));
                    txtRevenueTotal.setText(
                            "Tổng doanh thu: " + formatter.format(totalRevenue) + "đ (" + deliveredCount + " đơn)"
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải doanh thu: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void loadPreparingCount() {
        db.collection("Orders")
                .whereEqualTo("status", "PREPARING")
                .get()
                .addOnSuccessListener(snaps -> {
                    txtPreparingCount.setText(String.valueOf(snaps.size()));
                })
                .addOnFailureListener(e ->
                        txtPreparingCount.setText("0")
                );
    }

    private void loadCancelledCount() {
        db.collection("Orders")
                .get()
                .addOnSuccessListener(snaps -> {
                    int cancelledCount = 0;

                    for (QueryDocumentSnapshot doc : snaps) {
                        String status = doc.getString("status");
                        if ("CANCELLED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
                            cancelledCount++;
                        }
                    }

                    txtCancelledCount.setText(String.valueOf(cancelledCount));
                })
                .addOnFailureListener(e ->
                        txtCancelledCount.setText("0")
                );
    }

    private void loadBestSeller() {
        db.collection("Store")
                .orderBy("sold", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(snaps -> {
                    if (snaps.isEmpty()) {
                        txtBestSeller.setText("Chưa có");
                        return;
                    }

                    StringBuilder result = new StringBuilder();
                    int rank = 1;

                    for (QueryDocumentSnapshot doc : snaps) {
                        String name = doc.getString("Name");
                        Long sold = doc.getLong("sold");

                        if (name == null || name.trim().isEmpty()) {
                            name = "Chưa có tên";
                        }

                        if (sold == null) sold = 0L;

                        result.append(rank)
                                .append(". ")
                                .append(name)
                                .append(" (")
                                .append(sold)
                                .append(")\n");

                        rank++;
                    }

                    txtBestSeller.setText(result.toString().trim());
                })
                .addOnFailureListener(e ->
                        txtBestSeller.setText("Chưa có")
                );
    }

    private Date getStartDate(int mode) {
        Calendar cal = Calendar.getInstance();

        if (mode == 1) { // hôm nay
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }

        cal.add(Calendar.DAY_OF_YEAR, -mode + 1);
        return cal.getTime();
    }
}