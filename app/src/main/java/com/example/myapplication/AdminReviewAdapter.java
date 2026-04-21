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
    private final Context context;
    private final ArrayList<Review> list;
    private final OnDeleteReviewListener deleteListener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
    public AdminReviewAdapter(Context context, ArrayList<Review> list, OnDeleteReviewListener deleteListener) {
        this.context = context;
        this.list = list;
        this.deleteListener = deleteListener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtUser, txtTime, txtComment;
        RatingBar ratingBar;
        Button btnDeleteReview;

        VH(@NonNull View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.txtUser);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtComment = itemView.findViewById(R.id.txtComment);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnDeleteReview = itemView.findViewById(R.id.btnDeleteReview);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_admin_review, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Review r = list.get(position);

        holder.txtUser.setText(r.username != null ? r.username : "User");
        holder.txtComment.setText(r.comment != null ? r.comment : "(Không có nội dung)");
        holder.ratingBar.setRating((float) r.rating);
        int rounded = (int) Math.round(r.rating);

        if (rounded <= 3) {
            holder.itemView.setBackgroundColor(0xFFFFEBEE); // nền đỏ nhạt
            holder.txtUser.setTextColor(0xFFB00020);
            holder.txtComment.setTextColor(0xFFB00020);
        } else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF); // nền trắng
            holder.txtUser.setTextColor(0xFF000000);
            holder.txtComment.setTextColor(0xFF000000);
        }
        holder.btnDeleteReview.setText("Xóa");
        holder.btnDeleteReview.setAllCaps(false);

        if (r.createdAt != null) {
            holder.txtTime.setText(sdf.format(r.createdAt.toDate()));
        } else {
            holder.txtTime.setText("");
        }

        holder.btnDeleteReview.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteReview(r);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}