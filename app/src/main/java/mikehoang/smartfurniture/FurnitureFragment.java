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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Retrofit;

public class FurnitureFragment extends Fragment {
    private ListView mFurnitureObjects;
    private Furniture furnitureApi;
    private MainActivity parent;
    private View mProgressView;
    private View mFurnitureList;
    private List<JsonObject> furnitureList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_furniture, container, false);
        mFurnitureObjects = v.findViewById(R.id.furniture_objects);
        mProgressView = v.findViewById(R.id.furniture_list_progress);
        mFurnitureList = v.findViewById(R.id.furniture_list);
        parent = (MainActivity) getActivity();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.8:8000/en/api/v1/").build();
        furnitureApi = retrofit.create(Furniture.class);

        furnitureList = new ArrayList<JsonObject>();
        for (JsonElement furniture : parent.user.get("owned_furniture").getAsJsonArray()) {
            if (!furnitureList.contains(furniture.getAsJsonObject()))
                furnitureList.add(furniture.getAsJsonObject());
        }
        for (JsonElement furniture : parent.user.get("allowed_furniture").getAsJsonArray()) {
            if (!furnitureList.contains(furniture.getAsJsonObject()))
                furnitureList.add(furniture.getAsJsonObject());
        }
        for (JsonElement furniture : parent.user.get("current_furniture").getAsJsonArray()) {
            if (!furnitureList.contains(furniture.getAsJsonObject()))
                furnitureList.add(furniture.getAsJsonObject());
        }

        ListAdapter listAdapter = new ListAdapter();
        mFurnitureObjects.setAdapter(listAdapter);
        return v;
    }

    public static <T> T mostCommon(List<T> list, T def) {
        Map<T, Integer> map = new HashMap<>();
        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }
        Map.Entry<T, Integer> max = null;
        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue()) {
                max = e;
            }
        }
        if (max != null)
            return max.getKey();
        else
            return def;
    }

    class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return furnitureList.size();
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
            view = getLayoutInflater().inflate(R.layout.furniture_obj, null);

            TextView typeCode = (TextView) view.findViewById(R.id.type_code);
            TextView height = (TextView) view.findViewById(R.id.height);
            TextView length = (TextView) view.findViewById(R.id.length);
            TextView width = (TextView) view.findViewById(R.id.width);
            TextView incline = (TextView) view.findViewById(R.id.incline);
            TextView temperature = (TextView) view.findViewById(R.id.temperature);
            TextView rigidity = (TextView) view.findViewById(R.id.rigidity);
            TextView massage = (TextView) view.findViewById(R.id.massage);
            TextView owner = (TextView) view.findViewById(R.id.owner);

            JsonObject furniture = furnitureList.get(i);
            String tc = furniture.get("type").getAsString() + " - " + furniture.get("code").getAsString();
            String o = "Owner: " + furniture.get("owner").getAsJsonObject().get("username").getAsString();
            JsonArray options = furniture.get("current_options").getAsJsonArray();

            List<String> massageCount = new ArrayList<String>();
            List<String> rigidityCount = new ArrayList<String>();

            Double heightRes = 0.0;
            Double lengthRes = 0.0;
            Double widthRes = 0.0;
            Double inclineRes = 0.0;
            Double temperatureRes = 0.0;

            for (JsonElement el : options) {
                JsonObject option = el.getAsJsonObject();
                heightRes += Double.parseDouble(option.get("height").getAsString());
                lengthRes += Double.parseDouble(option.get("length").getAsString());
                widthRes += Double.parseDouble(option.get("width").getAsString());
                inclineRes += Double.parseDouble(option.get("incline").getAsString());
                temperatureRes += Double.parseDouble(option.get("temperature").getAsString());
                rigidityCount.add(option.get("rigidity").getAsString());
                massageCount.add(option.get("massage").getAsString());
            }
            if (options.size() > 0) {
                heightRes /= options.size();
                lengthRes /= options.size();
                widthRes /= options.size();
                inclineRes /= options.size();
                temperatureRes /= options.size();
            }
            String h = "Height: " + heightRes;
            String l = "Length: " + lengthRes;
            String w = "Width: " + widthRes;
            String inc = "Incline: " + inclineRes;
            String t = "Temperature: " + temperatureRes;
            String r = "Rigidity: " + mostCommon(rigidityCount, "medium");
            String m = "Massage: " + mostCommon(massageCount, "none");

            typeCode.setText(tc);
            owner.setText(o);
            height.setText(h);
            length.setText(l);
            width.setText(w);
            incline.setText(inc);
            temperature.setText(t);
            rigidity.setText(r);
            massage.setText(m);
            return view;
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mFurnitureList.setVisibility(show ? View.GONE : View.VISIBLE);
            mFurnitureList.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFurnitureList.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mFurnitureList.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
