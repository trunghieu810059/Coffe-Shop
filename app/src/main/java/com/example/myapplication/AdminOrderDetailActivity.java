package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
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
    private Button btnPlaced, btnPreparing, btnDelivered, btnCancelled, btnConfirmPaid;

    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_order_detail);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null || orderId.trim().isEmpty()) {
            finish();
            return;
        }

        bindViews();
        setupActions();
        loadOrderDetail();
    }

    private void bindViews() {
        txtInfo = findViewById(R.id.txtInfo);
        itemsContainer = findViewById(R.id.itemsContainer);
        btnPlaced = findViewById(R.id.btnPlaced);
        btnPreparing = findViewById(R.id.btnPreparing);
        btnDelivered = findViewById(R.id.btnDelivered);
        btnCancelled = findViewById(R.id.btnCancelled);
        btnConfirmPaid = findViewById(R.id.btnConfirmPaid);
    }

    private void setupActions() {
        btnPlaced.setOnClickListener(v -> confirmUpdateStatus("PLACED"));
        btnPreparing.setOnClickListener(v -> confirmUpdateStatus("PREPARING"));
        btnDelivered.setOnClickListener(v -> confirmUpdateStatus("DELIVERED"));
        btnCancelled.setOnClickListener(v -> confirmUpdateStatus("CANCELLED"));
        btnConfirmPaid.setOnClickListener(v -> confirmMarkAsPaid());
    }

    private void loadOrderDetail() {
        db.collection("Orders")
                .document(orderId)
                .get()
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

                    String paymentMethod = doc.getString("paymentMethod");
                    String paymentStatus = doc.getString("paymentStatus");

                    Long finalAmountLong = doc.getLong("finalAmount");
                    long finalAmount = finalAmountLong != null ? finalAmountLong : 0;

                    String cancelledByName = doc.getString("cancelledByName");
                    String cancelReason = doc.getString("cancelReason");

                    StringBuilder infoBuilder = new StringBuilder();
                    infoBuilder.append("Mã đơn: ").append(orderId)
                            .append("\nKhách: ").append(customerName != null ? customerName : username)
                            .append("\nSĐT: ").append(phone)
                            .append("\nĐịa chỉ: ").append(address)
                            .append("\nTrạng thái: ").append(statusText(status))
                            .append("\nThanh toán: ").append(paymentMethodText(paymentMethod))
                            .append("\nTrạng thái thanh toán: ").append(paymentStatusText(paymentStatus))
                            .append("\nTổng: ").append(formatter.format(finalAmount)).append("đ");

                    if ("CANCELLED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
                        infoBuilder.append("\nNgười hủy: ")
                                .append(cancelledByName != null ? cancelledByName : "Không rõ")
                                .append("\nLý do hủy: ")
                                .append(cancelReason != null ? cancelReason : "Không có lý do");
                    }

                    txtInfo.setText(infoBuilder.toString());
                    updateConfirmPaidButton(paymentMethod, paymentStatus);
                    bindItems(doc.get("items"));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi load đơn: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void updateConfirmPaidButton(String paymentMethod, String paymentStatus) {
        boolean canConfirmPaid =
                ("cash".equalsIgnoreCase(paymentMethod) && "UNPAID".equalsIgnoreCase(paymentStatus))
                        || (("banking".equalsIgnoreCase(paymentMethod) || "momo".equalsIgnoreCase(paymentMethod))
                        && "PENDING".equalsIgnoreCase(paymentStatus));

        btnConfirmPaid.setVisibility(canConfirmPaid ? View.VISIBLE : View.GONE);
    }

    private void bindItems(Object rawItems) {
        itemsContainer.removeAllViews();

        if (!(rawItems instanceof List<?>)) return;

        List<?> list = (List<?>) rawItems;
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?>)) continue;

            Map<?, ?> itemMap = (Map<?, ?>) obj;

            String name = String.valueOf(itemMap.get("name"));
            String size = String.valueOf(itemMap.get("size"));
            String topping = String.valueOf(itemMap.get("topping"));

            int quantity = itemMap.get("quantity") instanceof Number
                    ? ((Number) itemMap.get("quantity")).intValue() : 0;
            int totalPrice = itemMap.get("totalPrice") instanceof Number
                    ? ((Number) itemMap.get("totalPrice")).intValue() : 0;

            TextView itemView = new TextView(this);
            itemView.setText("• " + name + " | Size: " + size + " | Topping: " + topping +
                    "\n  SL: " + quantity + " | Thành tiền: " + formatter.format(totalPrice) + "đ");
            itemView.setTextSize(14);
            itemView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            itemView.setPadding(0, 0, 0, 18);

            itemsContainer.addView(itemView);
        }
    }

    private void confirmUpdateStatus(String newStatus) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận cập nhật")
                .setMessage("Bạn có chắc muốn chuyển đơn hàng sang trạng thái \"" + statusText(newStatus) + "\" không?")
                .setPositiveButton("Xác nhận", (dialog, which) -> updateStatus(newStatus))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void updateStatus(String newStatus) {
        if ("CANCELLED".equals(newStatus)) {
            db.collection("Orders")
                    .document(orderId)
                    .update(
                            "status", "CANCELLED",
                            "cancelledBy", "admin",
                            "cancelledByName", "Quản trị viên",
                            "cancelReason", "Đơn hàng bị huỷ bởi quản trị viên"
                    )
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
            return;
        }

        db.collection("Orders")
                .document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void confirmMarkAsPaid() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận thanh toán")
                .setMessage("Bạn có chắc muốn xác nhận đơn hàng này đã được thanh toán không?")
                .setPositiveButton("Xác nhận", (dialog, which) -> markAsPaid())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void markAsPaid() {
        db.collection("Orders")
                .document(orderId)
                .update("paymentStatus", "PAID")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã xác nhận thanh toán", Toast.LENGTH_SHORT).show();
                    loadOrderDetail();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi xác nhận thanh toán: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String statusText(String status) {
        if (status == null) return "Không rõ";
        if ("paid".equalsIgnoreCase(status)) return "Đã đặt";
        if ("PLACED".equalsIgnoreCase(status)) return "Đã đặt";
        if ("PREPARING".equalsIgnoreCase(status)) return "Đang chuẩn bị";
        if ("DELIVERED".equalsIgnoreCase(status)) return "Đã giao";
        if ("CANCELED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) return "Đã huỷ";
        return status;
    }

    private String paymentMethodText(String paymentMethod) {
        if (paymentMethod == null) return "Không rõ";
        if ("cash".equalsIgnoreCase(paymentMethod)) return "Tiền mặt";
        if ("banking".equalsIgnoreCase(paymentMethod)) return "Chuyển khoản";
        if ("momo".equalsIgnoreCase(paymentMethod)) return "MoMo";
        return paymentMethod;
    }

    private String paymentStatusText(String paymentStatus) {
        if (paymentStatus == null) return "Không rõ";
        if ("PENDING".equalsIgnoreCase(paymentStatus)) return "Chờ xác nhận";
        if ("UNPAID".equalsIgnoreCase(paymentStatus)) return "Chưa thanh toán";
        if ("PAID".equalsIgnoreCase(paymentStatus)) return "Đã thanh toán";
        return paymentStatus;
    }
}