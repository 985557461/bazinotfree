package com.xiaoyu.BaZi.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiaoyuPC on 2015/4/26.
 */
public class CommonUtil {
    /**
     * �ж������Ƿ�Ϸ�
     * @param email
     * @return
     */
    public static boolean isEmail(String email){
        if (TextUtils.isEmpty(email)) return false;
        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//����ƥ��
        Matcher m = p.matcher(email);
        return m.matches();
    }
}
