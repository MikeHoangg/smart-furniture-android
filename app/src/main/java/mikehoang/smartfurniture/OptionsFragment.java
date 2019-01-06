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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;


public class OptionsFragment extends Fragment {
    private View progressBlock;
    private View mainBlock;
    private List<JsonObject> optionsList;

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

        MainActivity parent = (MainActivity) getActivity();
        if (parent != null)
            for (JsonElement option : parent.user.get("options_set").getAsJsonArray())
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
