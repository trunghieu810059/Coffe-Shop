package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentSnapshot;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private TextView txtCheckoutProduct, txtCheckoutOption, txtCheckoutTotal, txtDiscountInfo;
    private EditText edtReceiverName, edtReceiverPhone, edtReceiverAddress;
    private EditText edtOrderNote, edtCouponCode;
    private Button btnBackCheckout, btnConfirmOrder, btnApplyCoupon;
    private CheckBox chkSaveDefaultAddress;
    private RadioGroup rgPaymentMethod;

    private FirebaseFirestore db;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    private String productName = "";
    private String size = "M";
    private String topping = "Không";
    private int quantity = 1;
    private int unitPrice = 0;
    private int totalAmount = 0;

    private boolean fromCart = false;
    private ArrayList<CartItem> cartItems;

    private int discountAmount = 0;
    private String appliedCoupon = "";
    private String selectedPaymentMethod = "cash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();

        txtCheckoutProduct = findViewById(R.id.txtCheckoutProduct);
        txtCheckoutOption = findViewById(R.id.txtCheckoutOption);
        txtCheckoutTotal = findViewById(R.id.txtCheckoutTotal);
        txtDiscountInfo = findViewById(R.id.txtDiscountInfo);

        edtReceiverName = findViewById(R.id.edtReceiverName);
        edtReceiverPhone = findViewById(R.id.edtReceiverPhone);
        edtReceiverAddress = findViewById(R.id.edtReceiverAddress);
        edtOrderNote = findViewById(R.id.edtOrderNote);
        edtCouponCode = findViewById(R.id.edtCouponCode);

        btnBackCheckout = findViewById(R.id.btnBackCheckout);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnApplyCoupon = findViewById(R.id.btnApplyCoupon);

        chkSaveDefaultAddress = findViewById(R.id.chkSaveDefaultAddress);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);

        btnBackCheckout.setOnClickListener(v -> finish());

        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCash) {
                selectedPaymentMethod = "cash";
            } else if (checkedId == R.id.rbBanking) {
                selectedPaymentMethod = "banking";
            } else if (checkedId == R.id.rbMomo) {
                selectedPaymentMethod = "momo";
            }
        });

        btnApplyCoupon.setOnClickListener(v -> applyCoupon());

        loadIntentData();
        loadUserProfile();

        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
    }

    private void loadIntentData() {
        fromCart = getIntent().getBooleanExtra("fromCart", false);

        if (fromCart) {
            cartItems = CartManager.getCartList(this);

            if (cartItems == null || cartItems.isEmpty()) {
                txtCheckoutProduct.setText("Không có sản phẩm");
                txtCheckoutOption.setText("Giỏ hàng đang trống");
                totalAmount = 0;
                updateTotalText();
                return;
            }

            StringBuilder productText = new StringBuilder();
            StringBuilder optionText = new StringBuilder();

            totalAmount = 0;

            for (CartItem item : cartItems) {
                productText.append("• ").append(item.name).append("\n");

                optionText.append(item.name)
                        .append(" | Size: ").append(item.size)
                        .append(" | Topping: ").append(item.topping)
                        .append(" | SL: ").append(item.quantity)
                        .append("\n");

                totalAmount += item.totalPrice;
            }

            txtCheckoutProduct.setText(productText.toString().trim());
            txtCheckoutOption.setText(optionText.toString().trim());
            updateTotalText();

        } else {
            productName = getIntent().getStringExtra("productName");
            size = getIntent().getStringExtra("size");
            topping = getIntent().getStringExtra("topping");
            quantity = getIntent().getIntExtra("quantity", 1);
            unitPrice = getIntent().getIntExtra("unitPrice", 0);

            if (productName == null) productName = "Sản phẩm";
            if (size == null) size = "M";
            if (topping == null) topping = "Không";

            totalAmount = unitPrice * quantity;

            txtCheckoutProduct.setText(productName);
            txtCheckoutOption.setText("Size: " + size + " | Topping: " + topping + " | Số lượng: " + quantity);
            updateTotalText();
        }
    }

    private void loadUserProfile() {
        String username = UserSession.getUsername(this);

        db.collection("Users")
                .document(username)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fullName = doc.getString("fullName");
                        String phone = doc.getString("phone");
                        String address = doc.getString("address");

                        if (fullName != null) edtReceiverName.setText(fullName);
                        if (phone != null) edtReceiverPhone.setText(phone);
                        if (address != null) edtReceiverAddress.setText(address);
                    }
                });
    }

    private void applyCoupon() {
        String code = edtCouponCode.getText().toString().trim().toUpperCase();

        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Vouchers")
                .document(code)
                .get()
                .addOnSuccessListener(this::handleVoucherResult)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kiểm tra voucher: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void handleVoucherResult(DocumentSnapshot doc) {
        if (!doc.exists()) {
            discountAmount = 0;
            appliedCoupon = "";
            txtDiscountInfo.setText("Mã giảm giá không tồn tại");
            updateTotalText();
            Toast.makeText(this, "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Boolean isActive = doc.getBoolean("isActive");
        String code = doc.getString("code");
        String type = doc.getString("type");
        Double valueDouble = doc.getDouble("value");
        String description = doc.getString("description");

        if (isActive == null || !isActive) {
            discountAmount = 0;
            appliedCoupon = "";
            txtDiscountInfo.setText("Mã giảm giá hiện không khả dụng");
            updateTotalText();
            Toast.makeText(this, "Voucher đã bị tắt", Toast.LENGTH_SHORT).show();
            return;
        }

        if (code == null || type == null || valueDouble == null) {
            discountAmount = 0;
            appliedCoupon = "";
            txtDiscountInfo.setText("Dữ liệu voucher không hợp lệ");
            updateTotalText();
            Toast.makeText(this, "Voucher lỗi dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        int value = valueDouble.intValue();
        discountAmount = 0;
        appliedCoupon = code;

        if ("percent".equalsIgnoreCase(type)) {
            discountAmount = totalAmount * value / 100;
        } else if ("fixed".equalsIgnoreCase(type)) {
            discountAmount = Math.min(value, totalAmount);
        } else {
            discountAmount = 0;
            appliedCoupon = "";
            txtDiscountInfo.setText("Loại voucher không hợp lệ");
            updateTotalText();
            Toast.makeText(this, "Voucher lỗi kiểu giảm giá", Toast.LENGTH_SHORT).show();
            return;
        }

        String desc = description != null ? description : "";
        txtDiscountInfo.setText(
                "Đã áp dụng mã " + appliedCoupon +
                        ": -" + formatter.format(discountAmount) + "đ" +
                        (desc.isEmpty() ? "" : "\n" + desc)
        );

        updateTotalText();
        Toast.makeText(this, "Áp dụng mã thành công", Toast.LENGTH_SHORT).show();
    }

    private void updateTotalText() {
        int finalTotal = Math.max(0, totalAmount - discountAmount);

        txtCheckoutTotal.setText(
                "Tạm tính: " + formatter.format(totalAmount) + "đ\n" +
                        "Giảm giá: -" + formatter.format(discountAmount) + "đ\n" +
                        "Thanh toán: " + formatter.format(finalTotal) + "đ"
        );
    }

    private void confirmOrder() {
        String username = UserSession.getUsername(this);
        String fullName = edtReceiverName.getText().toString().trim();
        String phone = edtReceiverPhone.getText().toString().trim();
        String address = edtReceiverAddress.getText().toString().trim();
        String orderNote = edtOrderNote.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tên, số điện thoại và địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (chkSaveDefaultAddress.isChecked()) {
            saveUserProfile(username, fullName, phone, address);
        }

        if (fromCart) {
            confirmOrderFromCart(username, fullName, phone, address, orderNote);
        } else {
            confirmSingleOrder(username, fullName, phone, address, orderNote);
        }
    }

    private void saveUserProfile(String username, String fullName, String phone, String address) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("phone", phone);
        userData.put("address", address);

        db.collection("Users")
                .document(username)
                .set(userData, SetOptions.merge());
    }

    private void confirmSingleOrder(String username, String fullName, String phone, String address, String orderNote) {
        int finalTotal = Math.max(0, totalAmount - discountAmount);

        Map<String, Object> order = new HashMap<>();
        order.put("username", username);
        order.put("customerName", fullName);
        order.put("phone", phone);
        order.put("address", address);
        order.put("orderNote", orderNote);
        order.put("paymentMethod", selectedPaymentMethod);
        order.put("couponCode", appliedCoupon);
        order.put("discountAmount", discountAmount);

// ✅ items: để lịch sử không bị gom + dễ mở rộng
        ArrayList<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("name", productName);
        item.put("size", size);
        item.put("topping", topping);
        item.put("quantity", quantity);
        item.put("unitPrice", unitPrice);
        item.put("totalPrice", unitPrice * quantity);
        items.add(item);

        order.put("items", items);

// ✅ tổng tiền
        order.put("totalAmount", totalAmount);
        order.put("finalAmount", finalTotal);

// ✅ trạng thái chuẩn để admin quản lý
        order.put("status", "PLACED"); // đã đặt

// ✅ thời gian chuẩn (để orderBy)
        order.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Orders")
                .add(order)
                .addOnSuccessListener(ref -> {
                    // ✅ gọi popup cảm ơn + mời đánh giá
                    showReviewDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Tạo đơn thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void confirmOrderFromCart(String username, String fullName, String phone, String address, String orderNote) {
        ArrayList<Map<String, Object>> itemMaps = new ArrayList<>();

        for (CartItem item : cartItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", item.name);
            map.put("size", item.size);
            map.put("topping", item.topping);
            map.put("quantity", item.quantity);
            map.put("unitPrice", item.unitPrice);
            map.put("totalPrice", item.totalPrice);
            itemMaps.add(map);
        }

        int finalTotal = Math.max(0, totalAmount - discountAmount);

        Map<String, Object> order = new HashMap<>();
        order.put("username", username);
        order.put("customerName", fullName);
        order.put("phone", phone);
        order.put("address", address);
        order.put("orderNote", orderNote);
        order.put("paymentMethod", selectedPaymentMethod);
        order.put("couponCode", appliedCoupon);
        order.put("discountAmount", discountAmount);
        order.put("items", itemMaps);
        order.put("totalAmount", totalAmount);
        order.put("finalAmount", finalTotal);
        order.put("status", "PLACED");
        order.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("Orders")
                .add(order)
                .addOnSuccessListener(unused -> {
                    for (CartItem item : cartItems) {
                        db.collection("Store")
                                .document(item.name)
                                .update("sold", FieldValue.increment(item.quantity));
                    }

                    CartManager.clearCart(this);
                    Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                    showReviewDialog();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi thanh toán: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void showReviewDialog() {
        String username = UserSession.getUsername(this);

        new AlertDialog.Builder(this)
                .setTitle("Cảm ơn bạn đã đặt hàng")
                .setMessage("Đơn hàng của bạn đã được ghi nhận. Bạn có muốn để lại đánh giá cho cửa hàng không?")
                .setPositiveButton("Đánh giá ngay", (dialog, which) -> {
                    Intent intent = new Intent(CheckoutActivity.this, ReviewStoreActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("fromCheckout", true);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Để sau", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}