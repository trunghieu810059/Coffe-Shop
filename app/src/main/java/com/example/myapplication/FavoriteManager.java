package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class FavoriteManager {

    private static ArrayList<Product> favoriteList = new ArrayList<>();
    private static final Gson gson = new Gson();

    private static String getKey(Context context) {
        return "favorite_" + UserSession.getUsername(context);
    }

    public static void loadFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_storage", Context.MODE_PRIVATE);
        String json = prefs.getString(getKey(context), null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<Product>>() {}.getType();
            favoriteList = gson.fromJson(json, type);
        } else {
            favoriteList = new ArrayList<>();
        }
    }

    public static void saveFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_storage", Context.MODE_PRIVATE);
        prefs.edit().putString(getKey(context), gson.toJson(favoriteList)).apply();
    }

    public static void addFavorite(Context context, Product product) {
        loadFavorites(context);
        if (!isFavorite(context, product.name)) {
            favoriteList.add(product);
            saveFavorites(context);
        }
    }

    public static void removeFavorite(Context context, String productName) {
        loadFavorites(context);
        for (int i = 0; i < favoriteList.size(); i++) {
            if (favoriteList.get(i).name.equals(productName)) {
                favoriteList.remove(i);
                break;
            }
        }
        saveFavorites(context);
    }

    public static boolean isFavorite(Context context, String productName) {
        loadFavorites(context);
        for (Product p : favoriteList) {
            if (p.name.equals(productName)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Product> getFavoriteList(Context context) {
        loadFavorites(context);
        return favoriteList;
    }
}