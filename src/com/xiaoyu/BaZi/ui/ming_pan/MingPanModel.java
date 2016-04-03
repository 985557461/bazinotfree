package com.xiaoyu.BaZi.ui.ming_pan;

import com.meilishuo.gson.annotations.SerializedName;

/**
 * Created by xiaoyuPC on 2015/4/28.
 */
public class MingPanModel {
    @SerializedName("code")
    public int code;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public Data data;

    class Data{
        @SerializedName("bazi")
        public String bazi;//这个暂时不用

        @SerializedName("birthday_type")
        public String birthday_type;

        @SerializedName("dayun_year")
        public String dayun_year;

        @SerializedName("dayzhu")
        public String dayzhu;

        @SerializedName("eight_pattern")
        public String eight_pattern;

        @SerializedName("eight_six")
        public String eight_six;

        @SerializedName("hourzhu")
        public String hourzhu;

        @SerializedName("kongwang")
        public String kongwang;

        @SerializedName("liunian")
        public String liunian;

        @SerializedName("lucky_year")
        public String lucky_year;

        @SerializedName("monthzhu")
        public String monthzhu;

        @SerializedName("yearzhu")
        public String yearzhu;

        @SerializedName("zodiac")
        public String zodiac;
    }
}
