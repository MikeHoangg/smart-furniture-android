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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FurnitureAddFragment extends Fragment {
    private EditText codeField;
    private EditText brandField;
    private Spinner typeField;
    private CheckBox isPublicField;
    private MainActivity parent;
    private View progressBlock;
    private View formBlock;
    public JsonObject user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_furniture_add_edit,
                container, false);

        codeField = v.findViewById(R.id.code);
        brandField = v.findViewById(R.id.brand);
        typeField = v.findViewById(R.id.type);
        isPublicField = v.findViewById(R.id.is_public);
        progressBlock = v.findViewById(R.id.furniture_progress);
        formBlock = v.findViewById(R.id.furniture_form);
        Button mSaveButton = v.findViewById(R.id.save_button);

        parent = (MainActivity) getActivity();
        if (parent.user != null)
            user = parent.user;
        else {
            String userData = Preferences.getValue(parent, "USER");
            user = new JsonParser().parse(userData).getAsJsonObject();
        }

        List<String> types = new ArrayList<String>();
        if (parent.furnitureTypes != null)
            for (JsonObject obj : parent.furnitureTypes)
                types.add(obj.get("name").getAsString());
        else {
            String furnitureTypesData = Preferences.getValue(parent, "FURNITURE_TYPES");
            for (JsonElement el : new JsonParser().parse(furnitureTypesData).getAsJsonArray())
                types.add(el.getAsJsonObject().get("name").getAsString());
        }
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(parent,
                android.R.layout.simple_spinner_item, types);
        typeField.setAdapter(typesAdapter);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.closeKeyboard(parent);
                attemptSave();
            }
        });
        return v;
    }

    private void attemptSave() {
        codeField.setError(null);
        brandField.setError(null);
        isPublicField.setError(null);

        String code = codeField.getText().toString();
        String brand = brandField.getText().toString();
        String type = typeField.getSelectedItem().toString();
        Boolean is_public = isPublicField.isChecked();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(code)) {
            codeField.setError(getString(R.string.error_field_required));
            focusView = codeField;
            cancel = true;
        }

        if (TextUtils.isEmpty(brand)) {
            brandField.setError(getString(R.string.error_field_required));
            focusView = brandField;
            cancel = true;
        }

        if (cancel)
            focusView.requestFocus();
        else {
            showProgress(true);
            parent.api.createFurniture(parent.key,
                    code,
                    brand,
                    type,
                    is_public,
                    user.get("id").getAsInt()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    if (response.body() != null) {
                        Toast.makeText(parent, R.string.response_success_furniture,
                                Toast.LENGTH_LONG).show();
                        parent.getUserAndRedirect(new FurnitureFragment(), R.string.nav_item_furniture);
                    } else if (response.errorBody() != null) {
                        JsonElement res = MainActivity.getJsonResponse(response.errorBody(), parent);
                        if (res != null) {
                            JsonObject errorResponse = res.getAsJsonObject();
                            JsonElement non_field_error = errorResponse.get("non_field_errors");
                            JsonElement code_error = errorResponse.get("code");
                            JsonElement brand_error = errorResponse.get("brand");
                            JsonElement isPublic_error = errorResponse.get("is_public");

                            if (non_field_error != null)
                                Toast.makeText(parent, non_field_error.getAsString(),
                                        Toast.LENGTH_LONG).show();
                            if (code_error != null)
                                codeField.setError(code_error.getAsString());
                            if (brand_error != null)
                                brandField.setError(brand_error.getAsString());
                            if (isPublic_error != null)
                                isPublicField.setError(isPublic_error.getAsString());
                        }
                    }
                    showProgress(false);
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.d("server error", t.toString());
                    Toast.makeText(parent, R.string.response_fail_server,
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
