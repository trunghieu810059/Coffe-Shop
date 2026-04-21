package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ReviewHistoryAdapter extends RecyclerView.Adapter<ReviewHistoryAdapter.VH> {

    private final Context context;
    private final ArrayList<Review> list;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

    public ReviewHistoryAdapter(Context context, ArrayList<Review> list) {
        this.context = context;
        this.list = list;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtReviewStatus, txtReviewTime, txtReviewComment, txtAdminReply;
        RatingBar ratingBar;

        VH(@NonNull View itemView) {
            super(itemView);
            txtReviewStatus = itemView.findViewById(R.id.txtReviewStatus);
            txtReviewTime = itemView.findViewById(R.id.txtReviewTime);
            txtReviewComment = itemView.findViewById(R.id.txtReviewComment);
            txtAdminReply = itemView.findViewById(R.id.txtAdminReply);
            ratingBar = itemView.findViewById(R.id.ratingBarReviewHistory);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_review_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Review r = list.get(position);

        holder.ratingBar.setRating((float) r.rating);
        holder.txtReviewComment.setText(r.comment != null ? r.comment : "(Không có nội dung)");

        if (r.createdAt != null) {
            holder.txtReviewTime.setText(sdf.format(r.createdAt.toDate()));
        } else {
            holder.txtReviewTime.setText("");
        }

        holder.txtReviewStatus.setText(r.isHidden ? "Trạng thái: Đã ẩn" : "Trạng thái: Đang hiển thị");

        if (r.adminReply != null && !r.adminReply.trim().isEmpty()) {
            holder.txtAdminReply.setText("Phản hồi từ quán: " + r.adminReply);
            holder.txtAdminReply.setVisibility(View.VISIBLE);
        } else {
            holder.txtAdminReply.setText("");
            holder.txtAdminReply.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}