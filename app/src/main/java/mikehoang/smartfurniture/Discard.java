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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
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

public class Discard extends Fragment {
    private Spinner furnitureField;
    private MainActivity parent;
    private View progressBlock;
    private View formBlock;
    private List<JsonObject> furnitureList;
    private JsonObject user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_discard, container, false);

        furnitureField = v.findViewById(R.id.furniture);
        progressBlock = v.findViewById(R.id.discard_progress);
        formBlock = v.findViewById(R.id.discard_form);
        TextView errorBlock = v.findViewById(R.id.error);
        Button discardButton = v.findViewById(R.id.discard_button);
        View furnitureBlock = v.findViewById(R.id.furniture_block);

        parent = (MainActivity) getActivity();
        if (parent.user != null)
            user = parent.user;
        else {
            String userData = Preferences.getValue(parent, "USER");
            user = new JsonParser().parse(userData).getAsJsonObject();
        }

        furnitureList = new ArrayList<JsonObject>();
        List<String> furniture = new ArrayList<String>();
        for (JsonElement el : user.get("current_furniture").getAsJsonArray()) {
            JsonObject obj = el.getAsJsonObject();
            String s = obj.get("type").getAsString() + " - " + obj.get("code").getAsString();
            if (!furniture.contains(s)) {
                furniture.add(s);
                furnitureList.add(obj);
            }
        }
        ArrayAdapter<String> furnitureAdapter = new ArrayAdapter<String>(parent,
                android.R.layout.simple_spinner_item, furniture);
        furnitureField.setAdapter(furnitureAdapter);

        furnitureBlock.setVisibility(furniture.size() > 0 ? View.VISIBLE : View.GONE);
        discardButton.setVisibility(furniture.size() > 0 ? View.VISIBLE : View.GONE);
        errorBlock.setVisibility(furniture.size() > 0 ? View.GONE : View.VISIBLE);

        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDiscard();
            }
        });
        return v;
    }

    private void attemptDiscard() {
        String furniture = furnitureField.getSelectedItem().toString();

        int furnitureId = 0;

        for (JsonObject f : furnitureList) {
            String s = f.get("type").getAsString() + " - " + f.get("code").getAsString();
            if (s.equals(furniture)) {
                furnitureId = Integer.parseInt(f.get("id").getAsString());
                break;
            }
        }

        showProgress(true);
        parent.api.discardOptions(parent.key,
                Integer.parseInt(user.get("id").getAsString()),
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
