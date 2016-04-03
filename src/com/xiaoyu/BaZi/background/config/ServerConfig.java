package com.xiaoyu.BaZi.background.config;

/**
 * Created by sreay on 14-8-18.
 */
public class ServerConfig {
	// 测试环境
	public static String BASE_URL_TEST = "http://182.92.227.113/";
	// 正式环境
	public static String BASE_URL_OFFICAL = "http://182.92.227.113/";

	public static String BASE_URL = BASE_URL_OFFICAL;

	public static void initUrl(boolean boo) {
		if (boo) {
			ServerConfig.BASE_URL = ServerConfig.BASE_URL_TEST;
		} else {
			ServerConfig.BASE_URL = ServerConfig.BASE_URL_OFFICAL;
		}
	}

	public static final String URL_BAZI_BAZIFENXI = "bazi/bazifenxi";
	public static final String URL_BAZI_LOGIN ="bazi/login";
	public static final String URL_BAZI_REGUSER ="bazi/reguser";
	public static final String URL_BAZI_ZHAOMIMA = "bazi/zhaomima";
	public static final String URL_BAZI_PAYRECORD = "bazi/payrecord";
	public static final String URL_BAZI_PAYNUM = "bazi/paynum";
	public static final String URL_BAZI_NEWDATE = "bazi/newDate";
}
