package com.xiaoyu.BaZi.background;

import android.text.TextUtils;
import com.xiaoyu.BaZi.background.config.CommonPreference;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sreay on 15-3-12.
 */
public class Account {
    /**
     * 自己的用户的信息*
     */
    public static String kUserInfo = "key_user_info";
    public static String kUserId = "user_id";
    public static String kUserName = "user_name";
    public static String kEmail = "email";
    public static String kXing = "xing";
    public static String kMing = "ming";
    public static String kBoy = "boy";
    public static String kGongLi = "gongli";
    public static String kBirthday = "birthday";
    public static String kBirthTime = "birth_time";//出生的时间
    public static String kIsVip = "is_vip";

    public String userId = "";
    public String userName = "";
    public String email = "";
    public String xing = "";
    public String ming = "";
    public boolean boy = true;
    public boolean gongLi = true;//0--公历 1--农历
    public String birthday = "";
    public String birthTime = "";
    public String isVip = "0";

    /**
     * 别人的用户的信息*
     */
    public static String kOtherUserInfo = "key_other_user_info";
    public static String kOtherXing = "other_xing";
    public static String kOtherMing = "other_ming";
    public static String kOtherBoy = "other_boy";
    public static String kOtherGongLi = "other_gongli";
    public static String kOtherBirthday = "other_birthday";
    public static String kOtherBirthTime = "other_birth_time";//出生的时间

    public String otherXing = "";
    public String otherMing = "";
    public boolean otherBoy = true;
    public boolean otherGongLi = true;
    public String otherBirthday = "";
    public String otherBirthTime = "";

    public static Account loadAccount() {
        String userInfo = CommonPreference.getStringValue(kUserInfo, "");
        Account account = new Account();
        if (TextUtils.isEmpty(userInfo)) {
            return account;
        }
        try {
            JSONObject user = new JSONObject(userInfo);
            account.userId = user.optString(kUserId);
            account.userName = user.optString(kUserName);
            account.email = user.optString(kEmail);
            account.xing = user.optString(kXing);
            account.ming = user.optString(kMing);
            account.boy = user.optBoolean(kBoy);
            account.gongLi = user.optBoolean(kGongLi);
            account.birthday = user.optString(kBirthday);
            account.birthTime = user.optString(kBirthTime);
            account.isVip = user.optString(kIsVip, "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String OtherUserInfo = CommonPreference.getStringValue(kOtherUserInfo, "");
        if (TextUtils.isEmpty(OtherUserInfo)) {
            return account;
        }
        try {
            JSONObject user = new JSONObject(OtherUserInfo);
            account.otherXing = user.optString(kOtherXing);
            account.otherMing = user.optString(kOtherMing);
            account.otherBoy = user.optBoolean(kOtherBoy);
            account.otherGongLi = user.optBoolean(kOtherGongLi);
            account.otherBirthday = user.optString(kOtherBirthday);
            account.otherBirthTime = user.optString(kOtherBirthTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return account;
    }

    public void saveMeInfoToPreference() {
        JSONObject info = new JSONObject();
        try {
            info.put(kUserId, userId);
            info.put(kUserName, userName);
            info.put(kEmail, email);
            info.put(kXing, xing);
            info.put(kMing, ming);
            info.put(kBoy, boy);
            info.put(kGongLi, gongLi);
            info.put(kBirthday, birthday);
            info.put(kBirthTime, birthTime);
            info.put(kIsVip, isVip);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CommonPreference.setStringValue(kUserInfo, info.toString());
    }

    public void saveOtherInfoToPreference() {
        JSONObject info = new JSONObject();
        try {
            info.put(kOtherXing, otherXing);
            info.put(kOtherMing, otherMing);
            info.put(kOtherBoy, otherBoy);
            info.put(kOtherGongLi, otherGongLi);
            info.put(kOtherBirthday, otherBirthday);
            info.put(kOtherBirthTime, otherBirthTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CommonPreference.setStringValue(kOtherUserInfo, info.toString());
    }

    public void clearMeInfo() {
        userId = "";
        userName = "";
        email = "";
        xing = "";
        ming = "";
        boy = false;
        gongLi = false;//0--公历 1--农历
        birthday = "";
        birthTime = "";
        isVip = "0";
        saveMeInfoToPreference();
    }
}
