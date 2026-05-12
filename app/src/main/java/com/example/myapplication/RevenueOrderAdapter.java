package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RevenueOrderAdapter extends RecyclerView.Adapter<RevenueOrderAdapter.VH> {

    private final Context context;
    private final ArrayList<Order> list;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

    public RevenueOrderAdapter(Context context, ArrayList<Order> list) {
        this.context = context;
        this.list = list;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtTime, txtAmount;

        VH(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtAmount = itemView.findViewById(R.id.txtAmount);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_revenue_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Order order = list.get(position);

        String orderId = order.orderId != null ? order.orderId : "";
        String shortId = orderId.length() > 6 ? orderId.substring(0, 6) : orderId;
        if (shortId.trim().isEmpty()) shortId = "------";

        holder.txtOrderId.setText("Mã đơn: #" + shortId);

        if (order.createdAt != null) {
            holder.txtTime.setText("Thời gian: " + sdf.format(order.createdAt));
        } else {
            holder.txtTime.setText("Thời gian: Chưa có");
        }

        holder.txtAmount.setText("+" + formatter.format(order.finalAmount) + "đ");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
