package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton buttonGoogleSignIn;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.getData()));
                    }
                }
        );

        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("124978209029-jpf8hdr2t4q34tlpi8ms156jifkkonnl.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        buttonGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });
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

            Log.d("GoogleAuth", "Account: " + account);
            Log.d("GoogleAuth", "Email: " + account.getEmail());
            Log.d("GoogleAuth", "Token length: " + (account.getIdToken() != null ? account.getIdToken().length() : "null"));

            String userName = (account != null && account.getDisplayName() != null)
                    ? account.getDisplayName()
                    : "Google User";

            String idToken = account.getIdToken();
            if (idToken == null) {
                Toast.makeText(this, "Не удалось получить токен", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
            Call<ResponseModel> call = apiService.sendGoogleToken(new GoogleAuthRequest(idToken));
            call.enqueue(new retrofit2.Callback<ResponseModel>() {
                @Override
                public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ResponseModel resp = response.body();

                        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                        prefs.edit()
                                .putLong("user_id", resp.getUserId())
                                .apply();

                        Intent intent = new Intent(MainActivity.this, MainPageActivity.class);
                        intent.putExtra("username", account.getDisplayName());
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
            Log.e("GoogleAuth", "Sign-in failed. Code: " + e.getStatusCode() + ", Message: " + e.getMessage());
            Toast.makeText(this, "Ошибка входа: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

}