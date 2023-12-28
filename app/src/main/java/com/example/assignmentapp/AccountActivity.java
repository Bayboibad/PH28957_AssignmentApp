package com.example.assignmentapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignmentapp.model.UserModel;
import com.squareup.picasso.Picasso;

public class AccountActivity extends AppCompatActivity {
    ImageView imgAvt;
    TextView tvName;
    ImageButton btnBack;
    Button btnChangeInformation,btnLogout;
    private final String BASE_URL = "https://ncgmgl-2806.csb.app/";

    //private final String BASE_URL = "http://192.168.1.4:2806/";

    //private final String BASE_URL = "http://192.168.137.27:2806/";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        btnBack = findViewById(R.id.btnBackAccount);
        btnLogout =findViewById(R.id.btnLogOut);
        tvName = findViewById(R.id.tvNameUserLogin);
        imgAvt = findViewById(R.id.imgAvtUserLogin);
        btnChangeInformation = findViewById(R.id.btnChangeInformation);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            UserModel user = (UserModel) bundle.getSerializable("userLogin");

            tvName.setText(user.getFullName());
            String fullImageUrl = BASE_URL + user.getImage();
            Picasso.get().load(fullImageUrl).into(imgAvt);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userLogin",user);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
            });
            btnChangeInformation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(AccountActivity.this, ChangeInformationActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userLogin",user);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
            });
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogLogout();
                }
            });
            startService(new Intent(this, SocketService.class));
        }
    }
    private void dialogLogout() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xác nhận đăng xuất");
            builder.setMessage("Bạn có chắc chắn muốn đăng xuất không?");
            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                    finish();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
    }

    protected void onDestroy() {
        super.onDestroy();
        // Dừng dịch vụ khi Activity bị hủy
        stopService(new Intent(this, SocketService.class));
    }
}