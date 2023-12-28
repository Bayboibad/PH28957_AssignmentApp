package com.example.assignmentapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotificationDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notification_database";
    private static final int DATABASE_VERSION = 4;

    public NotificationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng lưu trữ thông báo
        db.execSQL("CREATE TABLE notifications (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, date TEXT, user TEXT);");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu có và tạo lại
        db.execSQL("DROP TABLE IF EXISTS notifications");
        onCreate(db);
    }

    // Thêm phương thức để xóa thông báo theo user._id
    public void deleteNotificationsByUserId(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("notifications", "user = ?", new String[]{userId});
        db.close();
    }

}
