package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.VH> {

    public interface OnClick {
        void onClick(Order o);
    }

    private final Context context;
    private final ArrayList<Order> list;
    private final OnClick onClick;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public AdminOrderAdapter(Context context, ArrayList<Order> list, OnClick onClick) {
        this.context = context;
        this.list = list;
        this.onClick = onClick;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtCustomer, txtTotal, txtStatus;
        VH(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtCustomer = itemView.findViewById(R.id.txtCustomer);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Order o = list.get(position);
        String shortId = o.orderId != null && o.orderId.length() > 6 ? o.orderId.substring(0, 6) : o.orderId;

        holder.txtOrderId.setText("Mã đơn: #" + shortId);
        holder.txtCustomer.setText("Khách: " + (o.customerName != null ? o.customerName : o.username));
        holder.txtTotal.setText("Tổng: " + formatter.format(o.finalAmount) + "đ");
        holder.txtStatus.setText("Trạng thái: " + statusText(o.status));

        holder.itemView.setOnClickListener(v -> onClick.onClick(o));
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

    @Override
    public int getItemCount() { return list.size(); }
}