package com.example.assignmentapp;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.assignmentapp.api.ApiUserService;
import com.example.assignmentapp.api.ConstUser;
import com.example.assignmentapp.api.RealPathUtil;
import com.example.assignmentapp.model.UserModel;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

     private final String BASE_URL = "https://ncgmgl-2806.csb.app/";

    //private final String BASE_URL = "http://192.168.1.4:2806/";

    //private final String BASE_URL = "http://192.168.137.27:2806/";

    private static final int MY_REQUEST_CODE = 10;
    Uri mUri;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://ncgmgl-2806.csb.app");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e("zzz", "onActivityResult");
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if (data == null){
                            return;
                        }
                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                            imgAvt.setImageBitmap(bitmap);
                            imgAvt.setVisibility(View.VISIBLE);
                        }catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
    );

    public Button btnRegister;
    public Button btnLogin;

    public TextInputEditText edFullName,edUsername,edEmail,edPassword,edPhoneNumber,edAddress;

    ImageView imgAvt;
    Button btnAddImage;

    List<UserModel> listUser = new ArrayList<>();
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initUI();
        LoadDSUser();
        startService(new Intent(this, SocketService.class));
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onClickRequestPermission();

            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUri != null ){
                    if (isValidData(edFullName, edUsername, edEmail, edPassword, edPhoneNumber, edAddress)){
                        callAPIRegisterUser();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Thêm ảnh đi bạn ơi!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void initUI() {
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        imgAvt = findViewById(R.id.imgAvt);
        btnAddImage = findViewById(R.id.btnAddImg);
        edFullName = findViewById(R.id.edFullName);
        edUsername = findViewById(R.id.edUsernameRe);
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPasswordRe);
        edPhoneNumber = findViewById(R.id.edPhoneNumber);
        edAddress = findViewById(R.id.edAddress);
    }
    private void onClickRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            openGallery();
            return;
        }

        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            openGallery();
        }else {
            String [] permission = {READ_EXTERNAL_STORAGE};
            requestPermissions(permission, MY_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openGallery();
            }
        }

    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
            mActivityResultLauncher.launch(Intent.createChooser(intent,"Select Picture"));
    }

    private void callAPIRegisterUser() {

        String fullName  = edFullName.getText().toString().trim();
        String userName  = edUsername.getText().toString().trim();
        String email  = edEmail.getText().toString().trim();
        String password  = edPassword.getText().toString().trim();
        String address  = edAddress.getText().toString().trim();
        String phoneNumber  = edPhoneNumber.getText().toString().trim();

        int count = 0;
        for (int i = 0;i<listUser.size();i++){
            if (userName.equals(listUser.get(i).getUsername())){
                count++;
            }
        }
        if (count>0){
            Toast.makeText(this, "Username đã tồn tại!", Toast.LENGTH_SHORT).show();
        }else {
            RequestBody requestBodyFullName = RequestBody.create(MediaType.parse("multipart/form-data"),fullName);

            RequestBody requestBodyUsername = RequestBody.create(MediaType.parse("multipart/form-data"),userName);

            RequestBody requestBodyEmail = RequestBody.create(MediaType.parse("multipart/form-data"),email);

            RequestBody requestBodyPassword = RequestBody.create(MediaType.parse("multipart/form-data"),password);

            RequestBody requestBodyAddress = RequestBody.create(MediaType.parse("multipart/form-data"),address);

            RequestBody requestBodyPhoneNumber = RequestBody.create(MediaType.parse("multipart/form-data"),phoneNumber);

            String strRealPath = RealPathUtil.getRealPath(this,mUri);
            Log.e("zzz", strRealPath);
            File file = new File(strRealPath);

            RequestBody image = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part mPartImage = MultipartBody.Part.createFormData(ConstUser.KEY_IMAGE, file.getName(),image);

            ApiUserService.apiService.addUser(requestBodyFullName,requestBodyUsername,requestBodyEmail,requestBodyPassword,requestBodyAddress,requestBodyPhoneNumber,mPartImage).enqueue(new Callback<UserModel>() {
                @Override
                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                    if (response.isSuccessful()){
                        UserModel comicModel = response.body();
                        Toast.makeText(RegisterActivity.this,"REGISTER SUCCESSFULLY",Toast.LENGTH_SHORT).show();
                        if (comicModel!=null){
                            edFullName.setText("");
                            edUsername.setText("");
                            edEmail.setText("");
                            edPassword.setText("");
                            edPhoneNumber.setText("");
                            edAddress.setText("");
                            imgAvt.setImageBitmap(null);
                            imgAvt.setVisibility(View.GONE);
                            mSocket.emit("register", "Vừa có 1 tài khoản được đăng ký!");
                            mSocket.connect();
                            mSocket.on("register", new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    RegisterActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String data_sv_send = (String) args[0];
                                            postNotify("ComicCorner thông báo: ", data_sv_send );
                                            mSocket.off();
                                        }
                                    });
                                }
                            });

                        }
                    }
                }
                @Override
                public void onFailure(Call<UserModel> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this,"REGISTER FAILED",Toast.LENGTH_SHORT).show();
                }
            });
        }


    }
    void postNotify(String title, String content){
        // Khởi tạo layout cho Notify
        Notification customNotification = new NotificationCompat.Builder(RegisterActivity.this, NotifyConfig.CHANEL_ID)
                .setSmallIcon(android.R.drawable.ic_input_add)
                .setContentTitle( title )
                .setContentText(content)
                .setAutoCancel(true)
                .build();
        // Khởi tạo Manager để quản lý notify
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(RegisterActivity.this);

        // Cần kiểm tra quyền trước khi hiển thị notify
        if (ActivityCompat.checkSelfPermission(RegisterActivity.this,
                android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            // Gọi hộp thoại hiển thị xin quyền người dùng
            ActivityCompat.requestPermissions(RegisterActivity.this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 999999);
            Toast.makeText(RegisterActivity.this, "Chưa cấp quyền", Toast.LENGTH_SHORT).show();
            return; // thoát khỏi hàm nếu chưa được cấp quyền
        }
        // nếu đã cấp quyền rồi thì sẽ vượt qua lệnh if trên và đến đây thì hiển thị notify
        // mỗi khi hiển thị thông báo cần tạo 1 cái ID cho thông báo riêng
        int id_notiy = (int) new Date().getTime();// lấy chuỗi time là phù hợp
        //lệnh hiển thị notify
        notificationManagerCompat.notify(id_notiy , customNotification);
    }

    void LoadDSUser(){
        ApiUserService.apiService.getDataUser().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful()){
                    listUser.addAll(response.body());
                    Log.d("zzz", "onResponse: "+listUser.get(0).getFullName());

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
    void postData() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        // Ánh xạ các view
        final TextInputEditText edFullName = findViewById(R.id.edFullName);
        final TextInputEditText edUsername = findViewById(R.id.edUsernameRe);
        final TextInputEditText edEmail = findViewById(R.id.edEmail);
        final TextInputEditText edPassword = findViewById(R.id.edPasswordRe);
        final TextInputEditText edPhoneNumber = findViewById(R.id.edPhoneNumber);
        final TextInputEditText edAddress = findViewById(R.id.edAddress);
        final String dia_chi = BASE_URL + "/user/addUser";

        if (isValidData(edFullName, edUsername, edEmail, edPassword, edPhoneNumber, edAddress)){
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(dia_chi);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                        // Thiết lập phương thức
                        connection.setRequestMethod("POST");

                        // Tạo đối tượng dữ liệu để gửi lên server
                        JSONObject postData = new JSONObject();
                        postData.put("fullName", edFullName.getText().toString());
                        postData.put("username", edUsername.getText().toString());
                        postData.put("email", edEmail.getText().toString());
                        postData.put("password", edPassword.getText().toString());
                        postData.put("address", edAddress.getText().toString());
                        postData.put("phoneNumber", edPhoneNumber.getText().toString());
                        postData.put("image", "uploads/bom1.jpg");

                        // Thiết lập kiểu dữ liệu
                        connection.setRequestProperty("Content-Type", "application/json");

                        // Mở luồng gửi dữ liệu
                        OutputStream outputStream = connection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

                        // Ghi dữ liệu vào luồng output
                        writer.append(postData.toString());
                        writer.flush();
                        writer.close();
                        outputStream.close();

                        // Lấy mã phản hồi từ server
                        int responseCode = connection.getResponseCode();

                        // Đóng kết nối
                        connection.disconnect();
                        int count = 0;
                        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                            // Thêm người dùng thành công
                            Log.d("zzz", "run: Thêm người dùng thành công");
                            count++;
                        } else {
                            // Thêm người dùng không thành công
                            Log.d("zzz", "run: Thêm người dùng không thành công, Mã phản hồi: " + responseCode);
                        }

                        int finalCount = count;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (finalCount >0){
                                    Toast.makeText(getApplicationContext(), "Register Successfully", Toast.LENGTH_SHORT).show();
                                    edFullName.setText("");
                                    edUsername.setText("");
                                    edEmail.setText("");
                                    edPassword.setText("");
                                    edAddress.setText("");
                                    edPhoneNumber.setText("");
                                }
                            }
                        });
                    } catch (MalformedURLException e) {
                        Log.e("zzz", "run: MalformedURLException", e);
                    } catch (IOException | JSONException e) {
                        Log.e("zzz", "run: IOException | JSONException", e);
                    }
                }
            });
        }
    }
    private boolean isValidData(
            TextInputEditText edFullName,
            TextInputEditText edUsername,
            TextInputEditText edEmail,
            TextInputEditText edPassword,
            TextInputEditText edPhoneNumber,
            TextInputEditText edAddress
    ) {
        // Kiểm tra không để trống
        if (TextUtils.isEmpty(edFullName.getText())
                || TextUtils.isEmpty(edUsername.getText())
                || TextUtils.isEmpty(edEmail.getText())
                || TextUtils.isEmpty(edPassword.getText())
                || TextUtils.isEmpty(edPhoneNumber.getText())
                || TextUtils.isEmpty(edAddress.getText())) {
            Toast.makeText(getApplicationContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Kiểm tra định dạng email
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (!edEmail.getText().toString().trim().matches(emailPattern)) {
            Toast.makeText(getApplicationContext(), "Định dạng email không đúng", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Kiểm tra định dạng số điện thoại
        String phoneNumberPattern = "(0[0-9]{9})|([0-9]{10})";
        if (!edPhoneNumber.getText().toString().trim().matches(phoneNumberPattern)) {
            Toast.makeText(getApplicationContext(), "Định dạng số điện thoại không đúng", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Nếu tất cả đều hợp lệ, trả về true
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, SocketService.class));
        mSocket.off();
    }

}