package mikehoang.smartfurniture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditProfileFragment extends Fragment {
    private EditText mUsernameView;
    private EditText mEmailView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mHeightView;
    private Button mSaveButton;
    private MainActivity parent;
    private User userApi;
    private View mProgressView;
    private View mEditProfileForm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mUsernameView = v.findViewById(R.id.username);
        mEmailView = v.findViewById(R.id.email);
        mFirstNameView = v.findViewById(R.id.first_name);
        mLastNameView = v.findViewById(R.id.last_name);
        mHeightView = v.findViewById(R.id.height);
        mSaveButton = v.findViewById(R.id.save_button);
        mProgressView = v.findViewById(R.id.edit_profile_progress);
        mEditProfileForm = v.findViewById(R.id.edit_profile_form);
        parent = (MainActivity) getActivity();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.8:8000/en/api/v1/").build();
        userApi = retrofit.create(User.class);

        mUsernameView.setText(parent.user.get("username").getAsString());
        mEmailView.setText(parent.user.get("email").getAsString());
        mFirstNameView.setText(parent.user.get("first_name").getAsString());
        mLastNameView.setText(parent.user.get("last_name").getAsString());
        try {
            parent.user.get("height").getAsJsonNull();
        } catch (IllegalStateException e) {
            mHeightView.setText(parent.user.get("height").getAsString());
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSave();
            }
        });
        return v;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mEditProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mEditProfileForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mEditProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mEditProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void attemptSave() {
        mUsernameView.setError(null);
        mEmailView.setError(null);
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mHeightView.setError(null);

        String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String height = mHeightView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(height)) {
            mHeightView.setError(getString(R.string.error_field_required));
            focusView = mHeightView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            userApi.editCurrentUser(Preferences.getAccessToken(parent),
                    username,
                    email,
                    firstName,
                    lastName,
                    Double.parseDouble(height)).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.body() != null) {
                            parent.getCurrentUser();
                            Toast.makeText(parent, "Successfully saved profile.", Toast.LENGTH_LONG).show();
                        } else if (response.errorBody() != null) {
                            JsonObject errorResponse = new JsonParser().parse(response.errorBody().string()).getAsJsonObject();
                            JsonElement non_field_error = errorResponse.get("non_field_errors");
                            JsonElement username_error = errorResponse.get("username");
                            JsonElement email_error = errorResponse.get("email");
                            JsonElement firstName_error = errorResponse.get("first_name");
                            JsonElement lastName_error = errorResponse.get("last_name");
                            JsonElement height_error = errorResponse.get("height");
                            if (non_field_error != null)
                                Toast.makeText(parent, non_field_error.getAsString(), Toast.LENGTH_LONG).show();
                            if (username_error != null)
                                mUsernameView.setError(username_error.getAsString());
                            if (email_error != null)
                                mEmailView.setError(email_error.getAsString());
                            if (firstName_error != null)
                                mFirstNameView.setError(firstName_error.getAsString());
                            if (lastName_error != null)
                                mLastNameView.setError(lastName_error.getAsString());
                            if (height_error != null)
                                mHeightView.setError(height_error.getAsString());
                        }
                    } catch (IOException e) {
                        Log.d("error", e.toString());
                        Toast.makeText(parent, "An error occurred.", Toast.LENGTH_LONG).show();
                    } finally {
                        showProgress(false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("server error", t.toString());
                    Toast.makeText(parent, "A server error occurred.", Toast.LENGTH_LONG).show();
                    showProgress(false);
                }
            });
        }
    }
}
