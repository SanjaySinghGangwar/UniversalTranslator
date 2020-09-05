package com.sanjaysgangwar.universaltranslator.sevices;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class utils {
    public static String APIkey = "AIzaSyA7hQ5A_MnRf2TM2yf0nIO61wdqNKPWgyQ";

    public static String currentTimeStamp() {
        Long tsLong = System.currentTimeMillis();
        return tsLong.toString();
    }

    public static boolean networkIsOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }
}
