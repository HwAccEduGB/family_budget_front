package com.example.myapplication;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.TransactionRequest;
import com.example.myapplication.model.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MainPageActivity extends AppCompatActivity {

    private TextView textViewBalance;
    private String userName;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        userName = getIntent().getStringExtra("username");
        if (userName == null) userName = "N/A";

        apiService = ApiClient.getRetrofit().create(ApiService.class);
        textViewBalance = findViewById(R.id.textViewBalance);

        Button btnWithdraw = findViewById(R.id.buttonWithdraw);
        Button btnDeposit = findViewById(R.id.buttonDeposit);
        Button btnHistory = findViewById(R.id.buttonHistory);
        Button btnShowAll = findViewById(R.id.buttonShowAll);

        fetchBalance();

        btnWithdraw.setOnClickListener(v -> showAmountDialog("withdraw", "Пополнение"));
        btnDeposit.setOnClickListener(v -> showAmountDialog("deposit", "Снятие"));
        btnHistory.setOnClickListener(v -> fetchTransactions(false));
        btnShowAll.setOnClickListener(v -> fetchTransactions(true));
    }

    private long getUserId() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getLong("user_id", -1);
    }
    private void fetchBalance() {
        long userId = getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getBalance(userId).enqueue(new retrofit2.Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null ) {
                    String balance = response.body();
                    textViewBalance.setText("Баланс: " + balance);
                } else {
                    // Обработать ошибки
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(MainPageActivity.this, "Ошибка при получении баланса", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAmountDialog(String operationType, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(operationType.equals("deposit") ? "Пополнить" : "Снять");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("ОК", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
                return;
            }
            BigDecimal amount = new BigDecimal(amountStr);
            executeTransaction(amount, operationType, description);
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void executeTransaction(BigDecimal amount, String type, String description) {
        long userId = getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }
        TransactionRequest request = new TransactionRequest(userName, amount.doubleValue(), type, description);
        apiService.makeTransaction(userId, request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainPageActivity.this, "Транзакция выполнена", Toast.LENGTH_SHORT).show();
                    fetchBalance();
                } else {
                    Toast.makeText(MainPageActivity.this, "Ошибка при выполнении транзакции", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainPageActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTransactions(boolean fetchAll) {
        long userId = getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fetchAll) {
            apiService.getAllTransactions().enqueue(new retrofit2.Callback<List<TransactionResponse>>() {
                @Override
                public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        displayTransactions(response.body());
                    }
                }

                @Override
                public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                    Toast.makeText(MainPageActivity.this, "Ошибка при получении транзакций", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String userName = getIntent().getStringExtra("username");
            if (userName == null) userName = "N/A";

            apiService.getHistory(userName, userId).enqueue(new retrofit2.Callback<List<TransactionResponse>>() {
                @Override
                public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        displayTransactions(response.body());
                    }
                }

                @Override
                public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                    Toast.makeText(MainPageActivity.this, "Ошибка при получении транзакций", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void displayTransactions(List<TransactionResponse> transactions) {
        StringBuilder sb = new StringBuilder();
        for (TransactionResponse tr : transactions) {
            sb.append("ID: ").append(tr.id)
                    .append("\nИмя: ").append(tr.user != null ? tr.user.name : "N/A")
                    .append("\nТип: ").append(tr.type)
                    .append("\nСумма: ").append(tr.amount)
                    .append("\nДата: ").append(tr.date)
                    .append("\nОписание: ").append(tr.description)
                    .append("\n\n");
        }
        showTransactions(sb.toString());
    }

    private void showTransactions(String text) {
        // показываем через AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Транзакции")
                .setMessage(text)
                .setPositiveButton("ОК", null)
                .show();
    }
}