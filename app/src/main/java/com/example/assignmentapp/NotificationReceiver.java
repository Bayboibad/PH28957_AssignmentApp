package com.example.assignmentapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Xử lý thông báo ở đây, có thể là hiển thị thông báo hoặc cập nhật giao diện
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String date = intent.getStringExtra("date");

        // Ví dụ: hiển thị thông báo
        //showNotification(context, title, content);
    }

    private void showNotification(Context context, String title, String content) {
        Toast.makeText(context, title + ": " + content, Toast.LENGTH_SHORT).show();

    }
}
