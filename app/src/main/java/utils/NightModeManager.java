package utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bunny on 27/09/17.
 */

public class NightModeManager {


    public static void setNightMode(Context mContext , boolean nightMode) {
        SharedPreferences prefs = mContext.getSharedPreferences("nightmodemanager", 0);


        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter

        editor.putBoolean("nightmode", nightMode);



        editor.apply();
    }

    public static boolean getNightMode(Context mContext ) {
        SharedPreferences prefs = mContext.getSharedPreferences("nightmodemanager", 0);

        return prefs.getBoolean("nightmode", false);

    }



}
