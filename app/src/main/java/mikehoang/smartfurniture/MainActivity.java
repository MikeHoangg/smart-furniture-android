package mikehoang.smartfurniture;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public List<JsonObject> furnitureTypes;
    public List<JsonObject> rigidityTypes;
    public List<JsonObject> massageTypes;
    public JsonObject user;
    public API api;
    public String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        key = Preferences.getValue(MainActivity.this, "AUTH_TOKEN");

        Retrofit retrofit = new Retrofit.Builder().baseUrl(API.BASE_URL).build();
        api = retrofit.create(API.class);

        getFurnitureTypes();
        getRigidityMassageTypes();

        if (savedInstanceState == null)
            getUserAndRedirect(new FurnitureFragment(), R.string.nav_item_furniture);
        else
            getCurrentUser();

    }

    public static JsonElement getJsonResponse(final ResponseBody obj, final AppCompatActivity activity) {
        try {
            return new JsonParser().parse(obj.string());
        } catch (IOException e) {
            Log.d("error", e.toString());
            Toast.makeText(activity, R.string.error_unknown,
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public static void closeKeyboard(AppCompatActivity act) {
        View view = act.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void getCurrentUser() {
        api.getCurrentUser(key).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    JsonElement res = getJsonResponse(response.body(), MainActivity.this);
                    if (res != null) {
                        user = res.getAsJsonObject();
                        Preferences.setValue(MainActivity.this, "USER",
                                user.toString());
                        TextView username = (TextView) findViewById(R.id.text_username);
                        username.setText(user.get("username").getAsString());

                        TextView email = (TextView) findViewById(R.id.text_email);
                        email.setText(user.get("email").getAsString());

                        try {
                            user.get("image").getAsJsonNull();
                        } catch (IllegalStateException e) {
                            Log.d("error", e.toString());
                            ImageView image = (ImageView) findViewById(R.id.image_image);
                            Picasso.get().load(user.get("image").getAsString()).into(image);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d("server error", t.toString());
                Toast.makeText(MainActivity.this, R.string.response_fail_server,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getUserAndRedirect(final Fragment frag, final int title) {
        api.getCurrentUser(key).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.body() != null) {
                    JsonElement res = getJsonResponse(response.body(), MainActivity.this);
                    if (res != null) {
                        user = res.getAsJsonObject();
                        Preferences.setValue(MainActivity.this, "USER",
                                user.toString());
                        TextView username = (TextView) findViewById(R.id.text_username);
                        username.setText(user.get("username").getAsString());

                        TextView email = (TextView) findViewById(R.id.text_email);
                        email.setText(user.get("email").getAsString());

                        try {
                            user.get("image").getAsJsonNull();
                        } catch (IllegalStateException e) {
                            Log.d("error", e.toString());
                            ImageView image = (ImageView) findViewById(R.id.image_image);
                            Picasso.get().load(user.get("image").getAsString()).into(image);
                        }
                        MainActivity.this.setTitle(title);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().
                                replace(R.id.fragment, frag).commit();
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d("server error", t.toString());
                Toast.makeText(MainActivity.this, R.string.response_fail_server,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getRigidityMassageTypes() {
        api.getRigidityMassageTypes().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                rigidityTypes = new ArrayList<JsonObject>();
                massageTypes = new ArrayList<JsonObject>();
                if (response.body() != null) {
                    JsonElement res = getJsonResponse(response.body(), MainActivity.this);
                    if (res != null) {
                        JsonArray list = res.getAsJsonArray();

                        for (JsonElement el : list) {
                            JsonObject obj = el.getAsJsonObject();
                            if (obj.get("type").getAsString().equals("massage"))
                                massageTypes.add(obj);
                            else
                                rigidityTypes.add(obj);
                            Preferences.setValue(MainActivity.this,
                                    "MASSAGE_TYPES", massageTypes.toString());
                            Preferences.setValue(MainActivity.this,
                                    "RIGIDITY_TYPES", rigidityTypes.toString());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d("server error", t.toString());
            }
        });
    }

    private void getFurnitureTypes() {
        api.getFurnitureTypes().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                furnitureTypes = new ArrayList<JsonObject>();
                if (response.body() != null) {
                    JsonElement res = getJsonResponse(response.body(), MainActivity.this);
                    if (res != null) {
                        JsonArray list = res.getAsJsonArray();
                        Preferences.setValue(MainActivity.this,
                                "FURNITURE_TYPES", list.toString());
                        for (JsonElement el : list) {
                            JsonObject obj = el.getAsJsonObject();
                            furnitureTypes.add(obj);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d("server error", t.toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (user != null) {
            switch (id) {
                case R.id.nav_apply:
                    MainActivity.this.setTitle(R.string.nav_item_apply);
                    fragmentManager.beginTransaction().
                            replace(R.id.fragment, new Apply()).commit();
                    break;
                case R.id.nav_discard:
                    MainActivity.this.setTitle(R.string.nav_item_discard);
                    fragmentManager.beginTransaction().
                            replace(R.id.fragment, new Discard()).commit();
                    break;
                case R.id.nav_furniture:
                    MainActivity.this.setTitle(R.string.nav_item_furniture);
                    fragmentManager.beginTransaction().
                            replace(R.id.fragment, new FurnitureFragment()).commit();
                    break;
                case R.id.nav_add_furniture:
                    MainActivity.this.setTitle(R.string.nav_item_add_furniture);
                    fragmentManager.beginTransaction().
                            replace(R.id.fragment, new FurnitureActionFragment()).commit();
                    break;
                case R.id.nav_options:
                    MainActivity.this.setTitle(R.string.nav_item_options);
                    fragmentManager.beginTransaction().
                            replace(R.id.fragment, new OptionsFragment()).commit();
                    break;
                case R.id.nav_add_options:
                    MainActivity.this.setTitle(R.string.nav_item_add_options);
                    fragmentManager.beginTransaction().
                            replace(R.id.fragment, new OptionsActionFragment()).commit();
                    break;
                case R.id.nav_manage:
                    MainActivity.this.setTitle(R.string.nav_item_edit_profile);
                    fragmentManager.beginTransaction().
                            replace(R.id.fragment, new EditProfileFragment()).commit();
                    break;
                default:
                    break;
            }
        }
        if (id == R.id.nav_logout) {
            Preferences.setValue(MainActivity.this, "AUTH_TOKEN", null);
            Preferences.setValue(MainActivity.this, "USER", null);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            MainActivity.this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
