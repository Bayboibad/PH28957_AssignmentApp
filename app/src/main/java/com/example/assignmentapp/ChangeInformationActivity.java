package com.example.assignmentapp;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.assignmentapp.api.ApiUserService;
import com.example.assignmentapp.api.ConstUser;
import com.example.assignmentapp.api.RealPathUtil;
import com.example.assignmentapp.model.UserModel;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeInformationActivity extends AppCompatActivity {

    private final String BASE_URL = "https://ncgmgl-2806.csb.app/";

    //private final String BASE_URL = "http://192.168.1.4:2806/";
    //private final String BASE_URL = "http://192.168.137.27:2806/";

    private static final int MY_REQUEST_CODE = 10;
    Uri mUri;

    public String idUser;

    List<UserModel> listUser = new ArrayList<>();


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
                            imgUser.setImageBitmap(bitmap);
                        }catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
    );

    ImageButton btnBack;
    TextInputEditText edFullName,edUsername,edEmail,edPassword,edPhoneNumber,edAddress;
    Button btnAddImage,btnConfirm;
    ImageView imgUser;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_information);

        initUI();
        LoadDSUser();



        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            UserModel user = (UserModel) bundle.getSerializable("userLogin");

            setData(user);
            idUser = user.get_id();
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ChangeInformationActivity.this, AccountActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userLogin",user);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
            });

            btnAddImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickRequestPermission();
                }
            });

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mUri!=null){
                        callAPIUpdateUser();
                    }else {
                        Toast.makeText(ChangeInformationActivity.this, "Thay đổi ảnh đi bạn ơi!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        startService(new Intent(this, SocketService.class));
    }

    protected void onDestroy() {
        super.onDestroy();
        // Dừng dịch vụ khi Activity bị hủy
        stopService(new Intent(this, SocketService.class));
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

    private void callAPIUpdateUser() {

        String id = idUser;
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
            RequestBody requestBodyID = RequestBody.create(MediaType.parse("multipart/form-data"),id);

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

            ApiUserService.apiService.updateUser(requestBodyID,requestBodyFullName,requestBodyUsername,requestBodyEmail,requestBodyPassword,requestBodyAddress,requestBodyPhoneNumber,mPartImage).enqueue(new Callback<UserModel>() {
                @Override
                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                    if (response.isSuccessful()){
                        UserModel comicModel = response.body();
//                    Toast.makeText(,ChangeInformationActivity.this,"UPDATE SUCCESSFULLY",Toast.LENGTH_SHORT).show();
                        if (comicModel!=null){
                            edFullName.setText("");
                            edUsername.setText("");
                            edEmail.setText("");
                            edPassword.setText("");
                            edPhoneNumber.setText("");
                            edAddress.setText("");
                            imgUser.setImageBitmap(null);
                            imgUser.setVisibility(View.GONE);
                            dialogLogout();
                        }
                    }
                }
                @Override
                public void onFailure(Call<UserModel> call, Throwable t) {
                    Toast.makeText(ChangeInformationActivity.this,"UPDATE FAILED",Toast.LENGTH_SHORT).show();
                }
            });
        }


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

    private void dialogLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận đăng xuất");
        builder.setMessage("Thay đổi thông tin thành công. Bạn cần đăng xuất và đăng nhập lại vào app.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();

                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void setData(UserModel user) {
        edFullName.setText(user.getFullName());
        edUsername.setText(user.getUsername());
        edEmail.setText(user.getEmail());
        edPassword.setText(user.getPassword());
        edAddress.setText(user.getAddress());
        edPhoneNumber.setText(user.getPhoneNumber());
        imgUser.setVisibility(View.VISIBLE);
        String fullImageUrl = BASE_URL + user.getImage();
        Picasso.get().load(fullImageUrl).into(imgUser);
    }

    private void initUI() {
        edFullName = findViewById(R.id.edFullNameUpdate);
        edUsername = findViewById(R.id.edUsernameUpdate);
        edEmail = findViewById(R.id.edEmailUpdate);
        edPassword = findViewById(R.id.edPasswordUpdate);
        edAddress = findViewById(R.id.edAddressUpdate);
        edPhoneNumber = findViewById(R.id.edPhoneNumberUpdate);
        imgUser = findViewById(R.id.imgAvtUpdate);
        btnAddImage = findViewById(R.id.btnAddImgUpdate);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack= findViewById(R.id.btnBackChangeInformation);

    }
}