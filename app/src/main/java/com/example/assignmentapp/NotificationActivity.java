package com.example.assignmentapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.assignmentapp.adapter.NotificationAdapter;
import com.example.assignmentapp.model.NotificationModel;
import com.example.assignmentapp.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationActivity extends AppCompatActivity {

    ListView lvNotification;
    ImageButton btnBack;
    Button btnDeletele;
    private NotificationReceiver notificationReceiver;

    private final String BASE_URL = "https://ncgmgl-2806.csb.app/";
   // private final String BASE_URL = "http://192.168.1.4:2806/";
    //private final String BASE_URL = "http://192.168.137.27:2806/";
    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        lvNotification = findViewById(R.id.lvNotification);
        btnDeletele = findViewById(R.id.btnDeleteNotification);

        btnBack = findViewById(R.id.btnBackNotification);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            UserModel user = (UserModel) bundle.getSerializable("userLogin");
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userLogin",user);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
            });

            btnDeletele.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteUser(user);
                }
            });
            startService(new Intent(this, SocketService.class));
            displayNotifications(user);
//            IntentFilter filter = new IntentFilter("NEW_NOTIFICATION");
//            registerReceiver(new NotificationReceiver(), filter);
        }
    }

    // Phương thức xóa thông báo của một người dùng
    private void deleteNotificationsForUser(String userId) {
        NotificationDatabaseHelper dbHelper = new NotificationDatabaseHelper(this);
        dbHelper.deleteNotificationsByUserId(userId);
    }
    private void displayNotifications(UserModel user) {
        // Lấy dữ liệu từ cơ sở dữ liệu và hiển thị trên giao diện người dùng
        NotificationDatabaseHelper dbHelper = new NotificationDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<NotificationModel> notifications = new ArrayList<>();

        // Sử dụng điều kiện WHERE để lọc theo user._id
        String selection = "user = ?";
        String[] selectionArgs = {user.get_id()}; // Chuyển user._id thành chuỗi

        Cursor cursor = db.query("notifications", null, selection, selectionArgs, null, null, null);

        // Log ra danh sách notification
        while (cursor.moveToNext()) {
            @SuppressLint("Range") int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id")));
            @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("title"));
            @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex("content"));
            @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
            @SuppressLint("Range") String userId = cursor.getString(cursor.getColumnIndex("user"));
            Log.d("zzz", "Title: " + title + ", Content: " + content);
            notifications.add(new NotificationModel(id, title, content, date, userId));
        }

        cursor.close();
        db.close();

        // Sử dụng Adapter để hiển thị dữ liệu trên ListView
        Collections.reverse(notifications);
        NotificationAdapter adapter = new NotificationAdapter(this, notifications);
        lvNotification.setAdapter(adapter);
        if (notifications.size() == 0){
            btnDeletele.setVisibility(View.GONE);
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(notificationReceiver);
        stopService(new Intent(this, SocketService.class));
    }
    private void deleteUser(UserModel user) {
//        Toast.makeText(this, ""+id, Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc muốn xóa tất cả thông báo không?");
        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNotificationsForUser(user.get_id());
                // Sau khi xóa, cập nhật lại danh sách thông báo
                displayNotifications(user);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Đóng dialog nếu người dùng không muốn xóa
                dialog.dismiss();
            }
        });
        builder.show();
    }
}