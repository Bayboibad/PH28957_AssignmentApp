package com.example.assignmentapp.api;

import com.example.assignmentapp.R;
import com.example.assignmentapp.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiUserService {

    public static final String DOMAIN = "https://ncgmgl-2806.csb.app/user/";
    //public static final String DOMAIN = "http://192.168.1.4:2806/user/";
    //public static final String DOMAIN = "http://192.168.137.27:2806/user/";
    Gson gson = new GsonBuilder().setDateFormat("yyyy MM dd HH:mm:ss").create();


    ApiUserService apiService = new Retrofit.Builder()
            .baseUrl(DOMAIN)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiUserService.class);

    @Multipart
    @POST("addUser")
    Call<UserModel> addUser(@Part(ConstUser.KEY_FULL_NAME) RequestBody fullName,
                             @Part(ConstUser.KEY_USERNAME) RequestBody username,
                             @Part(ConstUser.KEY_EMAIL) RequestBody email,
                             @Part(ConstUser.KEY_PASSWORD) RequestBody password,
                             @Part(ConstUser.KEY_ADDRESS) RequestBody address,
                             @Part(ConstUser.KEY_PHONE_NUMBER) RequestBody phoneNumber,
                             @Part MultipartBody.Part image);

    @Multipart
    @POST("updateUser")
    Call<UserModel> updateUser(@Part(ConstUser.KEY_ID) RequestBody _id,
                            @Part(ConstUser.KEY_FULL_NAME) RequestBody fullName,
                            @Part(ConstUser.KEY_USERNAME) RequestBody username,
                            @Part(ConstUser.KEY_EMAIL) RequestBody email,
                            @Part(ConstUser.KEY_PASSWORD) RequestBody password,
                            @Part(ConstUser.KEY_ADDRESS) RequestBody address,
                            @Part(ConstUser.KEY_PHONE_NUMBER) RequestBody phoneNumber,
                            @Part MultipartBody.Part image);

    @GET("list")
    Call<List<UserModel>> getDataUser();

}
