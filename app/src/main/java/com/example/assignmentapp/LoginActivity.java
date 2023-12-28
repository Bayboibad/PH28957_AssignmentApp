package com.example.assignmentapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.assignmentapp.api.ApiUserService;
import com.example.assignmentapp.model.UserModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private final String BASE_URL = "https://ncgmgl-2806.csb.app/";

    //private final String BASE_URL = "http://192.168.1.4:2806/";
    //private final String BASE_URL = "http://192.168.137.27:2806/";
    private Button btnRegister;
    private Button btnLogin;
    private TextInputEditText edUsername;
    private TextInputEditText edPassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLoginLogin);
        btnRegister = findViewById(R.id.btnRegisterLogin);
        edUsername = findViewById(R.id.edUserNameLogin);
        edPassword = findViewById(R.id.edPasswordLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidData(edUsername,edPassword)){
                    LoadDSUser();
                }
            }
        });
        startService(new Intent(this, SocketService.class));
    }
    void LoadDSUser(){
        List<UserModel> listUser = new ArrayList();
        ApiUserService.apiService.getDataUser().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful()){
                    listUser.addAll(response.body());
                    Log.d("zzz", "onResponse: "+listUser.get(0).getFullName());
                    if (handleLoginData(listUser)==0){
                        Toast.makeText(LoginActivity.this, "LOGIN FAILED", Toast.LENGTH_SHORT).show();
                    }
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, SocketService.class));
    }

    //    private void getDataUser() {
//        ExecutorService service = Executors.newSingleThreadExecutor();
//        Handler handler = new Handler(Looper.getMainLooper());
//
//        if(isValidData(edUsername,edPassword)){
//            service.execute(new Runnable() {
//                @Override
//                public void run() {
//                    String dia_chi =BASE_URL + "user/list";
//                    String noi_dung = "";
//                    try {
//                        URL url = new URL(dia_chi);
//                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                        InputStream inputStream = connection.getInputStream();
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                        StringBuilder builder = new StringBuilder();
//                        String row;
//
//                        while ((row = reader.readLine()) != null) {
//                            builder.append(row).append("\n");
//                        }
//                        reader.close();
//                        inputStream.close();
//                        connection.disconnect();
//                        noi_dung = builder.toString();
//
//                        int status = 0;
//                        // Gọi hàm xử lý dữ liệu đăng nhập sau khi đã lấy được dữ liệu từ server
////                        if (handleLoginData(noi_dung) == 0){
////                            status = 0;
////                        }else {
////                            status = 1;
////                        }
//
//                        int finalStatus = status;
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (finalStatus == 0){
//                                    Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
//
//                    } catch (MalformedURLException e) {
//                        throw new RuntimeException(e);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            });
//        }
//
//    }
    private Integer handleLoginData(List<UserModel> listUser) {
        int count = 0;
        //            JSONArray jsonArray = new JSONArray(jsonData);
        String enteredUsername = edUsername.getText().toString();
        String enteredPassword = edPassword.getText().toString();
        for (int i = 0; i < listUser.size(); i++) {
//                JSONObject userObject = jsonArray.getJSONObject(i);
            String username = listUser.get(i).getUsername();
            String password = listUser.get(i).getPassword();

            if (enteredUsername.equals(username) && enteredPassword.equals(password)) {
                // Nếu trùng, chuyển sang MainActivity
                count++;
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("userLogin",listUser.get(i));
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                return count;  // Kết thúc vòng lặp khi đã tìm thấy đối tượng trùng
            }
        }
        // Nếu không tìm thấy đối tượng trùng
        //showToast("Invalid username or password");

        return count;
    }

    private boolean isValidData(TextInputEditText edUsername, TextInputEditText edPassword) {
        // Kiểm tra không để trống
        if (TextUtils.isEmpty(edUsername.getText()) || TextUtils.isEmpty(edPassword.getText())) {
            Toast.makeText(getApplicationContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Nếu tất cả đều hợp lệ, trả về true
        return true;
    }
}
