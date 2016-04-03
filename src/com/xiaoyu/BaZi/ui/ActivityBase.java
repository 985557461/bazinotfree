package com.xiaoyu.BaZi.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.xiaoyu.BaZi.utils.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sreay on 15-3-11.
 */
public abstract class ActivityBase extends FragmentActivity {

	private static List<Activity> activityList = new ArrayList<Activity>();
	private ProgressDialog waitting_dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getViews();
		initViews();
		setListeners();
		addActivity(this);
	}

	@Override
	protected void onDestroy() {
		Request.cancelRequest(this);
		removeActivity(this);
		super.onDestroy();
	}

	public void showDialog() {
		showDialog("请稍候...");
	}

	public void showDialog(String msg) {
		showDialog(msg, -1);
	}

	public void showDialog(String msg, final long requestId) {
		if (waitting_dialog == null) {
			waitting_dialog = new ProgressDialog(this);
			waitting_dialog.setMessage(msg);
			waitting_dialog.setCancelable(true);
			waitting_dialog.setCanceledOnTouchOutside(false);
		}
		waitting_dialog.setMessage(msg);
		if (!waitting_dialog.isShowing()) {
			waitting_dialog.show();
		}
		waitting_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
			}
		});
	}

	public void dismissDialog() {
		if (!isFinishing() && waitting_dialog != null && waitting_dialog.isShowing()) {
			waitting_dialog.dismiss();
		}
	}

	protected void addActivity(Activity activity) {
		if (activityList == null) {
			activityList = new ArrayList<Activity>();
		}
		activityList.add(activity);
	}

	protected void removeActivity(Activity activity) {
		if (activityList != null) {
			activityList.remove(activity);
		}
	}

	protected void killActivity() {
		if (activityList != null) {
			for (; 0 < activityList.size(); ) {
				Activity activity = activityList.remove(0);
				if (activity != null) {
					activity.finish();
				}
			}
		}
	}

	protected abstract void getViews();

	protected abstract void initViews();

	protected abstract void setListeners();
}
