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

public class OptionsActionFragment extends Fragment {
    private EditText mNameView;
    private Spinner mTypeView;
    private EditText mHeightView;
    private EditText mLengthView;
    private EditText mWidthView;
    private EditText mInclineView;
    private EditText mTemperatureView;
    private Spinner mRigidityView;
    private Spinner mMassageView;
    private Button mSaveButton;
    private View mOptionsForm;
    private View mProgressView;
    private MainActivity parent;
    private Option optionsApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_options_add_edit, container, false);
        mNameView = v.findViewById(R.id.name);
        mTypeView = v.findViewById(R.id.type);
        mHeightView = v.findViewById(R.id.height);
        mLengthView = v.findViewById(R.id.length);
        mWidthView = v.findViewById(R.id.width);
        mInclineView = v.findViewById(R.id.incline);
        mTemperatureView = v.findViewById(R.id.temperature);
        mRigidityView = v.findViewById(R.id.rigidity);
        mMassageView = v.findViewById(R.id.massage);
        mSaveButton = v.findViewById(R.id.save_button);
        mOptionsForm = v.findViewById(R.id.options_form);
        mProgressView = v.findViewById(R.id.options_progress);
        parent = (MainActivity) getActivity();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.8:8000/en/api/v1/").build();
        optionsApi = retrofit.create(Option.class);

        List<String> massageTypes = new ArrayList<String>();
        for (JsonObject obj : parent.massageTypes)
            massageTypes.add(obj.get("name").getAsString());
        ArrayAdapter<String> massageAdapter = new ArrayAdapter<String>(parent, android.R.layout.simple_spinner_item, massageTypes);
        mMassageView.setAdapter(massageAdapter);

        List<String> rigidityTypes = new ArrayList<String>();
        for (JsonObject obj : parent.rigidityTypes)
            rigidityTypes.add(obj.get("name").getAsString());
        ArrayAdapter<String> rigidityAdapter = new ArrayAdapter<String>(parent, android.R.layout.simple_spinner_item, rigidityTypes);
        mRigidityView.setAdapter(rigidityAdapter);

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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mOptionsForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mOptionsForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mOptionsForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mOptionsForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void attemptSave() {
        mNameView.setError(null);
        mHeightView.setError(null);
        mLengthView.setError(null);
        mWidthView.setError(null);
        mInclineView.setError(null);
        mTemperatureView.setError(null);

        String name = mNameView.getText().toString();
        String height = mHeightView.getText().toString();
        String width = mWidthView.getText().toString();
        String length = mLengthView.getText().toString();
        String incline = mInclineView.getText().toString();
        String temperature = mTemperatureView.getText().toString();
        String massage = mMassageView.getSelectedItem().toString();
        String type = mTypeView.getSelectedItem().toString();
        String rigidity = mRigidityView.getSelectedItem().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(height)) {
            mHeightView.setError(getString(R.string.error_field_required));
            focusView = mHeightView;
            cancel = true;
        }
        if (TextUtils.isEmpty(width)) {
            mWidthView.setError(getString(R.string.error_field_required));
            focusView = mWidthView;
            cancel = true;
        }

        if (TextUtils.isEmpty(length)) {
            mLengthView.setError(getString(R.string.error_field_required));
            focusView = mLengthView;
            cancel = true;
        }
        if (TextUtils.isEmpty(incline)) {
            mInclineView.setError(getString(R.string.error_field_required));
            focusView = mInclineView;
            cancel = true;
        }

        if (TextUtils.isEmpty(temperature)) {
            mTemperatureView.setError(getString(R.string.error_field_required));
            focusView = mTemperatureView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            optionsApi.createOptions(Preferences.getAccessToken(parent),
                    name,
                    type,
                    Double.parseDouble(height),
                    Double.parseDouble(length),
                    Double.parseDouble(width),
                    Double.parseDouble(incline),
                    Double.parseDouble(temperature),
                    rigidity,
                    massage,
                    parent.user.get("id").getAsInt()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.body() != null) {
                            parent.getCurrentUser();
                            Toast.makeText(parent, "Successfully saved options.", Toast.LENGTH_LONG).show();
                        } else if (response.errorBody() != null) {
                            JsonObject errorResponse = new JsonParser().parse(response.errorBody().string()).getAsJsonObject();
                            JsonElement non_field_error = errorResponse.get("non_field_errors");
                            JsonElement name_error = errorResponse.get("name");
                            JsonElement height_error = errorResponse.get("height");
                            JsonElement length_error = errorResponse.get("length");
                            JsonElement width_error = errorResponse.get("width");
                            JsonElement incline_error = errorResponse.get("incline");
                            JsonElement temperature_error = errorResponse.get("temperature");
                            if (non_field_error != null)
                                Toast.makeText(parent, non_field_error.getAsString(), Toast.LENGTH_LONG).show();
                            if (name_error != null)
                                mNameView.setError(name_error.getAsString());
                            if (height_error != null)
                                mHeightView.setError(height_error.getAsString());
                            if (length_error != null)
                                mLengthView.setError(length_error.getAsString());
                            if (width_error != null)
                                mWidthView.setError(width_error.getAsString());
                            if (incline_error != null)
                                mInclineView.setError(incline_error.getAsString());
                            if (temperature_error != null)
                                mTemperatureView.setError(temperature_error.getAsString());

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
