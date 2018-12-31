package mikehoang.smartfurniture;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

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
    private User userApi;
    private Types typesApi;
    public List<JsonObject> furnitureTypes;
    public List<JsonObject> rigidityTypes;
    public List<JsonObject> massageTypes;
    public JsonObject user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.8:8000/en/api/v1/").build();
        userApi = retrofit.create(User.class);
        typesApi = retrofit.create(Types.class);
        getCurrentUser();
        getFurnitureTypes();
        getRigidityMassageTypes();

        if (savedInstanceState == null && user != null) {
            setFirstPage(navigationView);
        }
    }

    private void setFirstPage(NavigationView navigationView) {
        MainActivity.this.setTitle("Furniture");
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FurnitureFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_furniture);
    }

    public void getCurrentUser() {
        userApi.getCurrentUser(Preferences.getAccessToken(MainActivity.this)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        user = new JsonParser().parse(response.body().string()).getAsJsonObject();

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
                } catch (IOException e) {
                    Log.d("error", e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("server error", t.toString());
            }
        });
    }

    private void getRigidityMassageTypes() {
        typesApi.getRigidityMassageTypes().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    rigidityTypes = new ArrayList<JsonObject>();
                    massageTypes = new ArrayList<JsonObject>();
                    if (response.body() != null) {
                        JsonArray list = new JsonParser().parse(response.body().string()).getAsJsonArray();
                        for (JsonElement el : list) {
                            JsonObject obj = el.getAsJsonObject();
                            if (obj.get("type").getAsString().equals("massage"))
                                massageTypes.add(obj);
                            else
                                rigidityTypes.add(obj);
                        }

                    }
                } catch (IOException e) {
                    Log.d("error", e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("server error", t.toString());
            }
        });
    }

    private void getFurnitureTypes() {
        typesApi.getFurnitureTypes().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    furnitureTypes = new ArrayList<JsonObject>();
                    if (response.body() != null) {
                        JsonArray list = new JsonParser().parse(response.body().string()).getAsJsonArray();
                        for (JsonElement el : list) {
                            furnitureTypes.add(el.getAsJsonObject());
                        }
                    }
                } catch (IOException e) {
                    Log.d("error", e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("server error", t.toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_apply && user != null) {
            MainActivity.this.setTitle("Apply furniture settings");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Apply()).commit();
        } else if (id == R.id.nav_discard && user != null) {
            MainActivity.this.setTitle("Discard furniture settings");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Discard()).commit();
        } else if (id == R.id.nav_furniture && user != null) {
            MainActivity.this.setTitle("Furniture");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FurnitureFragment()).commit();
        } else if (id == R.id.nav_add_furniture && user != null) {
            MainActivity.this.setTitle("Add furniture");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FurnitureActionFragment()).commit();
        } else if (id == R.id.nav_options && user != null) {
            MainActivity.this.setTitle("Furniture options");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OptionsFragment()).commit();
        } else if (id == R.id.nav_add_options && user != null) {
            MainActivity.this.setTitle("Add furniture options");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OptionsActionFragment()).commit();
        } else if (id == R.id.nav_manage && user != null) {
            MainActivity.this.setTitle("Edit profile");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new EditProfileFragment()).commit();
        } else if (id == R.id.nav_logout && user != null) {
            Preferences.setAccessToken(MainActivity.this, null);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            MainActivity.this.finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
