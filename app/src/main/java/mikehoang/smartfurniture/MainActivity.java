package mikehoang.smartfurniture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private User userApi;
    private Types typesApi;
    private List<JsonObject> furnitureTypes;
    private List<JsonObject> rigidityTypes;
    private List<JsonObject> massageTypes;
    private JsonObject user;

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
    }

    private void getCurrentUser() {
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
                            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(user.get("image").getAsString()).getContent());
                            image.setImageBitmap(bitmap);
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
        typesApi.getRigidityMassageTypes().enqueue(new Callback<List<ResponseBody>>() {
            @Override
            public void onResponse(Call<List<ResponseBody>> call, Response<List<ResponseBody>> response) {
                try {
                    rigidityTypes = new ArrayList<JsonObject>();
                    massageTypes = new ArrayList<JsonObject>();
                    if (response.body() != null) {
                        for (ResponseBody res : response.body()) {
                            JsonObject obj = new JsonParser().parse(res.string()).getAsJsonObject();
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
            public void onFailure(Call<List<ResponseBody>> call, Throwable t) {
                Log.d("server error", t.toString());
            }
        });
    }

    private void getFurnitureTypes() {
        typesApi.getFurnitureTypes().enqueue(new Callback<List<ResponseBody>>() {
            @Override
            public void onResponse(Call<List<ResponseBody>> call, Response<List<ResponseBody>> response) {
                try {
                    furnitureTypes = new ArrayList<JsonObject>();
                    if (response.body() != null) {
                        for (ResponseBody res : response.body()) {
                            furnitureTypes.add(new JsonParser().parse(res.string()).getAsJsonObject());
                        }
                    }
                } catch (IOException e) {
                    Log.d("error", e.toString());
                }
            }

            @Override
            public void onFailure(Call<List<ResponseBody>> call, Throwable t) {
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_furniture) {
            MainActivity.this.setTitle("Furniture");
        } else if (id == R.id.nav_options) {
            MainActivity.this.setTitle("Furniture options");
        } else if (id == R.id.nav_manage) {
            MainActivity.this.setTitle("Edit profile");
        } else if (id == R.id.nav_logout) {
            Preferences.setAccessToken(MainActivity.this, null);
            MainActivity.this.finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
