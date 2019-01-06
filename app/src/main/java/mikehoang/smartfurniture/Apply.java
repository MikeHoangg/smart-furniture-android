package mikehoang.smartfurniture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Apply extends Fragment {
    private Spinner furnitureField;
    private Spinner optionsField;
    private TextView errorBlock;
    private Button applyButton;
    private MainActivity parent;
    private View progressBlock;
    private View formBlock;
    private View optionsBlock;
    private View furnitureBlock;
    private List<String> types;
    private List<JsonObject> optionsObjList;
    private List<JsonObject> furnitureObjList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_apply, container, false);

        optionsBlock = v.findViewById(R.id.options_block);
        furnitureBlock = v.findViewById(R.id.furniture_block);
        furnitureField = v.findViewById(R.id.furniture);
        optionsField = v.findViewById(R.id.options);
        applyButton = v.findViewById(R.id.apply_button);
        errorBlock = v.findViewById(R.id.error);
        progressBlock = v.findViewById(R.id.apply_progress);
        formBlock = v.findViewById(R.id.apply_form);
        parent = (MainActivity) getActivity();

        types = new ArrayList<String>();
        furnitureObjList = new ArrayList<JsonObject>();
        optionsObjList = new ArrayList<JsonObject>();

        Spinner typeField = v.findViewById(R.id.type);

        for (JsonObject obj : parent.furnitureTypes)
            types.add(obj.get("name").getAsString());
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(parent,
                android.R.layout.simple_spinner_item, types);
        typeField.setAdapter(typesAdapter);

        getSpinners(types.get(0));

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptApply();
            }
        });

        typeField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSpinners(types.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                errorBlock.setText(R.string.error_field_required);
                errorBlock.setVisibility(View.VISIBLE);
                optionsBlock.setVisibility(View.GONE);
                furnitureBlock.findViewById(R.id.furniture_block).setVisibility(View.GONE);
                applyButton.setVisibility(View.GONE);
            }
        });
        return v;
    }

    private void getSpinners(String type) {
        List<String> furniture = getFurnitureList(type);
        List<String> options = getOptionsList(type);
        Boolean error = false;
        if (furniture.size() > 0) {
            ArrayAdapter<String> furnitureAdapter = new ArrayAdapter<String>(parent,
                    android.R.layout.simple_spinner_item, furniture);
            furnitureField.setAdapter(furnitureAdapter);
        } else {
            error = true;
            errorBlock.setText(R.string.error_no_furniture);
        }
        if (options.size() > 0) {
            ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(parent,
                    android.R.layout.simple_spinner_item, options);
            optionsField.setAdapter(optionsAdapter);
        } else {
            error = true;
            errorBlock.setText(R.string.error_no_options);
        }
        errorBlock.setVisibility(error ? View.VISIBLE : View.GONE);
        optionsBlock.setVisibility(error ? View.GONE : View.VISIBLE);
        furnitureBlock.setVisibility(error ? View.GONE : View.VISIBLE);
        applyButton.setVisibility(error ? View.GONE : View.VISIBLE);
    }

    private List<String> getFurnitureList(String type) {
        List<String> furnitureList = new ArrayList<String>();
        for (String list : API.FURNITURE_LIST)
            for (JsonElement furniture : parent.user.get(list).getAsJsonArray()) {
                JsonObject f = furniture.getAsJsonObject();
                if (f.get("type").getAsString().equals(type)) {
                    String res = f.get("type").getAsString() + " - " + f.get("code").getAsString();
                    if (!furnitureList.contains(res)) {
                        furnitureList.add(res);
                        furnitureObjList.add(f);
                    }
                }
            }
        return furnitureList;
    }

    private List<String> getOptionsList(String type) {
        List<String> optionsList = new ArrayList<String>();
        for (JsonElement option : parent.user.get("options_set").getAsJsonArray()) {
            JsonObject f = option.getAsJsonObject();
            if (f.get("type").getAsString().equals(type)) {
                String res = f.get("name").getAsString();
                optionsList.add(res);
                optionsObjList.add(f);
            }
        }
        return optionsList;
    }

    private void attemptApply() {
        String furniture = furnitureField.getSelectedItem().toString();
        String options = optionsField.getSelectedItem().toString();

        int furnitureId = 0;
        int optionsId = 0;

        for (JsonObject o : optionsObjList)
            if (o.get("name").getAsString().equals(options)) {
                optionsId = o.get("id").getAsInt();
                break;
            }

        for (JsonObject f : furnitureObjList) {
            String s = f.get("type").getAsString() + " - " + f.get("code").getAsString();
            if (s.equals(furniture)) {
                furnitureId = f.get("id").getAsInt();
                break;
            }
        }

        showProgress(true);
        parent.api.applyOptions(parent.key,
                optionsId,
                furnitureId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    JsonElement res = MainActivity.getJsonResponse(response.body(), parent);
                    if (res != null) {
                        JsonObject successResponse = res.getAsJsonObject();
                        parent.getCurrentUser();
                        JsonElement detail = successResponse.get("detail");
                        if (detail != null)
                            Toast.makeText(parent, detail.getAsString(), Toast.LENGTH_LONG).show();
                    }
                } else if (response.errorBody() != null) {
                    JsonElement res = MainActivity.getJsonResponse(response.errorBody(), parent);
                    if (res != null) {
                        JsonObject errorResponse = res.getAsJsonObject();
                        JsonElement non_field_error = errorResponse.get("non_field_errors");
                        JsonElement detail = errorResponse.get("detail");

                        if (non_field_error != null)
                            Toast.makeText(parent, non_field_error.getAsString(),
                                    Toast.LENGTH_LONG).show();
                        if (detail != null)
                            Toast.makeText(parent, detail.getAsString(), Toast.LENGTH_LONG).show();
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
