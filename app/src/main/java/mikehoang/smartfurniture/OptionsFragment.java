package mikehoang.smartfurniture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;

public class OptionsFragment extends Fragment {
    private ListView mOptionsObjects;
    private Option optionApi;
    private MainActivity parent;
    private View mProgressView;
    private View mOptionsList;
    private List<JsonObject> optionsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_options, container, false);
        mOptionsObjects = v.findViewById(R.id.options_objects);
        mProgressView = v.findViewById(R.id.options_list_progress);
        mOptionsList = v.findViewById(R.id.options_list);
        parent = (MainActivity) getActivity();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.8:8000/en/api/v1/").build();
        optionApi = retrofit.create(Option.class);

        optionsList = new ArrayList<JsonObject>();
        for (JsonElement option : parent.user.get("options_set").getAsJsonArray()) {
            optionsList.add(option.getAsJsonObject());
        }

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
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
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
            return view;
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mOptionsList.setVisibility(show ? View.GONE : View.VISIBLE);
            mOptionsList.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mOptionsList.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mOptionsList.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
