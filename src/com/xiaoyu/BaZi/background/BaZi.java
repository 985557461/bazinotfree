package com.xiaoyu.BaZi.background;

import android.app.Application;
import com.meilishuo.gson.Gson;
import com.xiaoyu.BaZi.background.config.AppInfo;
import com.xiaoyu.BaZi.utils.share_util.ShareUtil;

/**
 * Created by sreay on 15-3-11.
 */
public class BaZi extends Application {
	private static BaZi mApplication;
	private Gson gson;
	private Account account;
	private ShareUtil shareUtil;

	public boolean isForMe = true;//是否是为自己算命

	@Override
	public void onCreate() {
		super.onCreate();
		mApplication = this;

		initApplicationInfo();
	}

	public Gson getGson() {
		if (gson == null) {
			gson = new Gson();
		}
		return gson;
	}

	public Account getAccount(){
		if(account == null){
			account = Account.loadAccount();
		}
		return account;
	}

	public ShareUtil getShareUtil() {
		if (shareUtil == null) {
			shareUtil = new ShareUtil();
			shareUtil.initSDK(this);
		}
		return shareUtil;
	}


	private void initApplicationInfo(){
		AppInfo.retrieveAppInfo(this);
	}

	public static BaZi getInstance() {
		return mApplication;
	}
}
