package com.example.assignmentapp.dialog;

import android.app.ProgressDialog;
import android.content.Context;

public class DialogLoading {
    private ProgressDialog progressDialog;
    private static DialogLoading instance;

    private DialogLoading(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false); // Cho phép hủy bỏ hay không
        progressDialog.setMessage("Loading..."); // Đặt thông điệp hiển thị
    }

    public static synchronized DialogLoading getInstance(Context context) {
        if (instance == null) {
            instance = new DialogLoading(context);
        }
        return instance;
    }

    public void show() {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public void dismiss() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
