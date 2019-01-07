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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
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


public class OptionsFragment extends Fragment {
    private View progressBlock;
    private View mainBlock;
    private List<JsonObject> optionsList;
    private MainActivity parent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_options, container, false);

        ListView mOptionsObjects = v.findViewById(R.id.options_objects);
        progressBlock = v.findViewById(R.id.options_list_progress);
        mainBlock = v.findViewById(R.id.options_list);

        optionsList = new ArrayList<JsonObject>();

        parent = (MainActivity) getActivity();
        JsonObject user;
        if (parent.user != null)
            user = parent.user;
        else {
            String userData = Preferences.getValue(parent, "USER");
            user = new JsonParser().parse(userData).getAsJsonObject();
        }

        for (JsonElement option : user.get("options_set").getAsJsonArray())
            optionsList.add(option.getAsJsonObject());
        ListAdapter listAdapter = new ListAdapter();
        mOptionsObjects.setAdapter(listAdapter);
        return v;
    }

    class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return optionsList.size();
        }

        @Override
        public Object getItem(int i) {
            return optionsList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.options_obj, null);

            TextView name = (TextView) view.findViewById(R.id.name);
            TextView type = (TextView) view.findViewById(R.id.type);
            TextView height = (TextView) view.findViewById(R.id.height);
            TextView length = (TextView) view.findViewById(R.id.length);
            TextView width = (TextView) view.findViewById(R.id.width);
            TextView incline = (TextView) view.findViewById(R.id.incline);
            TextView temperature = (TextView) view.findViewById(R.id.temperature);
            TextView rigidity = (TextView) view.findViewById(R.id.rigidity);
            TextView massage = (TextView) view.findViewById(R.id.massage);

            JsonObject option = optionsList.get(i);
            String n = option.get("name").getAsString();
            String t = "Type: " + option.get("type").getAsString();
            String h = "Height: " + option.get("height").getAsString();
            String l = "Length: " + option.get("length").getAsString();
            String w = "Width: " + option.get("width").getAsString();
            String inc = "Incline: " + option.get("incline").getAsString();
            String temp = "Temperature: " + option.get("temperature").getAsString();
            String r = "Rigidity: " + option.get("rigidity").getAsString();
            String m = "Massage: " + option.get("massage").getAsString();

            name.setText(n);
            type.setText(t);
            height.setText(h);
            length.setText(l);
            width.setText(w);
            incline.setText(inc);
            temperature.setText(temp);
            rigidity.setText(r);
            massage.setText(m);

            Button deleteButton = (Button) view.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JsonObject o = optionsList.get(i);
                    deleteOptions(o.get("id").getAsInt(), i);
                }
            });

            return view;
        }
    }

    private void deleteOptions(final int id, final int i) {
        parent.api.deleteOptions(parent.key, id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    parent.getCurrentUser();
                    optionsList.remove(i);
                    Toast.makeText(parent, R.string.response_success_delete_options,
                            Toast.LENGTH_LONG).show();
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mainBlock.setVisibility(show ? View.GONE : View.VISIBLE);
            mainBlock.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mainBlock.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mainBlock.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
