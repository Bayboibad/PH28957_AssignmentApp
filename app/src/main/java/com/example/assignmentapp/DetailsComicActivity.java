package com.example.assignmentapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignmentapp.adapter.CommentAdapter;
import com.example.assignmentapp.model.ComicModel;
import com.example.assignmentapp.model.CommentModel;
import com.example.assignmentapp.model.UserModel;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class DetailsComicActivity extends AppCompatActivity {

    private final String BASE_URL = "https://ncgmgl-2806.csb.app/";

    //private final String BASE_URL = "http://192.168.1.4:2806/";
    //private final String BASE_URL = "http://192.168.137.27:2806/";
    public ImageButton btnBack;
    public ImageView imgCover;
    public TextView tvNameComic;
    public TextView tvAuthor;
    public TextView tvAllComment;
    public TextView tvPublicationYear;
    public TextView tvDescription;
    public ListView lvComment;



    public ImageButton btnComment;
    public Button btnRead;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://ncgmgl-2806.csb.app");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_comic);
        btnBack = findViewById(R.id.btnBack);
        imgCover = findViewById(R.id.imgCoverComic);
        tvNameComic = findViewById(R.id.tvNameComic);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvPublicationYear = findViewById(R.id.tvPublicationYear);
        tvDescription = findViewById(R.id.tvDescription);
        lvComment = findViewById(R.id.lvComment);
        tvAllComment = findViewById(R.id.tvAllComment);
        btnComment = findViewById(R.id.btnComment);
        btnRead = findViewById(R.id.btnRead);
        startService(new Intent(this, SocketService.class));
        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            // Lấy Bundle từ Intent
            Bundle receivedBundle = receivedIntent.getExtras();

            if (receivedBundle != null) {
                // Lấy đối tượng ComicModel từ Bundle
                ComicModel receivedComic = (ComicModel) receivedBundle.getSerializable("selected_comic");
                UserModel user = (UserModel) receivedBundle.getSerializable("userLogin");

//                Log.d("zzz", "onCreate: "+_id);
                if (receivedComic != null) {
                    String fullCoverImageUrl = BASE_URL + receivedComic.getCoverImage();
                    Picasso.get().load(fullCoverImageUrl).into(imgCover);
//
                    tvNameComic.setText(receivedComic.getName());
                    tvAuthor.setText("Tác giả: "+ receivedComic.getAuthor());
                    tvPublicationYear.setText("Năm xuất bản: "+ receivedComic.getPublicationYear());
                    tvDescription.setText("Thông tin ngắn: "+receivedComic.getDescription());

                    btnRead.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), ContentImageActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("comic",receivedComic);
                            bundle.putSerializable("userLogin",user);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        }
                    });

                    btnBack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("userLogin",user);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        }
                    });
                    btnComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            postComment(user.get_id(),receivedComic.getId(),lvComment,receivedComic);

                            //mSocket.connect();
                            //startService(new Intent(getApplicationContext(), SocketService.class));

                        }
                    });
                    tvAllComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getComment(lvComment,receivedComic);
                        }
                    });
                    getComment(lvComment,receivedComic);
                }

            }

        }
    }

    protected void onDestroy() {
        super.onDestroy();
        // Dừng dịch vụ khi Activity bị hủy
        stopService(new Intent(this, SocketService.class));
    }

    void postComment(String idUser,String idComic,ListView listView,ComicModel comic) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        // Ánh xạ các view
        final TextInputEditText edComment = findViewById(R.id.edComment);
        final String dia_chi = BASE_URL + "comic/add-comment";

        if (edComment.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(),"Hãy comment gì đó!",Toast.LENGTH_SHORT).show();
        }else {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(dia_chi);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                        // Thiết lập phương thức
                        connection.setRequestMethod("POST");

                        // Lấy ngày hiện tại và định dạng
                        Date currentDate = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                        String formattedDate = dateFormat.format(currentDate);

                        // Tạo đối tượng dữ liệu để gửi lên server
                        JSONObject postData = new JSONObject();
                        postData.put("user", idUser);
                        postData.put("comic", idComic);
                        postData.put("content", edComment.getText().toString());
                        postData.put("commentDate", formattedDate);

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
                            Log.d("zzz", "run: Thêm comment thành công");

                            count++;
                        } else {
                            // Thêm người dùng không thành công
                            Log.d("zzz", "run: Thêm comment không thành công, Mã phản hồi: " + responseCode);
                        }

                        int finalCount = count;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (finalCount >0){
                                    edComment.setText("");
                                    getComment(listView,comic);
                                    mSocket.emit("cmt", "Có 1 cmt vừa được thêm");
                                    mSocket.connect();

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


    void getComment(ListView listView,ComicModel comic) {
        Log.d("zzz", "getComment: "+comic.getId());
        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        service.execute(new Runnable() {
            @Override
            public void run() {

                String dia_chi = BASE_URL + "comic/list-comment/?comic=" + comic.getId();
                String noi_dung = "";
                try {
                    URL url = new URL(dia_chi);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder builder = new StringBuilder();
                    String row;

                    while ((row = reader.readLine()) != null) {
                        builder.append(row).append("\n");
                    }

                    reader.close();
                    inputStream.close();
                    connection.disconnect();
                    noi_dung = builder.toString();
                    Log.d("zzz", "run: Noi dung: " + noi_dung);

                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                final List<CommentModel> commentList = parseJsonData(noi_dung);

                Collections.reverse(commentList);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Tạo Adapter và thiết lập cho ListView
                        CommentAdapter adapter = new CommentAdapter(DetailsComicActivity.this, commentList);
                        listView.setAdapter(adapter);
                    }
                });
            }
        });
    }
    private List<CommentModel> parseJsonData(String jsonString) {
        List<CommentModel> comments = new ArrayList<>();

        try {
            JSONObject jsonData = new JSONObject(jsonString);
            JSONArray dataArray = jsonData.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject commentObject = dataArray.getJSONObject(i);

                String id = commentObject.getString("_id");
//                Log.d("zzz", "parseJsonData: id = " + id);
                // Lấy đối tượng user từ JSON
                JSONObject userObject = commentObject.getJSONObject("user");
                String fullName = userObject.getString("fullName");
//                Log.d("zzz", "parseJsonData: fullName = " + fullName);
                String imageUser = userObject.getString("image");

//                Log.d("zzz", "parseJsonData: image = " + imageUser);
                JSONObject comicObject = commentObject.getJSONObject("comic");
                String nameComic = comicObject.getString("name");
//                Log.d("zzz", "parseJsonData: nameComic = " + nameComic);
                String content = commentObject.getString("content");
//                Log.d("zzz", "parseJsonData: content = " + content);

                String commentDate = commentObject.getString("commentDate");
//                Log.d("zzz", "parseJsonData: commentDate = " + commentDate);
                // Định dạng để chuyển đổi từ chuỗi sang ngày
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


                CommentModel comment = new CommentModel(id, fullName, imageUser, nameComic, content, commentDate);

                comments.add(comment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return comments;
    }
}