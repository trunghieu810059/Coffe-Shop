package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {

    private ListView listViewFavorite;
    private Button btnBackFavorite;
    private ProductAdapter adapter;
    private ArrayList<Product> favoriteList;
    private TextView txtEmptyFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite);

        listViewFavorite = findViewById(R.id.listViewFavorite);
        btnBackFavorite = findViewById(R.id.btnBackFavorite);
        txtEmptyFavorite = findViewById(R.id.txtEmptyFavorite);

        btnBackFavorite.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();

        favoriteList = FavoriteManager.getFavoriteList(this);

        if (favoriteList == null || favoriteList.isEmpty()) {
            listViewFavorite.setAdapter(null);
            txtEmptyFavorite.setText("Chưa có sản phẩm yêu thích");
            txtEmptyFavorite.setVisibility(TextView.VISIBLE);
        } else {
            txtEmptyFavorite.setVisibility(TextView.GONE);
            adapter = new ProductAdapter(this, favoriteList);
            listViewFavorite.setAdapter(adapter);
        }
    }
}