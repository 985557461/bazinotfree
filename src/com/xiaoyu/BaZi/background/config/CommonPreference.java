package com.xiaoyu.BaZi.background.config;

import android.content.Context;
import android.content.SharedPreferences;
import com.xiaoyu.BaZi.background.BaZi;

/**
 * Created by sreay on 14-8-19.
 */
public class CommonPreference {
    private static String PreferenceName = "bazi_common";

    /**
     * 支付的钱数，服务器控制
     * *
     */
    public static final String kPayMoney = "key_pay_money";

    public static void setStringValue(String key, String value) {
        Context context = BaZi.getInstance();
        SharedPreferences preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getStringValue(String key, String defaultValue) {
        Context context = BaZi.getInstance();
        SharedPreferences preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        return preferences.getString(key, defaultValue);
    }

    public static int getIntValue(String key, int defaultValue) {
        Context context = BaZi.getInstance();
        SharedPreferences preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        return preferences.getInt(key, defaultValue);
    }

    public static void setIntValue(String key, int value) {
        Context context = BaZi.getInstance();
        SharedPreferences preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }
}
