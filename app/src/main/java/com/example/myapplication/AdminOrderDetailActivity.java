package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class AdminOrderDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String orderId;

    private TextView txtInfo;
    private LinearLayout itemsContainer;
    private Button btnPlaced, btnPreparing, btnDelivered, btnCancelled;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_order_detail);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) { finish(); return; }

        txtInfo = findViewById(R.id.txtInfo);
        itemsContainer = findViewById(R.id.itemsContainer);
        btnPlaced = findViewById(R.id.btnPlaced);
        btnPreparing = findViewById(R.id.btnPreparing);
        btnDelivered = findViewById(R.id.btnDelivered);
        btnCancelled = findViewById(R.id.btnCancelled);

        btnPlaced.setOnClickListener(v -> confirmUpdateStatus("PLACED"));
        btnPreparing.setOnClickListener(v -> confirmUpdateStatus("PREPARING"));
        btnDelivered.setOnClickListener(v -> confirmUpdateStatus("DELIVERED"));
        btnCancelled.setOnClickListener(v -> confirmUpdateStatus("CANCELLED"));
        loadOrderDetail();
    }

    private void loadOrderDetail() {
        db.collection("Orders").document(orderId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Không tìm thấy đơn", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String username = doc.getString("username");
                    String customerName = doc.getString("customerName");
                    String phone = doc.getString("phone");
                    String address = doc.getString("address");
                    String status = doc.getString("status");
                    if (status == null) status = "PLACED";

                    Long finalL = doc.getLong("finalAmount");
                    long finalAmount = finalL != null ? finalL : 0;
                    String cancelledByName = doc.getString("cancelledByName");
                    String cancelReason = doc.getString("cancelReason");

                    String infoText =
                            "Mã đơn: " + orderId +
                                    "\nKhách: " + (customerName != null ? customerName : username) +
                                    "\nSĐT: " + phone +
                                    "\nĐịa chỉ: " + address +
                                    "\nTrạng thái: " + statusText(status) +
                                    "\nTổng: " + formatter.format(finalAmount) + "đ";

                    if ("CANCELLED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
                        infoText +=
                                "\nNgười hủy: " + (cancelledByName != null ? cancelledByName : "Không rõ") +
                                        "\nLý do hủy: " + (cancelReason != null ? cancelReason : "Không có lý do");
                    }

                    txtInfo.setText(infoText);

                    itemsContainer.removeAllViews();

                    Object rawItems = doc.get("items");
                    if (rawItems instanceof List<?>) {
                        List<?> list = (List<?>) rawItems;
                        for (Object obj : list) {
                            if (obj instanceof Map<?, ?>) {
                                Map<?, ?> m = (Map<?, ?>) obj;

                                String name = String.valueOf(m.get("name"));
                                String size = String.valueOf(m.get("size"));
                                String topping = String.valueOf(m.get("topping"));

                                int quantity = m.get("quantity") instanceof Number ? ((Number)m.get("quantity")).intValue() : 0;
                                int totalPrice = m.get("totalPrice") instanceof Number ? ((Number)m.get("totalPrice")).intValue() : 0;

                                TextView item = new TextView(this);
                                item.setText("• " + name + " | Size: " + size + " | Topping: " + topping +
                                        "\n  SL: " + quantity + " | Thành tiền: " + formatter.format(totalPrice) + "đ");
                                item.setTextSize(14);
                                item.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                                item.setPadding(0, 0, 0, 18);
                                itemsContainer.addView(item);
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi load đơn: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void updateStatus(String newStatus) {
        if ("CANCELLED".equals(newStatus)) {
            db.collection("Orders").document(orderId)
                    .update(
                            "status", "CANCELLED",
                            "cancelledBy", "admin",
                            "cancelledByName", "Quản trị viên",
                            "cancelReason", "Đơn hàng bị huỷ bởi quản trị viên"
                    )
                    .addOnSuccessListener(r -> {
                        Toast.makeText(this, "Đã cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        } else {
            db.collection("Orders").document(orderId)
                    .update("status", newStatus)
                    .addOnSuccessListener(r -> {
                        Toast.makeText(this, "Đã cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }
    }

    private String statusText(String s) {
        if (s == null) return "Không rõ";

        if ("paid".equalsIgnoreCase(s)) return "Đã đặt";
        if ("PLACED".equalsIgnoreCase(s)) return "Đã đặt";
        if ("PREPARING".equalsIgnoreCase(s)) return "Đang chuẩn bị";
        if ("DELIVERED".equalsIgnoreCase(s)) return "Đã giao";
        if ("CANCELED".equalsIgnoreCase(s)) return "Đã huỷ";
        if ("CANCELLED".equalsIgnoreCase(s)) return "Đã huỷ";

        return s;
    }
    private void confirmUpdateStatus(String newStatus) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận cập nhật")
                .setMessage("Bạn có chắc muốn chuyển đơn hàng sang trạng thái \"" + statusText(newStatus) + "\" không?")
                .setPositiveButton("Xác nhận", (dialog, which) -> updateStatus(newStatus))
                .setNegativeButton("Huỷ", null)
                .show();
    }
}