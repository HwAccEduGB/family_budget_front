package com.example.myapplication.model;

public class GoogleAuthRequest {
    String token;

    public GoogleAuthRequest(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
