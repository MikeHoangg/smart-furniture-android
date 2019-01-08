package mikehoang.smartfurniture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameField;
    private EditText passwordField;
    private View formBlock;
    private View progressBlock;
    private API api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Preferences.getValue(LoginActivity.this, "AUTH_TOKEN") != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            LoginActivity.this.finish();
        }

        usernameField = (EditText) findViewById(R.id.username);
        passwordField = (EditText) findViewById(R.id.password);
        formBlock = findViewById(R.id.login_form);
        progressBlock = findViewById(R.id.login_progress);

        Button SignInButton = (Button) findViewById(R.id.sign_in_button);
        SignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.closeKeyboard(LoginActivity.this);
                attemptLogin();
            }
        });

        Retrofit retrofit = new Retrofit.Builder().baseUrl(API.BASE_URL).build();
        api = retrofit.create(API.class);
    }

    private void attemptLogin() {
        usernameField.setError(null);
        passwordField.setError(null);

        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordField.setError(getString(R.string.error_field_required));
            focusView = passwordField;
            cancel = true;
        }
        if (TextUtils.isEmpty(username)) {
            usernameField.setError(getString(R.string.error_field_required));
            focusView = usernameField;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            api.login(username, password).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    if (response.body() != null) {
                        JsonElement res = MainActivity.getJsonResponse(response.body(), LoginActivity.this);
                        if (res != null) {
                            JsonObject successResponse = res.getAsJsonObject();
                            String key = successResponse.get("key").getAsString();
                            Preferences.setValue(LoginActivity.this, "AUTH_TOKEN",
                                    "Token " + key);
                            startActivity(new Intent(LoginActivity.this,
                                    MainActivity.class));
                            LoginActivity.this.finish();
                        }
                    } else if (response.errorBody() != null) {
                        JsonElement res = MainActivity.getJsonResponse(response.errorBody(), LoginActivity.this);
                        if (res != null) {
                            JsonObject errorResponse = res.getAsJsonObject();
                            JsonElement non_field_error = errorResponse.get("non_field_errors");
                            JsonElement username_error = errorResponse.get("username");
                            JsonElement password_error = errorResponse.get("password");
                            if (non_field_error != null)
                                Toast.makeText(LoginActivity.this,
                                        non_field_error.getAsString(), Toast.LENGTH_LONG).show();
                            if (username_error != null)
                                usernameField.setError(username_error.getAsString());
                            if (password_error != null)
                                passwordField.setError(password_error.getAsString());
                        }
                    }
                    showProgress(false);
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.d("server error", t.toString());
                    Toast.makeText(LoginActivity.this, R.string.response_fail_server,
                            Toast.LENGTH_LONG).show();
                    showProgress(false);
                }
            });
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            formBlock.setVisibility(show ? View.GONE : View.VISIBLE);
            formBlock.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    formBlock.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressBlock.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBlock.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBlock.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            progressBlock.setVisibility(show ? View.VISIBLE : View.GONE);
            formBlock.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}

