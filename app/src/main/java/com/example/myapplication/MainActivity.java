package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.GoogleAuthRequest;
import com.example.myapplication.model.ResponseModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private Button  buttonStart;
    private SignInButton buttonGoogleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);
        buttonStart = findViewById(R.id.buttonStart);
//        EditText editTextUserName = findViewById(R.id.editTextUserName);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("124978209029-lmloik8mu5l0k3ut52s33qhl7phamilb.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Обработка входа через Google
        buttonGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // Обработка входа через "Старт"
//        buttonStart.setOnClickListener(v -> {
//            String userName = editTextUserName.getText().toString().trim();
//            if (!userName.isEmpty()) {
//                Intent intent = new Intent(MainActivity.this, MainPageActivity.class);
//                intent.putExtra("username", userName);
//                startActivity(intent);
//            } else {
//                Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // В этом месте — если исключение не выброшено, авторизация прошла успешно
            String userName = (account != null && account.getDisplayName() != null)
                    ? account.getDisplayName()
                    : "Google User";

            String idToken = account.getIdToken();
            if (idToken == null) {
                Toast.makeText(this, "Не удалось получить токен", Toast.LENGTH_SHORT).show();
                return;
            }

            // Вызов API для проверки токена
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://80.87.196.155:8081/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
            Call<ResponseModel> call = apiService.sendGoogleToken(new GoogleAuthRequest(idToken));
            call.enqueue(new retrofit2.Callback<ResponseModel>() {
                @Override
                public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                    if (response.isSuccessful()) {
                        // Проверка прошла успешно — переходим дальше
                        Intent intent = new Intent(MainActivity.this, MainPageActivity.class);
                        intent.putExtra("username", userName);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Ошибка проверки токена", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseModel> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ApiException e) {
            // Обработка исключения — выводим сообщение
            Toast.makeText(this, "Ошибка входа: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }
}