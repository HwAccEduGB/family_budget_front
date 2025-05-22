package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.TransactionRequest;
import com.example.myapplication.model.TransactionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPageActivity extends AppCompatActivity {

    private TextView textViewBalance;
    private String userName;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        apiService = ApiClient.getRetrofit().create(ApiService.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        userName = getIntent().getStringExtra("username");
        textViewBalance = findViewById(R.id.textViewBalance);

        Button btnWithdraw = findViewById(R.id.buttonWithdraw);
        Button btnDeposit = findViewById(R.id.buttonDeposit);
        Button btnHistory = findViewById(R.id.buttonHistory);
        Button btnShowAll = findViewById(R.id.buttonShowAll);

        fetchBalance();

        btnWithdraw.setOnClickListener(v -> showAmountDialog("withdraw"));
        btnDeposit.setOnClickListener(v -> showAmountDialog("deposit"));
        btnHistory.setOnClickListener(v -> fetchTransactions(false));
        btnShowAll.setOnClickListener(v -> fetchTransactions(true));
    }

    private void fetchBalance() {
        apiService.getBalance(userName).enqueue(new retrofit2.Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String balance = response.body();
                    textViewBalance.setText("Баланс: " + balance);
                } else {
                    // Обработка ошибок
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                // Обработка ошибок
            }
        });
    }

    private void showAmountDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(type.equals("withdraw") ? "Снять средства" : "Положить средства");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                performTransaction(type, amountStr);
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void performTransaction(String type, String amountStr) {
        double amount = Double.parseDouble(amountStr);
        String desc = type.equals("withdraw") ? "Снятие" : "Пополнение";

        TransactionRequest request = new TransactionRequest(userName, amount,
                type.equals("withdraw") ? "withdrawal" : "deposit", desc);

        apiService.makeTransaction(request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchBalance(); // обновляем баланс
                    Toast.makeText(MainPageActivity.this, "Операция выполнена", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainPageActivity.this, "Ошибка при операции", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainPageActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTransactions(final boolean fetchAll) {
        Call<List<TransactionResponse>> call;
        String title;

        if (fetchAll) {
            call = apiService.getAllTransactions();
            title = "Все транзакции";
        } else {
            call = apiService.getHistory(userName);
            title = "История по пользователю";
        }

        call.enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StringBuilder sb = new StringBuilder();
                    for (TransactionResponse tr : response.body()) {
                        sb.append("ID: ").append(tr.id)
                                .append("\nИмя: ").append(tr.user != null ? tr.user.name : "N/A")
                                .append("\nТип: ").append(tr.type)
                                .append("\nСумма: ").append(tr.amount)
                                .append("\nДата: ").append(tr.date)
                                .append("\nОписание: ").append(tr.description)
                                .append("\n\n");
                    }
                    Intent intent = new Intent(MainPageActivity.this, ResultActivity.class);
                    intent.putExtra("result", sb.toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(MainPageActivity.this, "Ошибка получения данных", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                Toast.makeText(MainPageActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
}