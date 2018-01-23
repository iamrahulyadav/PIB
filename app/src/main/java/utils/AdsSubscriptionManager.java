package utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bunny on 25/08/17.
 */

public class AdsSubscriptionManager {

    public static int ADSPOSITION_COUNT=7;

    public static void setSubscriptionTime(Context mContext , int subscriptionDays ) {
        SharedPreferences prefs = mContext.getSharedPreferences("adsmanager", 0);


        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter

        editor.putLong("subscriptionTime", System.currentTimeMillis());
        editor.putInt("subscriptionDays", subscriptionDays);



        editor.apply();
    }

    public static void setSubscription(Context mContext , boolean isSubscribed ) {
        SharedPreferences prefs = mContext.getSharedPreferences("adsmanager", 0);


        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter

        editor.putBoolean("issubscribed", isSubscribed);



        editor.apply();
    }

    public static boolean checkShowAds(Context mContext ) {
        SharedPreferences prefs = mContext.getSharedPreferences("adsmanager", 0);

        boolean isSubscribed = prefs.getBoolean("issubscribed", false) ;
        if (isSubscribed){
            return false;
        }

        long subscriptionTime = prefs.getLong("subscriptionTime", 0) ;
        int subscriptionDays = prefs.getInt("subscriptionDays",3);

        long currentTime = System.currentTimeMillis();

        long subscriptionDuration =currentTime -subscriptionTime;

        //3days subscribe
        if ( subscriptionDuration<(86400000l *subscriptionDays) && 0<subscriptionDuration){
            return false ;
        }else{
            return true ;
        }


    }




    public static boolean getSubscription(Context mContext ) {
        SharedPreferences prefs = mContext.getSharedPreferences("adsmanager", 0);

        boolean isSubscribed = prefs.getBoolean("issubscribed", false) ;
        return isSubscribed;

    }






}
