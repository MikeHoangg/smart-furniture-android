package mikehoang.smartfurniture;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

class Preferences {
    static void setAccessToken(@NonNull Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ACCESSTOKEN", token);
        editor.apply();
    }

    static String getAccessToken(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref",
                Context.MODE_PRIVATE);
        return sharedPreferences.getString("ACCESSTOKEN", null);
    }
}
