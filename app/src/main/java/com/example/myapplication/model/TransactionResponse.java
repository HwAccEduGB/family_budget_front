package com.example.myapplication.model;

public class TransactionResponse {
    public int id;
    public User user;
    public double amount;
    public String type;
    public String date;
    public String description;

    public static class User {
        public int id;
        public String name;
        public String email;
    }
}
