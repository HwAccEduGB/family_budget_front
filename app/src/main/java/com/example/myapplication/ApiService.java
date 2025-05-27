package com.example.myapplication;

import com.example.myapplication.model.GoogleAuthRequest;
import com.example.myapplication.model.ResponseModel;
import com.example.myapplication.model.TransactionRequest;
import com.example.myapplication.model.TransactionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
        @GET("/api/balance")
        Call<String> getBalance(@Header("X-User-Id") Long userId);

        @POST("/api/transaction")
        Call<Void> makeTransaction(@Header("X-User-Id") Long userId, @Body TransactionRequest request);

        @GET("/api/transactions/{user}")
        Call<List<TransactionResponse>> getHistory(@Path("user") String user, @Header("X-User-Id") Long userId);

        @GET("/api/transactions")
        Call<List<TransactionResponse>> getAllTransactions();

        @POST("api/auth/google")
        Call<ResponseModel> sendGoogleToken(@Body GoogleAuthRequest request);
}
