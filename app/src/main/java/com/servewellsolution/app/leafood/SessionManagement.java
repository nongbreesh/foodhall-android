package com.servewellsolution.app.leafood;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by Breeshy on 8/30/2016 AD.
 */

public class SessionManagement {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    public static final String PREF_NAME = "shopuser";

    // All Shared Preferences Keys
    public static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_SHOPLAT = "shoplat";
    public static final String KEY_SHOPLNG = "shoplng";
    public static final String KEY_SHOPRADIUS = "shopradius";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_FULLNAME = "fullname";
    public static final String KEY_USERID = "userid";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_SHOPID = "shopid";
    public static final String KEY_TEL = "tel";
    public static final String KEY_FBID = "fbid";
    public static final String KEY_LAT= "KEY_LAT";
    public static final String KEY_LNG = "KEY_LNG";
    public static final String KEY_ADDRESS = "KEY_ADDRESS";
    public static final String KEY_NEARBY = "KEY_NEARBY";


    public static final String KEY_ISSHOPOPEN = "isshopopen";
    public static final String KEY_MINAMOUNT = "minamount";
    public static final String KEY_MINPRICE = "minprice";
    public static final String KEY_ORDERTIME = "ordertime";
    public static final String KEY_SHOPNAME = "shopname";
    public static final String KEY_SHOPIMG = "shopimg";
    public static final String KEY_APPROVE = "isapprove";
    // Constructor
    public SessionManagement(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_USERID, pref.getString(KEY_USERID, null));
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_TEL, pref.getString(KEY_TEL, null));
        user.put(KEY_FBID, pref.getString(KEY_FBID, null));
        user.put(KEY_FULLNAME, pref.getString(KEY_FULLNAME, null));
        user.put(KEY_LAT, pref.getString(KEY_LAT, null));
        user.put(KEY_LNG, pref.getString(KEY_LNG, null));
        user.put(KEY_ADDRESS, pref.getString(KEY_ADDRESS, null));
        user.put(KEY_NEARBY, pref.getString(KEY_NEARBY, null));


        return user;
    }

}
