package utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bunny on 12/09/17.
 */

public class LanguageManager {
    public static void setLanguage(Context mContext ,boolean isEnglish) {
        SharedPreferences prefs = mContext.getSharedPreferences("language_manager", 0);


        SharedPreferences.Editor editor = prefs.edit();


        editor.putBoolean("isEnglish", isEnglish);


        editor.apply();
    }

    public static boolean getLanguage(Context mContext ) {
        SharedPreferences prefs = mContext.getSharedPreferences("language_manager", 0);



       return prefs.getBoolean("isEnglish", true) ;

    }







}
