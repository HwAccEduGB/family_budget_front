package com.example.myapplication.model;

public class TransactionRequest {
    public String userName;
    public double amount;
    public String type; // "withdrawal" или "deposit"
    public String description;

    public TransactionRequest(String userName, double amount, String type, String description) {
        this.userName = userName;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }
}
