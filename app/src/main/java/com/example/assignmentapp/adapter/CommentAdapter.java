package com.example.assignmentapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.assignmentapp.R;
import com.example.assignmentapp.model.CommentModel;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends ArrayAdapter<CommentModel> {

    private final String BASE_URL = "https://ncgmgl-2806.csb.app/";

    //private final String BASE_URL = "http://192.168.1.4:2806/";
    //private final String BASE_URL = "http://192.168.137.27:2806/";
    public CommentAdapter(Context context, List<CommentModel> comments) {
        super(context, 0, comments);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommentModel comment = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_item, parent, false);
        }

        TextView tvUser = convertView.findViewById(R.id.tvUserComment);
        TextView tvContent = convertView.findViewById(R.id.tvContentComment);
        TextView tvDate = convertView.findViewById(R.id.tvCommentDate);
        ImageView imgUser = convertView.findViewById(R.id.imgUserComment);


        tvUser.setText(comment.getUser());
        tvContent.setText(comment.getContent());
        // Chuyển đổi định dạng ngày
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        try {
            Date date = inputFormat.parse(comment.getCommentDate());
            String formattedDate = outputFormat.format(date);
            tvDate.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            tvDate.setText(""); // Nếu có lỗi, đặt giá trị ngày thành chuỗi trống hoặc thông báo lỗi khác
        }

        // Sử dụng Picasso để tải ảnh từ URL và hiển thị trong ImageView
        String fullCoverImageUrl = BASE_URL + comment.getImageUser();
        Picasso.get().load(fullCoverImageUrl).into(imgUser);

        return convertView;
    }
}
