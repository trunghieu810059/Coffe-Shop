package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class CartManager {

    private static ArrayList<CartItem> cartList = new ArrayList<>();
    private static final Gson gson = new Gson();

    private static String getKey(Context context) {
        return "cart_" + UserSession.getUsername(context);
    }

    public static void loadCart(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_storage", Context.MODE_PRIVATE);
        String json = prefs.getString(getKey(context), null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
            cartList = gson.fromJson(json, type);
        } else {
            cartList = new ArrayList<>();
        }
    }

    public static void saveCart(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_storage", Context.MODE_PRIVATE);
        prefs.edit().putString(getKey(context), gson.toJson(cartList)).apply();
    }

    public static void addToCart(Context context, CartItem item) {
        loadCart(context);

        for (CartItem oldItem : cartList) {
            boolean sameName = oldItem.name.equalsIgnoreCase(item.name);
            boolean sameSize = oldItem.size.equalsIgnoreCase(item.size);
            boolean sameTopping = oldItem.topping.equalsIgnoreCase(item.topping);

            if (sameName && sameSize && sameTopping) {
                oldItem.quantity += item.quantity;
                oldItem.recalculatePrice();
                saveCart(context);
                return;
            }
        }

        cartList.add(item);
        saveCart(context);
    }
    public static ArrayList<CartItem> getCartList(Context context) {
        loadCart(context);
        return cartList;
    }

    public static int getTotalAmount(Context context) {
        loadCart(context);
        int total = 0;
        for (CartItem item : cartList) {
            total += item.totalPrice;
        }
        return total;
    }

    public static void clearCart(Context context) {
        cartList.clear();
        saveCart(context);
    }
    public static void mergeDuplicateItems(Context context) {
        loadCart(context);

        for (int i = 0; i < cartList.size(); i++) {
            CartItem item1 = cartList.get(i);

            for (int j = i + 1; j < cartList.size(); j++) {
                CartItem item2 = cartList.get(j);

                boolean sameName = item1.name.equalsIgnoreCase(item2.name);
                boolean sameSize = item1.size.equalsIgnoreCase(item2.size);
                boolean sameTopping = item1.topping.equalsIgnoreCase(item2.topping);

                if (sameName && sameSize && sameTopping) {
                    item1.quantity += item2.quantity;
                    item1.recalculatePrice();
                    cartList.remove(j);
                    j--;
                }
            }
        }

        saveCart(context);
    }
}