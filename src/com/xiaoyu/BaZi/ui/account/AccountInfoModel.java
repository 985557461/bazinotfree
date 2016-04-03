package com.xiaoyu.BaZi.ui.account;

import com.meilishuo.gson.annotations.SerializedName;

/**
 * Created by xiaoyuPC on 2015/4/30.
 */
public class AccountInfoModel {
    @SerializedName("code")
    public int code;

    @SerializedName("message")
    public String message;

    @SerializedName("user")
    public User user;

    public class User{
        @SerializedName("birthday")
        public String birthday;
        @SerializedName("datetype")
        public String datetype;
        @SerializedName("email")
        public String email;
        @SerializedName("id")
        public String id;
        @SerializedName("isvip")
        public String isvip;
        @SerializedName("ming")
        public String ming;
        @SerializedName("mobile")
        public String mobile;
        @SerializedName("password")
        public String password;
        @SerializedName("realname")
        public String realname;
        @SerializedName("sex")
        public String sex;
        @SerializedName("userid")
        public String userid;
        @SerializedName("username")
        public String username;
        @SerializedName("xing")
        public String xing;
    }





















}
