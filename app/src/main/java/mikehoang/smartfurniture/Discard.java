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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Discard extends Fragment {
    private Spinner mFurnitureView;
    private TextView mErrorView;
    private Button mDiscardButton;
    private ApplyOptions applyApi;
    private MainActivity parent;
    private View mProgressView;
    private View mDiscardForm;
    private List<JsonObject> furnitureObjList;
    private View furnitureBlock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_discard, container, false);
        mFurnitureView = v.findViewById(R.id.furniture);
        mErrorView = v.findViewById(R.id.error);
        mDiscardButton = v.findViewById(R.id.discard_button);
        mProgressView = v.findViewById(R.id.discard_progress);
        mDiscardForm = v.findViewById(R.id.discard_form);
        furnitureBlock = v.findViewById(R.id.furniture_block);

        parent = (MainActivity) getActivity();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.8:8000/en/api/v1/").build();
        applyApi = retrofit.create(ApplyOptions.class);

        furnitureObjList = new ArrayList<JsonObject>();
        List<String> furniture = new ArrayList<String>();
        for (JsonElement el : parent.user.get("current_furniture").getAsJsonArray()) {
            JsonObject obj = el.getAsJsonObject();
            String s = obj.get("type").getAsString() + " - " + obj.get("code").getAsString();
            if (!furniture.contains(s)) {
                furniture.add(s);
                furnitureObjList.add(obj);
            }
        }
        if (furniture.size() > 0) {
            furnitureBlock.setVisibility(View.VISIBLE);
            mDiscardButton.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
            ArrayAdapter<String> furnitureAdapter = new ArrayAdapter<String>(parent, android.R.layout.simple_spinner_item, furniture);
            mFurnitureView.setAdapter(furnitureAdapter);
        } else {
            furnitureBlock.setVisibility(View.GONE);
            mDiscardButton.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);
            mErrorView.setText(R.string.error_no_curr_furniture);
        }

        mDiscardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDiscard();
            }
        });
        return v;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDiscardForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mDiscardForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDiscardForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mDiscardForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void attemptDiscard() {
        String furniture = mFurnitureView.getSelectedItem().toString();

        int furnitureId = 0;


        for (JsonObject f : furnitureObjList) {
            String s = f.get("type").getAsString() + " - " + f.get("code").getAsString();
            if (s.equals(furniture)) {
                furnitureId = Integer.parseInt(f.get("id").getAsString());
                break;
            }
        }

        showProgress(true);
        applyApi.discardOptions(Preferences.getAccessToken(parent),
                Integer.parseInt(parent.user.get("id").getAsString()),
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
