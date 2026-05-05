package com.example.myapplication;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminNoticeHelper {

    private static final String COLLECTION = "AdminCounters";
    private static final String DOCUMENT = "dashboard";

    public static void ensureCounterDocument() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(COLLECTION)
                .document(DOCUMENT)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Map<String, Object> initData = new HashMap<>();
                        initData.put("orderNoticeCount", 0);
                        initData.put("reviewNoticeCount", 0);

                        db.collection(COLLECTION)
                                .document(DOCUMENT)
                                .set(initData);
                    }
                });
    }

    public static void increaseOrderNotice() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(COLLECTION)
                .document(DOCUMENT)
                .update("orderNoticeCount", FieldValue.increment(1));
    }

    public static void increaseReviewNotice() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(COLLECTION)
                .document(DOCUMENT)
                .update("reviewNoticeCount", FieldValue.increment(1));
    }

    public static void resetOrderNotice() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("orderNoticeCount", 0);

        db.collection(COLLECTION)
                .document(DOCUMENT)
                .update(data);
    }

    public static void resetReviewNotice() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("reviewNoticeCount", 0);

        db.collection(COLLECTION)
                .document(DOCUMENT)
                .update(data);
    }
}