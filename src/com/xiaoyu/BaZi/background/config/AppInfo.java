package com.xiaoyu.BaZi.background.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class AppInfo {
	public static String uuid = null;
	public final static String DEVICE_ID = "DeviceID";
	public static String imei = null;
	public static String mac = null;
	public static String ver = "0.5";

	public static String qudao_code = null;
	public final static String via = "android";
	public static final String app = "bz";

	//客户端版本号
	public static String clientid = "1";
	public static String cver = "1.0";


	public synchronized static String id(Context context) {
		String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		return MD5.toMd5(deviceId);
	}

	public static void retrieveAppInfo(Context context) {
		retrieveDeviceID(context);
		retrieveImei(context);
		retrieveMac(context);
		retrieveQudaoCode(context);
		getVersion(context);
	}

	private static void retrieveDeviceID(Context context) {
		// Init device id
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String deviceID = preferences.getString(AppInfo.DEVICE_ID, null);
		if (deviceID == null) {
			// Generate device id and save into pres
			deviceID = AppInfo.id(context);
			if (deviceID != null) {
				Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
				editor.putString(AppInfo.DEVICE_ID, deviceID);
				editor.commit();
			}
		}
		if (deviceID != null)
			uuid = deviceID;
		else
			uuid = "UNKNOWN";
	}

	/**
	 * 获取设备imei号码
	 *
	 * @param context
	 * @return
	 */
	private static void retrieveImei(Context context) {// 上传imei号
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		imei = manager.getDeviceId();
	}

	/**
	 * 获取设备mac地址
	 *
	 * @param context
	 * @return
	 */
	private static void retrieveMac(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		mac = info.getMacAddress();
	}

	/**
	 * 获取渠道号
	 *
	 * @param context
	 * @return
	 */
	private static void retrieveQudaoCode(Context context) {
		try {
			ApplicationInfo appInfo =
					context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			if (appInfo.metaData != null) {
				int value = appInfo.metaData.getInt("TD_CHANNEL_ID", 0);
				qudao_code = value + "";
				return;
			} else {
				qudao_code = "";
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取版本号
	 *
	 * @return 当前应用的版本号
	 */
	public static void getVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			String version = info.versionName;
			cver = version;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
