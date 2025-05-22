package com.example.myapplication;

import com.example.myapplication.model.GoogleAuthRequest;
import com.example.myapplication.model.ResponseModel;
import com.example.myapplication.model.TransactionRequest;
import com.example.myapplication.model.TransactionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("/api/balance")
    Call<String> getBalance(@Query("user") String user);

    @POST("/api/transaction")
    Call<Void> makeTransaction(@Body TransactionRequest request);

    @GET("/api/transactions/{user}")
    Call<List<TransactionResponse>> getHistory(@Path("user") String user);

    @GET("/api/transactions")
    Call<List<TransactionResponse>> getAllTransactions();

    @POST("auth/google")
    Call<ResponseModel> sendGoogleToken(@Body GoogleAuthRequest request);
}
