package com.example.assignmentapp;

import android.app.Notification;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.assignmentapp.api.ApiUserService;
import com.example.assignmentapp.model.UserModel;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocketService extends Service {
    private Socket mSocket;

    private NotificationDatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new NotificationDatabaseHelper(this);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mSocket = IO.socket("https://ncgmgl-2806.csb.app");
            //https://ncgmgl-2806.csb.app/
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSocket.connect();

        mSocket.on("new msg", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String data_sv_send = (String) args[0];
                String currentDate = getCurrentDateTime();
                handleSocketEvent(data_sv_send,currentDate);
            }
        });
        mSocket.on("cmt", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String data_sv_send = (String) args[0];
                String currentDate = getCurrentDateTime();
                handleSocketEvent(data_sv_send,currentDate);
            }
        });
        return START_STICKY;
    }

    private void handleSocketEvent(String data,String date) {

        List<UserModel> listUser = new ArrayList();
        ApiUserService.apiService.getDataUser().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful()){
                    listUser.addAll(response.body());
                    Log.d("zzz", ""+listUser.size());


                    for (int i = 0; i<listUser.size();i++){
                        saveNotificationToDatabase("ComicCorner thông báo", data, date,listUser.get(i).get_id());
                    }
                    postNotify("ComicCorner thông báo:", data);

                }else {
                    Log.d("zzz", "onResponse: Không lấy được danh sách");
                }
            }
            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Log.d("zzz", "onFailure: Get data failed");
            }
        });
    }
    private void saveNotificationToDatabase(String title, String content,String date,String user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("date", date);
        values.put("user", user);

        // Chèn thông báo vào bảng
        db.insert("notifications", null, values);
        db.close();
    }
    private void postNotify(String title, String content) {
        // Khởi tạo layout cho Notify
        Notification customNotification = new NotificationCompat.Builder(this, NotifyConfig.CHANEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .build();
        // Khởi tạo Manager để quản lý notify
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        // Cần kiểm tra quyền trước khi hiển thị notify
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return; // thoát khỏi hàm nếu chưa được cấp quyền
        }

        // nếu đã cấp quyền rồi thì sẽ vượt qua lệnh if trên và đến đây thì hiển thị notify
        // mỗi khi hiển thị thông báo cần tạo 1 cái ID cho thông báo riêng
        int id_notiy = (int) new Date().getTime(); // lấy chuỗi time là phù hợp

        //lệnh hiển thị notify
        notificationManagerCompat.notify(id_notiy, customNotification);

    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
            mSocket.off();
    }
}


