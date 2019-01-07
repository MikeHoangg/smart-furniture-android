package mikehoang.smartfurniture;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

class Preferences {
    static void setValue(@NonNull Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    static String getValue(@NonNull Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref",
                Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }
}
