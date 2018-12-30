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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FurnitureActionFragment extends Fragment {
    private EditText mCodeView;
    private EditText mBrandView;
    private Spinner mTypeView;
    private CheckBox mIsPublicView;
    private Button mSaveButton;
    private MainActivity parent;
    private Furniture furnitureApi;
    private View mProgressView;
    private View mFurnitureForm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_furniture_add_edit, container, false);
        mCodeView = v.findViewById(R.id.code);
        mBrandView = v.findViewById(R.id.brand);
        mTypeView = v.findViewById(R.id.type);
        mIsPublicView = v.findViewById(R.id.is_public);
        mSaveButton = v.findViewById(R.id.save_button);
        mProgressView = v.findViewById(R.id.furniture_progress);
        mFurnitureForm = v.findViewById(R.id.furniture_form);
        parent = (MainActivity) getActivity();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.8:8000/en/api/v1/").build();
        furnitureApi = retrofit.create(Furniture.class);

        List<String> types = new ArrayList<String>();
        for (JsonObject obj : parent.furnitureTypes)
            types.add(obj.get("name").getAsString());
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(parent, android.R.layout.simple_spinner_item, types);
        mTypeView.setAdapter(typesAdapter);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSave();
            }
        });
        return v;
    }

    private void attemptSave() {
        mCodeView.setError(null);
        mBrandView.setError(null);
        mIsPublicView.setError(null);

        String code = mCodeView.getText().toString();
        String brand = mBrandView.getText().toString();
        String type = mTypeView.getSelectedItem().toString();
        Boolean is_public = mIsPublicView.isChecked();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(code)) {
            mCodeView.setError(getString(R.string.error_field_required));
            focusView = mCodeView;
            cancel = true;
        }

        if (TextUtils.isEmpty(brand)) {
            mBrandView.setError(getString(R.string.error_field_required));
            focusView = mBrandView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            furnitureApi.createFurniture(Preferences.getAccessToken(parent),
                    code,
                    brand,
                    type,
                    is_public,
                    parent.user.get("id").getAsInt()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.body() != null) {
                            parent.getCurrentUser();
                            Toast.makeText(parent, "Successfully saved furniture.", Toast.LENGTH_LONG).show();
                        } else if (response.errorBody() != null) {
                            JsonObject errorResponse = new JsonParser().parse(response.errorBody().string()).getAsJsonObject();
                            JsonElement non_field_error = errorResponse.get("non_field_errors");
                            JsonElement code_error = errorResponse.get("code");
                            JsonElement brand_error = errorResponse.get("brand");
                            JsonElement isPublic_error = errorResponse.get("is_public");
                            if (non_field_error != null)
                                Toast.makeText(parent, non_field_error.getAsString(), Toast.LENGTH_LONG).show();
                            if (code_error != null)
                                mCodeView.setError(code_error.getAsString());
                            if (brand_error != null)
                                mBrandView.setError(brand_error.getAsString());
                            if (isPublic_error != null)
                                mIsPublicView.setError(isPublic_error.getAsString());
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mFurnitureForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mFurnitureForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFurnitureForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mFurnitureForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
