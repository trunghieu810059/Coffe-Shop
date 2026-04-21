package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class OrderHistoryActivity extends AppCompatActivity {

    private Button btnBackOrderHistory;
    private LinearLayout orderHistoryContainer;
    private FirebaseFirestore db;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);

        btnBackOrderHistory = findViewById(R.id.btnBackOrderHistory);
        orderHistoryContainer = findViewById(R.id.orderHistoryContainer);
        db = FirebaseFirestore.getInstance();

        btnBackOrderHistory.setOnClickListener(v -> finish());

        loadOrderHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrderHistory();
    }

    private void loadOrderHistory() {
        String username = UserSession.getUsername(this);

        if (username == null || username.trim().isEmpty()) {
            showTextOnly("Không có username trong session! Hãy đăng nhập lại.");
            return;
        }

        // ✅ Query chuẩn: field "username"
        queryOrdersByField("username", username, true);
    }

    /**
     * Query Orders theo fieldName (username hoặc Username),
     * nếu empty và allowFallback=true thì tự fallback sang fieldName khác.
     */
    private void queryOrdersByField(String fieldName, String username, boolean allowFallback) {
        db.collection("Orders")
                .whereEqualTo(fieldName, username)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    // Nếu không có kết quả và đang cho phép fallback,
                    // thử query lại với field "Username" (lỡ trước đó lưu sai chữ hoa)
                    if (snaps.isEmpty() && allowFallback && "username".equals(fieldName)) {
                        queryOrdersByField("Username", username, false);
                        return;
                    }

                    orderHistoryContainer.removeAllViews();

                    if (snaps.isEmpty()) {
                        showTextOnly("Chưa có đơn hàng nào (username=" + username + ")");
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snaps) {
                        renderOneOrderBox(doc);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi load lịch sử: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private void renderOneOrderBox(QueryDocumentSnapshot doc) {
        String orderId = doc.getId();

        Long finalLong = doc.getLong("finalAmount");
        int finalAmount = finalLong != null ? finalLong.intValue() : 0;

        Long totalLong = doc.getLong("totalAmount");
        int totalAmount = totalLong != null ? totalLong.intValue() : 0;

        String status = doc.getString("status");
        if (status == null || status.trim().isEmpty()) status = "PLACED";

        // --- Box đơn hàng ---
        LinearLayout orderBox = new LinearLayout(this);
        orderBox.setOrientation(LinearLayout.VERTICAL);
        orderBox.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 20);
        orderBox.setLayoutParams(params);
        orderBox.setBackgroundResource(R.drawable.bg_form_rounded);

        // Click vào đơn -> mở chi tiết đơn
        orderBox.setOnClickListener(v -> {
            Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });

        // --- Header ---
        TextView txtHeader = new TextView(this);
        String shortId = orderId.length() > 6 ? orderId.substring(0, 6) : orderId;

        // ưu tiên finalAmount, nếu finalAmount=0 thì fallback totalAmount
        int displayAmount = finalAmount > 0 ? finalAmount : totalAmount;

        txtHeader.setText(
                "Mã đơn: #" + shortId +
                        "\n" + statusLabel(status) +
                        "\nTổng tiền: " + formatter.format(displayAmount) + "đ"
        );
        txtHeader.setTextSize(16);
        txtHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        txtHeader.setPadding(0, 0, 0, 18);
        orderBox.addView(txtHeader);

        // --- Items ---
        Object rawItems = doc.get("items");
        if (rawItems instanceof List<?>) {
            List<?> tempList = (List<?>) rawItems;

            for (Object obj : tempList) {
                if (obj instanceof Map<?, ?>) {
                    Map<?, ?> rawMap = (Map<?, ?>) obj;

                    String productName = String.valueOf(rawMap.get("name"));
                    String size = String.valueOf(rawMap.get("size"));
                    String topping = String.valueOf(rawMap.get("topping"));

                    Object quantityObj = rawMap.get("quantity");
                    int quantity = quantityObj instanceof Number ? ((Number) quantityObj).intValue() : 0;

                    Object totalPriceObj = rawMap.get("totalPrice");
                    int totalPrice = totalPriceObj instanceof Number ? ((Number) totalPriceObj).intValue() : 0;

                    TextView txtItem = new TextView(this);
                    txtItem.setText(
                            "• " + productName +
                                    "\n  Size: " + size +
                                    "\n  Topping: " + topping +
                                    "\n  Số lượng: " + quantity +
                                    "\n  Thành tiền: " + formatter.format(totalPrice) + "đ"
                    );
                    txtItem.setTextSize(14);
                    txtItem.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                    txtItem.setPadding(0, 0, 0, 18);

                    orderBox.addView(txtItem);
                }
            }
        } else {
            // Nếu đơn cũ chưa có items
            TextView tv = new TextView(this);
            tv.setText("(Đơn này chưa có items)");
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            orderBox.addView(tv);
        }

        orderHistoryContainer.addView(orderBox);
    }

    private void showTextOnly(String message) {
        orderHistoryContainer.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextSize(16);
        tv.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        orderHistoryContainer.addView(tv);
    }

    private String statusText(String s) {
        if (s == null) return "Không rõ";

        if ("paid".equalsIgnoreCase(s)) return "Đã đặt";
        if ("PLACED".equalsIgnoreCase(s)) return "Đã đặt";
        if ("DELIVERED".equalsIgnoreCase(s)) return "Đã giao";
        if ("CANCELED".equalsIgnoreCase(s)) return "Đã huỷ";
        if ("CANCELLED".equalsIgnoreCase(s)) return "Đã huỷ";
        if ("PREPARING".equalsIgnoreCase(s)) return "Đang chuẩn bị";

        return s;
    }
    private String statusLabel(String s) {
        if (s == null) return "Trạng thái: Không rõ";

        if ("paid".equalsIgnoreCase(s)) return "Trạng thái: 🟡 Đã đặt";
        if ("PLACED".equalsIgnoreCase(s)) return "Trạng thái: 🟡 Đã đặt";
        if ("DELIVERED".equalsIgnoreCase(s)) return "Trạng thái: 🟢 Đã giao";
        if ("CANCELED".equalsIgnoreCase(s)) return "Trạng thái: 🔴 Đã huỷ";
        if ("CANCELLED".equalsIgnoreCase(s)) return "Trạng thái: 🔴 Đã huỷ";
        if ("PREPARING".equalsIgnoreCase(s)) return "Trạng thái: 🟠 Đang chuẩn bị";

        return "Trạng thái: " + s;
    }
}