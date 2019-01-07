package mikehoang.smartfurniture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {
    private EditText usernameField;
    private EditText emailField;
    private EditText firstNameField;
    private EditText lastNAmeField;
    private EditText heightField;
    private MainActivity parent;
    private View progressBlock;
    private View formBlock;
    private JsonObject user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        usernameField = v.findViewById(R.id.username);
        emailField = v.findViewById(R.id.email);
        firstNameField = v.findViewById(R.id.first_name);
        lastNAmeField = v.findViewById(R.id.last_name);
        heightField = v.findViewById(R.id.height);
        progressBlock = v.findViewById(R.id.edit_profile_progress);
        formBlock = v.findViewById(R.id.edit_profile_form);
        Button saveButton = v.findViewById(R.id.save_button);

        parent = (MainActivity) getActivity();
        if (parent.user != null)
            user = parent.user;
        else {
            String userData = Preferences.getValue(parent, "USER");
            user = new JsonParser().parse(userData).getAsJsonObject();
        }

        usernameField.setText(user.get("username").getAsString());
        emailField.setText(user.get("email").getAsString());
        firstNameField.setText(user.get("first_name").getAsString());
        lastNAmeField.setText(user.get("last_name").getAsString());
        try {
            user.get("height").getAsJsonNull();
        } catch (IllegalStateException e) {
            heightField.setText(user.get("height").getAsString());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.closeKeyboard(parent);
                attemptSave();
            }
        });
        return v;
    }


    private void attemptSave() {
        usernameField.setError(null);
        emailField.setError(null);
        firstNameField.setError(null);
        lastNAmeField.setError(null);
        heightField.setError(null);

        String username = usernameField.getText().toString();
        String email = emailField.getText().toString();
        String firstName = firstNameField.getText().toString();
        String lastName = lastNAmeField.getText().toString();
        String height = heightField.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            usernameField.setError(getString(R.string.error_field_required));
            focusView = usernameField;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            emailField.setError(getString(R.string.error_field_required));
            focusView = emailField;
            cancel = true;
        }

        if (TextUtils.isEmpty(height)) {
            heightField.setError(getString(R.string.error_field_required));
            focusView = heightField;
            cancel = true;
        }

        if (cancel)
            focusView.requestFocus();
        else {
            showProgress(true);
            parent.api.editCurrentUser(parent.key,
                    username,
                    email,
                    firstName,
                    lastName,
                    Double.parseDouble(height)).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    if (response.body() != null) {
                        parent.getCurrentUser();
                        Toast.makeText(parent, R.string.response_success_profile,
                                Toast.LENGTH_LONG).show();
                    } else if (response.errorBody() != null) {
                        JsonElement res = MainActivity.getJsonResponse(response.errorBody(), parent);
                        if (res != null) {
                            JsonObject errorResponse = res.getAsJsonObject();
                            JsonElement non_field_error = errorResponse.get("non_field_errors");
                            JsonElement username_error = errorResponse.get("username");
                            JsonElement email_error = errorResponse.get("email");
                            JsonElement firstName_error = errorResponse.get("first_name");
                            JsonElement lastName_error = errorResponse.get("last_name");
                            JsonElement height_error = errorResponse.get("height");

                            if (non_field_error != null)
                                Toast.makeText(parent, non_field_error.getAsString(),
                                        Toast.LENGTH_LONG).show();
                            if (username_error != null)
                                usernameField.setError(username_error.getAsString());
                            if (email_error != null)
                                emailField.setError(email_error.getAsString());
                            if (firstName_error != null)
                                firstNameField.setError(firstName_error.getAsString());
                            if (lastName_error != null)
                                lastNAmeField.setError(lastName_error.getAsString());
                            if (height_error != null)
                                heightField.setError(height_error.getAsString());
                        }
                    }
                    showProgress(false);
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.d("server error", t.toString());
                    Toast.makeText(parent, R.string.response_fail_server, Toast.LENGTH_LONG).show();
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
