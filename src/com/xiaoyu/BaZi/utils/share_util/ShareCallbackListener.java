package com.xiaoyu.BaZi.utils.share_util;

/**
 * Created by sreay on 14-11-4.
 */
public interface ShareCallbackListener {
	public void onComplete(String text);

	public void onError(String text);

	public void onCancel(String text);
}
