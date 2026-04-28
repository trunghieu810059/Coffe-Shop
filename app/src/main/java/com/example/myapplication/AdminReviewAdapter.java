package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.VH> {

    public interface OnDeleteReviewListener {
        void onDeleteReview(Review review);
    }

    public interface OnToggleHiddenListener {
        void onToggleHidden(Review review);
    }
    public interface OnReplyReviewListener {
        void onReplyReview(Review review);
    }

    private final Context context;
    private final ArrayList<Review> list;
    private final OnDeleteReviewListener deleteListener;
    private final OnToggleHiddenListener toggleHiddenListener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
    private final OnReplyReviewListener replyListener;

    public AdminReviewAdapter(
            Context context,
            ArrayList<Review> list,
            OnDeleteReviewListener deleteListener,
            OnToggleHiddenListener toggleHiddenListener,
            OnReplyReviewListener replyListener
    ) {
        this.context = context;
        this.list = list;
        this.deleteListener = deleteListener;
        this.toggleHiddenListener = toggleHiddenListener;
        this.replyListener = replyListener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtUser, txtTime, txtComment, txtReviewStatus;
        RatingBar ratingBar;
        Button btnDeleteReview, btnToggleHidden;
        Button btnReplyReview;

        VH(@NonNull View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.txtUser);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtReviewStatus = itemView.findViewById(R.id.txtReviewStatus);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnToggleHidden = itemView.findViewById(R.id.btnToggleHidden);
            btnDeleteReview = itemView.findViewById(R.id.btnDeleteReview);
            btnReplyReview = itemView.findViewById(R.id.btnReplyReview);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_review, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Review review = list.get(position);

        holder.txtUser.setText(review.username != null ? review.username : "User");
        holder.txtComment.setText(review.comment != null ? review.comment : "(Không có nội dung)");
        holder.ratingBar.setRating((float) review.rating);

        if (review.createdAt != null) {
            holder.txtTime.setText(sdf.format(review.createdAt.toDate()));
        } else {
            holder.txtTime.setText("");
        }

        int roundedRating = (int) Math.round(review.rating);
        if (roundedRating <= 2) {
            holder.itemView.setBackgroundColor(0xFFFFEBEE);
            holder.txtUser.setTextColor(0xFFB00020);
            holder.txtComment.setTextColor(0xFFB00020);
        } else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF);
            holder.txtUser.setTextColor(0xFF000000);
            holder.txtComment.setTextColor(0xFF000000);
        }

        if (review.isHidden) {
            holder.txtReviewStatus.setText("Trạng thái: Đã ẩn");
            holder.btnToggleHidden.setText("Hiện lại");
        } else {
            holder.txtReviewStatus.setText("Trạng thái: Đang hiển thị");
            holder.btnToggleHidden.setText("Ẩn");
        }

        holder.btnDeleteReview.setAllCaps(false);
        holder.btnToggleHidden.setAllCaps(false);

        holder.btnToggleHidden.setOnClickListener(v -> {
            if (toggleHiddenListener != null) {
                toggleHiddenListener.onToggleHidden(review);
            }
        });

        holder.btnDeleteReview.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteReview(review);
            }
        });
        holder.btnReplyReview.setAllCaps(false);
        holder.btnReplyReview.setOnClickListener(v -> {
            if (replyListener != null) {
                replyListener.onReplyReview(review);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}