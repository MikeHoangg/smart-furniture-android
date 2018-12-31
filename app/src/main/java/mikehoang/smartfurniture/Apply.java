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
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Apply extends Fragment {
    private Spinner mTypeView;
    private Spinner mFurnitureView;
    private Spinner mOptionsView;
    private TextView mErrorView;
    private Button mApplyButton;
    private ApplyOptions applyApi;
    private MainActivity parent;
    private View mProgressView;
    private List<String> types;
    private View mApplyForm;
    private List<JsonObject> optionsObjList;
    private List<JsonObject> furnitureObjList;
    private View optionsBlock;
    private View furnitureBlock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_apply, container, false);
        optionsBlock = v.findViewById(R.id.options_block);
        furnitureBlock = v.findViewById(R.id.furniture_block);
        mTypeView = v.findViewById(R.id.type);
        mFurnitureView = v.findViewById(R.id.furniture);
        mApplyButton = v.findViewById(R.id.apply_button);
        mOptionsView = v.findViewById(R.id.options);
        mErrorView = v.findViewById(R.id.error);
        mProgressView = v.findViewById(R.id.apply_progress);
        mApplyForm = v.findViewById(R.id.apply_form);
        parent = (MainActivity) getActivity();

        furnitureObjList = new ArrayList<JsonObject>();
        optionsObjList = new ArrayList<JsonObject>();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.8:8000/en/api/v1/").build();
        applyApi = retrofit.create(ApplyOptions.class);

        types = new ArrayList<String>();
        for (JsonObject obj : parent.furnitureTypes)
            types.add(obj.get("name").getAsString());
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(parent, android.R.layout.simple_spinner_item, types);
        mTypeView.setAdapter(typesAdapter);

        getSpinners(types.get(0));

        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptApply();
            }
        });

        mTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSpinners(types.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mErrorView.setText(R.string.error_field_required);
                mErrorView.setVisibility(View.VISIBLE);
                optionsBlock.setVisibility(View.GONE);
                furnitureBlock.findViewById(R.id.furniture_block).setVisibility(View.GONE);
                mApplyButton.setVisibility(View.GONE);
            }
        });
        return v;
    }

    private void getSpinners(String type) {
        List<String> furniture = getFurnitureList(type);
        List<String> options = getOptionsList(type);
        Boolean error = false;
        if (furniture.size() > 0) {
            ArrayAdapter<String> furnitureAdapter = new ArrayAdapter<String>(parent, android.R.layout.simple_spinner_item, furniture);
            mFurnitureView.setAdapter(furnitureAdapter);
        } else {
            error = true;
            mErrorView.setText(R.string.error_no_furniture);
        }
        if (options.size() > 0) {
            ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(parent, android.R.layout.simple_spinner_item, options);
            mOptionsView.setAdapter(optionsAdapter);
        } else {
            error = true;
            mErrorView.setText(R.string.error_no_options);
        }
        mErrorView.setVisibility(error ? View.VISIBLE : View.GONE);
        optionsBlock.setVisibility(error ? View.GONE : View.VISIBLE);
        furnitureBlock.setVisibility(error ? View.GONE : View.VISIBLE);
        mApplyButton.setVisibility(error ? View.GONE : View.VISIBLE);
    }

    private List<String> getFurnitureList(String type) {
        List<String> furnitureList = new ArrayList<String>();
        for (JsonElement furniture : parent.user.get("owned_furniture").getAsJsonArray()) {
            JsonObject f = furniture.getAsJsonObject();
            if (f.get("type").getAsString().equals(type)) {
                String res = f.get("type").getAsString() + " - " + f.get("code").getAsString();
                if (!furnitureList.contains(res)){
                    furnitureList.add(res);
                    furnitureObjList.add(f);
                }
            }
        }
        for (JsonElement furniture : parent.user.get("allowed_furniture").getAsJsonArray()) {
            JsonObject f = furniture.getAsJsonObject();
            if (f.get("type").getAsString().equals(type)) {
                String res = f.get("type").getAsString() + " - " + f.get("code").getAsString();
                if (!furnitureList.contains(res)){
                    furnitureList.add(res);
                    furnitureObjList.add(f);
                }
            }
        }
        for (JsonElement furniture : parent.user.get("current_furniture").getAsJsonArray()) {
            JsonObject f = furniture.getAsJsonObject();
            if (f.get("type").getAsString().equals(type)) {
                String res = f.get("type").getAsString() + " - " + f.get("code").getAsString();
                if (!furnitureList.contains(res)){
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mApplyForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mApplyForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mApplyForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mApplyForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void attemptApply() {
        String furniture = mFurnitureView.getSelectedItem().toString();
        String options = mOptionsView.getSelectedItem().toString();

        int furnitureId = 0;
        int optionsId = 0;

        for (JsonObject o : optionsObjList) {
            if (o.get("name").getAsString().equals(options)) {
                optionsId = Integer.parseInt(o.get("id").getAsString());
                break;
            }
        }

        for (JsonObject f : furnitureObjList) {
            String s = f.get("type").getAsString() + " - " + f.get("code").getAsString();
            if (s.equals(furniture)) {
                furnitureId = Integer.parseInt(f.get("id").getAsString());
                break;
            }
        }

        showProgress(true);
        applyApi.applyOptions(Preferences.getAccessToken(parent),
                optionsId,
                furnitureId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        JsonObject successResponse = new JsonParser().parse(response.body().string()).getAsJsonObject();
                        parent.getCurrentUser();
                        JsonElement detail = successResponse.get("detail");
                        if (detail != null)
                            Toast.makeText(parent, detail.getAsString(), Toast.LENGTH_LONG).show();
                    } else if (response.errorBody() != null) {
                        JsonObject errorResponse = new JsonParser().parse(response.errorBody().string()).getAsJsonObject();
                        JsonElement non_field_error = errorResponse.get("non_field_errors");
                        JsonElement detail = errorResponse.get("detail");

                        if (non_field_error != null)
                            Toast.makeText(parent, non_field_error.getAsString(), Toast.LENGTH_LONG).show();
                        if (detail != null)
                            Toast.makeText(parent, detail.getAsString(), Toast.LENGTH_LONG).show();
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
