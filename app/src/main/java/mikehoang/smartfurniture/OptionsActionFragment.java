package mikehoang.smartfurniture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OptionsActionFragment extends Fragment {
    private EditText nameField;
    private Spinner typeField;
    private EditText heightField;
    private EditText lengthField;
    private EditText widthField;
    private EditText inclineField;
    private EditText temperatureField;
    private Spinner rigidityField;
    private Spinner massageField;
    private View formBlock;
    private View progressBlock;
    private MainActivity parent;
    private JsonObject user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_options_add_edit, container, false);

        nameField = v.findViewById(R.id.name);
        typeField = v.findViewById(R.id.type);
        heightField = v.findViewById(R.id.height);
        lengthField = v.findViewById(R.id.length);
        widthField = v.findViewById(R.id.width);
        inclineField = v.findViewById(R.id.incline);
        temperatureField = v.findViewById(R.id.temperature);
        rigidityField = v.findViewById(R.id.rigidity);
        massageField = v.findViewById(R.id.massage);
        formBlock = v.findViewById(R.id.options_form);
        progressBlock = v.findViewById(R.id.options_progress);
        Button saveButton = v.findViewById(R.id.save_button);

        parent = (MainActivity) getActivity();
        if (parent.user != null)
            user = parent.user;
        else {
            String userData = Preferences.getValue(parent, "USER");
            user = new JsonParser().parse(userData).getAsJsonObject();
        }

        getSpinners();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.closeKeyboard(parent);
                attemptSave();
            }
        });
        return v;
    }

    private void getSpinners() {
        List<String> types = new ArrayList<String>();
        if (parent.furnitureTypes != null)
            for (JsonObject obj : parent.furnitureTypes)
                types.add(obj.get("name").getAsString());
        else {
            String typesData = Preferences.getValue(parent, "FURNITURE_TYPES");
            JsonArray arr = new JsonParser().parse(typesData).getAsJsonArray();
            for (JsonElement elj : arr)
                types.add(elj.getAsJsonObject().get("name").getAsString());
        }
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(parent,
                android.R.layout.simple_spinner_item, types);
        typeField.setAdapter(typesAdapter);

        List<String> massageList = new ArrayList<String>();
        if (parent.massageTypes != null)
            for (JsonObject obj : parent.massageTypes)
                massageList.add(obj.get("name").getAsString());
        else {
            String massageData = Preferences.getValue(parent, "MASSAGE_TYPES");
            JsonArray arr = new JsonParser().parse(massageData).getAsJsonArray();
            for (JsonElement elj : arr)
                massageList.add(elj.getAsJsonObject().get("name").getAsString());
        }
        ArrayAdapter<String> massageAdapter = new ArrayAdapter<String>(parent,
                android.R.layout.simple_spinner_item, massageList);
        massageField.setAdapter(massageAdapter);

        List<String> rigidityList = new ArrayList<String>();
        if (parent.rigidityTypes != null)
            for (JsonObject obj : parent.rigidityTypes)
                rigidityList.add(obj.get("name").getAsString());
        else {
            String rigidityData = Preferences.getValue(parent, "RIGIDITY_TYPES");
            JsonArray arr = new JsonParser().parse(rigidityData).getAsJsonArray();
            for (JsonElement elj : arr)
                rigidityList.add(elj.getAsJsonObject().get("name").getAsString());
        }
        ArrayAdapter<String> rigidityAdapter = new ArrayAdapter<String>(parent,
                android.R.layout.simple_spinner_item, rigidityList);
        rigidityField.setAdapter(rigidityAdapter);
    }

    private void attemptSave() {
        nameField.setError(null);
        heightField.setError(null);
        lengthField.setError(null);
        widthField.setError(null);
        inclineField.setError(null);
        temperatureField.setError(null);

        String name = nameField.getText().toString();
        String height = heightField.getText().toString();
        String width = widthField.getText().toString();
        String length = lengthField.getText().toString();
        String incline = inclineField.getText().toString();
        String temperature = temperatureField.getText().toString();
        String massage = massageField.getSelectedItem().toString();
        String type = typeField.getSelectedItem().toString();
        String rigidity = rigidityField.getSelectedItem().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            nameField.setError(getString(R.string.error_field_required));
            focusView = nameField;
            cancel = true;
        }

        if (TextUtils.isEmpty(height)) {
            heightField.setError(getString(R.string.error_field_required));
            focusView = heightField;
            cancel = true;
        }
        if (TextUtils.isEmpty(width)) {
            widthField.setError(getString(R.string.error_field_required));
            focusView = widthField;
            cancel = true;
        }

        if (TextUtils.isEmpty(length)) {
            lengthField.setError(getString(R.string.error_field_required));
            focusView = lengthField;
            cancel = true;
        }
        if (TextUtils.isEmpty(incline)) {
            inclineField.setError(getString(R.string.error_field_required));
            focusView = inclineField;
            cancel = true;
        }

        if (TextUtils.isEmpty(temperature)) {
            temperatureField.setError(getString(R.string.error_field_required));
            focusView = temperatureField;
            cancel = true;
        }

        if (cancel)
            focusView.requestFocus();
        else {
            showProgress(true);
            parent.api.createOptions(parent.key,
                    name,
                    type,
                    Double.parseDouble(height),
                    Double.parseDouble(length),
                    Double.parseDouble(width),
                    Double.parseDouble(incline),
                    Double.parseDouble(temperature),
                    rigidity,
                    massage,
                    user.get("id").getAsInt()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    if (response.body() != null) {
                        parent.getCurrentUser();
                        Toast.makeText(parent, R.string.response_success_options,
                                Toast.LENGTH_LONG).show();
                        parent.getUserAndRedirect(new OptionsFragment(), R.string.nav_item_options);

                    } else if (response.errorBody() != null) {
                        JsonElement res = MainActivity.getJsonResponse(response.errorBody(), parent);
                        if (res != null) {
                            JsonObject errorResponse = res.getAsJsonObject();
                            JsonElement non_field_error = errorResponse.get("non_field_errors");
                            JsonElement name_error = errorResponse.get("name");
                            JsonElement height_error = errorResponse.get("height");
                            JsonElement length_error = errorResponse.get("length");
                            JsonElement width_error = errorResponse.get("width");
                            JsonElement incline_error = errorResponse.get("incline");
                            JsonElement temperature_error = errorResponse.get("temperature");

                            if (non_field_error != null)
                                Toast.makeText(parent, non_field_error.getAsString(),
                                        Toast.LENGTH_LONG).show();
                            if (name_error != null)
                                nameField.setError(name_error.getAsString());
                            if (height_error != null)
                                heightField.setError(height_error.getAsString());
                            if (length_error != null)
                                lengthField.setError(length_error.getAsString());
                            if (width_error != null)
                                widthField.setError(width_error.getAsString());
                            if (incline_error != null)
                                inclineField.setError(incline_error.getAsString());
                            if (temperature_error != null)
                                temperatureField.setError(temperature_error.getAsString());
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
