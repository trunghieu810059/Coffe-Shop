package com.example.myapplication;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String orderId;

    private LinearLayout contentLayout;
    private TextView txtTitle, txtHeader, txtTimelineTitle, txtTimelineContent, txtCancelInfo;
    private LinearLayout itemsContainer;
    private Button btnCancelOrder, btnBackDetail;

    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        setupUI();
        loadOrderDetail();
    }

    private void setupUI() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(0xFFFFF3F7);

        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(16), dp(20), dp(16), dp(20));
        scrollView.addView(contentLayout);

        txtTitle = new TextView(this);
        txtTitle.setText("Chi tiết đơn hàng");
        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        txtTitle.setTypeface(null, Typeface.BOLD);
        txtTitle.setTextColor(0xFF8E3B5F);
        txtTitle.setPadding(0, 0, 0, dp(16));
        contentLayout.addView(txtTitle);

        LinearLayout infoCard = new LinearLayout(this);
        infoCard.setOrientation(LinearLayout.VERTICAL);
        infoCard.setBackgroundResource(R.drawable.bg_form_rounded);
        infoCard.setPadding(dp(16), dp(16), dp(16), dp(16));

        txtHeader = new TextView(this);
        txtHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        txtHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        txtHeader.setLineSpacing(0, 1.2f);
        infoCard.addView(txtHeader);
        txtTimelineTitle = new TextView(this);
        txtTimelineTitle.setText("Tiến trình đơn hàng");
        txtTimelineTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        txtTimelineTitle.setTypeface(null, Typeface.BOLD);
        txtTimelineTitle.setTextColor(0xFF8E3B5F);
        txtTimelineTitle.setPadding(0, dp(14), 0, dp(8));
        infoCard.addView(txtTimelineTitle);

        txtTimelineContent = new TextView(this);
        txtTimelineContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        txtTimelineContent.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        txtTimelineContent.setLineSpacing(0, 1.3f);
        txtTimelineContent.setPadding(0, 0, 0, dp(8));
        infoCard.addView(txtTimelineContent);
        txtCancelInfo = new TextView(this);
        txtCancelInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        txtCancelInfo.setTextColor(0xFFB00020);
        txtCancelInfo.setLineSpacing(0, 1.2f);
        txtCancelInfo.setPadding(0, dp(4), 0, dp(8));
        txtCancelInfo.setVisibility(TextView.GONE);
        infoCard.addView(txtCancelInfo);


        itemsContainer = new LinearLayout(this);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        itemsContainer.setPadding(0, dp(14), 0, 0);
        infoCard.addView(itemsContainer);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(20);
        contentLayout.addView(infoCard, cardParams);

        btnCancelOrder = new Button(this);
        btnCancelOrder.setText("Hủy đơn hàng");
        btnCancelOrder.setAllCaps(false);
        btnCancelOrder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        btnCancelOrder.setTypeface(null, Typeface.BOLD);
        btnCancelOrder.setTextColor(0xFFFFFFFF);
        btnCancelOrder.setBackgroundResource(R.drawable.bg_button_pink);

        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
        );
        cancelParams.bottomMargin = dp(12);
        contentLayout.addView(btnCancelOrder, cancelParams);

        btnBackDetail = new Button(this);
        btnBackDetail.setText("Quay lại");
        btnBackDetail.setAllCaps(false);
        btnBackDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        btnBackDetail.setTypeface(null, Typeface.BOLD);
        btnBackDetail.setTextColor(0xFF8E3B5F);
        btnBackDetail.setBackgroundResource(R.drawable.bg_form_rounded);

        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
        );
        contentLayout.addView(btnBackDetail, backParams);

        setContentView(scrollView);

        btnBackDetail.setOnClickListener(v -> finish());
        btnCancelOrder.setOnClickListener(v -> showCancelConfirmDialog());
    }

    private void loadOrderDetail() {
        db.collection("Orders").document(orderId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Không tìm thấy đơn", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String status = doc.getString("status");
                    if (status == null || status.trim().isEmpty()) status = "PLACED";

                    Long finalLong = doc.getLong("finalAmount");
                    int finalAmount = finalLong != null ? finalLong.intValue() : 0;

                    Long totalLong = doc.getLong("totalAmount");
                    int totalAmount = totalLong != null ? totalLong.intValue() : 0;

                    int displayAmount = finalAmount > 0 ? finalAmount : totalAmount;

                    txtHeader.setText(
                            "Mã đơn: " + orderId +
                                    "\nTrạng thái: " + statusText(status) +
                                    "\nTổng tiền: " + formatter.format(displayAmount) + "đ"
                    );
                    txtTimelineContent.setText(buildTimelineText(status));
                    String cancelledByName = doc.getString("cancelledByName");
                    String cancelReason = doc.getString("cancelReason");

                    if ("CANCELLED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
                        String who = cancelledByName != null ? cancelledByName : "Không rõ";
                        String reason = cancelReason != null ? cancelReason : "Không có lý do";

                        txtCancelInfo.setText("Người hủy: " + who + "\nLý do: " + reason);
                        txtCancelInfo.setVisibility(TextView.VISIBLE);
                    } else {
                        txtCancelInfo.setText("");
                        txtCancelInfo.setVisibility(TextView.GONE);
                    }

                    itemsContainer.removeAllViews();

                    TextView sectionTitle = new TextView(this);
                    sectionTitle.setText("Danh sách món");
                    sectionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                    sectionTitle.setTypeface(null, Typeface.BOLD);
                    sectionTitle.setTextColor(0xFF8E3B5F);
                    sectionTitle.setPadding(0, 0, 0, dp(10));
                    itemsContainer.addView(sectionTitle);

                    Object rawItems = doc.get("items");
                    if (rawItems instanceof List<?>) {
                        List<?> list = (List<?>) rawItems;
                        for (Object obj : list) {
                            if (obj instanceof Map<?, ?>) {
                                Map<?, ?> m = (Map<?, ?>) obj;

                                String name = String.valueOf(m.get("name"));
                                String size = String.valueOf(m.get("size"));
                                String topping = String.valueOf(m.get("topping"));
                                int quantity = m.get("quantity") instanceof Number ? ((Number) m.get("quantity")).intValue() : 0;
                                int totalPrice = m.get("totalPrice") instanceof Number ? ((Number) m.get("totalPrice")).intValue() : 0;

                                TextView item = new TextView(this);
                                item.setText(
                                        "• " + name +
                                                "\n  Size: " + size +
                                                "\n  Topping: " + topping +
                                                "\n  Số lượng: " + quantity +
                                                "\n  Thành tiền: " + formatter.format(totalPrice) + "đ"
                                );
                                item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                                item.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                                item.setLineSpacing(0, 1.2f);
                                item.setPadding(0, 0, 0, dp(14));
                                itemsContainer.addView(item);
                            }
                        }
                    }

                    if ("PLACED".equalsIgnoreCase(status)) {
                        btnCancelOrder.setEnabled(true);
                        btnCancelOrder.setAlpha(1f);
                        btnCancelOrder.setText("Hủy đơn hàng");
                    } else {
                        btnCancelOrder.setEnabled(false);
                        btnCancelOrder.setAlpha(0.6f);
                        btnCancelOrder.setText("Không thể hủy đơn");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải chi tiết đơn: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void showCancelConfirmDialog() {
        final android.widget.EditText edtReason = new android.widget.EditText(this);
        edtReason.setHint("Nhập lý do hủy đơn");
        edtReason.setMinLines(2);
        edtReason.setPadding(32, 24, 32, 24);

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Vui lòng nhập lý do hủy đơn hàng")
                .setView(edtReason)
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    String reason = edtReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        reason = "Khách hàng yêu cầu hủy đơn";
                    }
                    cancelOrder(reason);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void cancelOrder(String reason) {
        db.collection("Orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String customerName = doc.getString("customerName");
                    if (customerName == null || customerName.trim().isEmpty()) {
                        customerName = "Khách hàng";
                    }

                    db.collection("Orders")
                            .document(orderId)
                            .update(
                                    "status", "CANCELLED",
                                    "cancelledBy", "customer",
                                    "cancelledByName", customerName,
                                    "cancelReason", reason,
                                    "cancelledAt", FieldValue.serverTimestamp()
                            )
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                                loadOrderDetail();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi hủy đơn: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi đọc thông tin đơn: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String statusText(String s) {
        if (s == null) return "Không rõ";

        if ("paid".equalsIgnoreCase(s)) return "Đã đặt";
        if ("PLACED".equalsIgnoreCase(s)) return "Đã đặt";
        if ("PREPARING".equalsIgnoreCase(s)) return "Đang chuẩn bị";
        if ("DELIVERING".equalsIgnoreCase(s) || "SHIPPING".equalsIgnoreCase(s)) return "Đang giao";
        if ("DELIVERED".equalsIgnoreCase(s)) return "Đã giao";
        if ("CANCELED".equalsIgnoreCase(s) || "CANCELLED".equalsIgnoreCase(s)) return "Đã huỷ";

        return s;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
    private String buildTimelineText(String status) {
        if (status == null || status.trim().isEmpty()) {
            status = "PLACED";
        }

        if ("CANCELLED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
            return "🔴 Đơn hàng đã bị huỷ";
        }

        StringBuilder timeline = new StringBuilder();

        // Bước 1: đã đặt
        timeline.append("✅ Đã đặt");

        // Bước 2: đang chuẩn bị
        if ("PREPARING".equalsIgnoreCase(status)
                || "DELIVERING".equalsIgnoreCase(status)
                || "SHIPPING".equalsIgnoreCase(status)
                || "DELIVERED".equalsIgnoreCase(status)) {
            timeline.append("\n✅ Đang chuẩn bị");
        } else {
            timeline.append("\n⬜ Đang chuẩn bị");
        }

        // Bước 3: đang giao
        if ("DELIVERING".equalsIgnoreCase(status)
                || "SHIPPING".equalsIgnoreCase(status)
                || "DELIVERED".equalsIgnoreCase(status)) {
            timeline.append("\n✅ Đang giao");
        } else {
            timeline.append("\n⬜ Đang giao");
        }

        // Bước 4: đã giao
        if ("DELIVERED".equalsIgnoreCase(status)) {
            timeline.append("\n✅ Đã giao");
        } else {
            timeline.append("\n⬜ Đã giao");
        }

        return timeline.toString();
    }
}