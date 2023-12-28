package com.example.assignmentapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignmentapp.adapter.ContentImageAdapter;
import com.example.assignmentapp.model.ComicModel;
import com.example.assignmentapp.model.UserModel;

public class ContentImageActivity extends AppCompatActivity {

    private final String BASE_URL = "https://ncgmgl-2806.csb.app/";

    //private final String BASE_URL = "http://192.168.1.4:2806/";
    //private final String BASE_URL = "http://192.168.137.27:2806/";
    public TextView tvName;
    public ListView lvContentImage;
    public ImageButton btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_image);
        tvName = findViewById(R.id.tvNameComicContentImage);
        lvContentImage = findViewById(R.id.lvContentImage);
        btnBack =findViewById(R.id.btnBackContentImage);

        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            // Lấy Bundle từ Intent
            Bundle receivedBundle = receivedIntent.getExtras();
            UserModel user = (UserModel) receivedBundle.getSerializable("userLogin");


            if (receivedBundle != null) {
                // Lấy đối tượng ComicModel từ Bundle
                ComicModel comic = (ComicModel) receivedBundle.getSerializable("comic");
//                Toast.makeText(getApplicationContext(),""+receivedComic.getName(),Toast.LENGTH_SHORT).show();
                if (comic != null) {
                    tvName.setText(comic.getName());
                }

                ContentImageAdapter adapter = new ContentImageAdapter(ContentImageActivity.this, comic.getContentImages());
                lvContentImage.setAdapter(adapter);

                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(),DetailsComicActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("selected_comic",comic);
                        bundle.putSerializable("userLogin",user);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                });

                startService(new Intent(this, SocketService.class));
            }
        }

    }

    protected void onDestroy() {
        super.onDestroy();
        // Dừng dịch vụ khi Activity bị hủy
        stopService(new Intent(this, SocketService.class));
    }
}