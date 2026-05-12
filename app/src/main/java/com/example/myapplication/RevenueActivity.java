package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

    // Mặc định xem 7 ngày gần nhất
    private int filterMode = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Không bật EdgeToEdge để tránh layout bị tràn lên status bar nếu XML chưa xử lý inset.
        setContentView(R.layout.activity_revenue);

        bindViews();
        setupRecyclerView();
        setupActions();

        db = FirebaseFirestore.getInstance();
        loadDashboard();
    }

    private void bindViews() {
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
    }

    private void setupRecyclerView() {
        rvRevenueOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RevenueOrderAdapter(this, orders);
        rvRevenueOrders.setAdapter(adapter);
    }

    private void setupActions() {
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
    }

    private void loadDashboard() {
        loadOrdersAndRevenue();
        loadBestSeller();
    }

    private void loadOrdersAndRevenue() {
        Date startDate = getStartDate(filterMode);

        db.collection("Orders")
                .get()
                .addOnSuccessListener(snaps -> {
                    orders.clear();

                    long totalRevenue = 0;
                    int totalOrders = 0;
                    int deliveredCountInFilter = 0;
                    int preparingCount = 0;
                    int cancelledCount = 0;

                    for (QueryDocumentSnapshot doc : snaps) {
                        totalOrders++;

                        String status = getStringSafe(doc, "status", "");

                        if ("PREPARING".equalsIgnoreCase(status)) {
                            preparingCount++;
                        }

                        if ("CANCELLED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
                            cancelledCount++;
                        }

                        if (!"DELIVERED".equalsIgnoreCase(status)) {
                            continue;
                        }

                        Date createdDate = getDateSafe(doc, "createdAt");
                        if (createdDate != null && createdDate.before(startDate)) {
                            continue;
                        }

                        Order order = mapDocumentToOrder(doc);
                        totalRevenue += order.finalAmount;
                        deliveredCountInFilter++;
                        orders.add(order);
                    }

                    Collections.sort(orders, (o1, o2) -> {
                        if (o1.createdAt == null && o2.createdAt == null) return 0;
                        if (o1.createdAt == null) return 1;
                        if (o2.createdAt == null) return -1;
                        return o2.createdAt.compareTo(o1.createdAt);
                    });

                    adapter.notifyDataSetChanged();

                    txtTotalOrders.setText(String.valueOf(totalOrders));
                    txtDeliveredCount.setText(String.valueOf(deliveredCountInFilter));
                    txtPreparingCount.setText(String.valueOf(preparingCount));
                    txtCancelledCount.setText(String.valueOf(cancelledCount));

                    txtRevenueTotal.setText(
                            "Tổng doanh thu: " + formatter.format(totalRevenue) + "đ (" + deliveredCountInFilter + " đơn)"
                    );
                })
                .addOnFailureListener(e -> {
                    resetDashboardValues();
                    Toast.makeText(this, "Lỗi tải doanh thu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private Order mapDocumentToOrder(DocumentSnapshot doc) {
        Order order = new Order();
        order.orderId = doc.getId();
        order.username = getStringSafe(doc, "username", "");
        order.customerName = getStringSafe(doc, "customerName", "");
        order.phone = getStringSafe(doc, "phone", "");
        order.address = getStringSafe(doc, "address", "");
        order.status = getStringSafe(doc, "status", "");
        order.finalAmount = getLongSafe(doc, "finalAmount", 0L);
        order.totalAmount = getLongSafe(doc, "totalAmount", 0L);
        order.createdAt = getDateSafe(doc, "createdAt");
        return order;
    }

    private void loadBestSeller() {
        db.collection("Store")
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Map<String, Object>> products = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snaps) {
                        if ("info".equalsIgnoreCase(doc.getId())) {
                            continue;
                        }

                        String name = getStringSafe(doc, "Name", "");
                        if (name.trim().isEmpty()) {
                            continue;
                        }

                        long sold = getLongSafe(doc, "sold", 0L);

                        Map<String, Object> item = new HashMap<>();
                        item.put("name", name);
                        item.put("sold", sold);
                        products.add(item);
                    }

                    if (products.isEmpty()) {
                        txtBestSeller.setText("Chưa có");
                        return;
                    }

                    Collections.sort(products, (a, b) -> {
                        long soldA = (long) a.get("sold");
                        long soldB = (long) b.get("sold");
                        return Long.compare(soldB, soldA);
                    });

                    StringBuilder result = new StringBuilder();
                    int limit = Math.min(3, products.size());

                    for (int i = 0; i < limit; i++) {
                        Map<String, Object> item = products.get(i);
                        result.append(i + 1)
                                .append(". ")
                                .append(item.get("name"))
                                .append(" (")
                                .append(item.get("sold"))
                                .append(")");

                        if (i < limit - 1) {
                            result.append("\n");
                        }
                    }

                    txtBestSeller.setText(result.toString());
                })
                .addOnFailureListener(e -> txtBestSeller.setText("Chưa có"));
    }

    private void resetDashboardValues() {
        txtTotalOrders.setText("0");
        txtDeliveredCount.setText("0");
        txtPreparingCount.setText("0");
        txtCancelledCount.setText("0");
        txtRevenueTotal.setText("Tổng doanh thu: 0đ");
        orders.clear();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private Date getStartDate(int mode) {
        Calendar cal = Calendar.getInstance();

        if (mode == 1) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }

        cal.add(Calendar.DAY_OF_YEAR, -mode + 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private String getStringSafe(DocumentSnapshot doc, String fieldName, String defaultValue) {
        Object value = doc.get(fieldName);
        if (value == null) return defaultValue;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? defaultValue : text;
    }

    private long getLongSafe(DocumentSnapshot doc, String fieldName, long defaultValue) {
        Object value = doc.get(fieldName);

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            try {
                String cleaned = ((String) value)
                        .replace("đ", "")
                        .replace(",", "")
                        .replace(".", "")
                        .trim();

                if (cleaned.isEmpty()) return defaultValue;
                return Long.parseLong(cleaned);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    private Date getDateSafe(DocumentSnapshot doc, String fieldName) {
        Object value = doc.get(fieldName);

        if (value instanceof Timestamp) {
            return ((Timestamp) value).toDate();
        }

        if (value instanceof Date) {
            return (Date) value;
        }

        return null;
    }
}
