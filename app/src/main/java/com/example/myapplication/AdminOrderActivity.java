package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Locale;

public class AdminOrderActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private EditText edtSearchOrder;
    private Spinner spinnerOrderStatus;

    private final ArrayList<Order> allOrders = new ArrayList<>();
    private final ArrayList<Order> filteredOrders = new ArrayList<>();
    private AdminOrderAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_orders);

        db = FirebaseFirestore.getInstance();

        rvOrders = findViewById(R.id.rvOrders);
        edtSearchOrder = findViewById(R.id.edtSearchOrder);
        spinnerOrderStatus = findViewById(R.id.spinnerOrderStatus);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminOrderAdapter(this, filteredOrders, o -> {
            Intent i = new Intent(AdminOrderActivity.this, AdminOrderDetailActivity.class);
            i.putExtra("orderId", o.orderId);
            startActivity(i);
        });
        rvOrders.setAdapter(adapter);

        setupSpinner();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void setupSpinner() {
        ArrayList<String> statusOptions = new ArrayList<>();
        statusOptions.add("Tất cả");
        statusOptions.add("Đã đặt");
        statusOptions.add("Đang chuẩn bị");
        statusOptions.add("Đã giao");
        statusOptions.add("Đã huỷ");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrderStatus.setAdapter(spinnerAdapter);

        spinnerOrderStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                filterOrders();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupSearch() {
        edtSearchOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadOrders() {
        db.collection("Orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    allOrders.clear();

                    for (QueryDocumentSnapshot doc : snaps) {
                        Order o = new Order();
                        o.orderId = doc.getId();
                        o.username = doc.getString("username");
                        o.customerName = doc.getString("customerName");
                        o.phone = doc.getString("phone");
                        o.address = doc.getString("address");
                        o.status = doc.getString("status");
                        if (o.status == null || o.status.trim().isEmpty()) o.status = "PLACED";

                        Long finalL = doc.getLong("finalAmount");
                        o.finalAmount = finalL != null ? finalL : 0;

                        Long totalL = doc.getLong("totalAmount");
                        o.totalAmount = totalL != null ? totalL : 0;

                        allOrders.add(o);
                    }

                    filterOrders();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải đơn: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void filterOrders() {
        filteredOrders.clear();

        String keyword = edtSearchOrder.getText().toString().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = spinnerOrderStatus.getSelectedItem().toString();

        for (Order o : allOrders) {
            boolean matchStatus = matchStatus(o.status, selectedStatus);
            boolean matchKeyword = matchKeyword(o, keyword);

            if (matchStatus && matchKeyword) {
                filteredOrders.add(o);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private boolean matchStatus(String orderStatus, String selectedStatus) {
        if (selectedStatus.equals("Tất cả")) return true;

        if (selectedStatus.equals("Đã đặt")) {
            return "PLACED".equalsIgnoreCase(orderStatus) || "paid".equalsIgnoreCase(orderStatus);
        }

        if (selectedStatus.equals("Đang chuẩn bị")) {
            return "PREPARING".equalsIgnoreCase(orderStatus);
        }

        if (selectedStatus.equals("Đã giao")) {
            return "DELIVERED".equalsIgnoreCase(orderStatus);
        }

        if (selectedStatus.equals("Đã huỷ")) {
            return "CANCELED".equalsIgnoreCase(orderStatus)
                    || "CANCELLED".equalsIgnoreCase(orderStatus);
        }

        return true;
    }

    private boolean matchKeyword(Order o, String keyword) {
        if (keyword.isEmpty()) return true;

        String shortId = o.orderId != null && o.orderId.length() > 6
                ? o.orderId.substring(0, 6).toLowerCase(Locale.ROOT)
                : (o.orderId != null ? o.orderId.toLowerCase(Locale.ROOT) : "");

        String fullId = o.orderId != null ? o.orderId.toLowerCase(Locale.ROOT) : "";
        String customerName = o.customerName != null ? o.customerName.toLowerCase(Locale.ROOT) : "";
        String phone = o.phone != null ? o.phone.toLowerCase(Locale.ROOT) : "";
        String username = o.username != null ? o.username.toLowerCase(Locale.ROOT) : "";

        return fullId.contains(keyword)
                || shortId.contains(keyword)
                || customerName.contains(keyword)
                || phone.contains(keyword)
                || username.contains(keyword);
    }
}