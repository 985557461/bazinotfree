package com.xiaoyu.BaZi.utils.share_util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.utils.UIHandler;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import com.xiaoyu.BaZi.background.BaZi;

import java.util.HashMap;

/**
 * Created by sreay on 14-11-4.
 */
public class ShareUtil implements Handler.Callback {
	private ShareCallbackListener listener;

	public void initSDK(Context context) {
		ShareSDK.initSDK(context);
	}

	/**
	 * 微信分享一定要压缩图片的大小，否则可能根本就掉不起来微信的界面
	 */
	//微信好友分享网络图片
	public void shareToWeChatUrl(String title, String content, String url, String link, ShareCallbackListener l) {
		listener = l;
		initSDK(BaZi.getInstance());
		Platform plat = ShareSDK.getPlatform(Wechat.NAME);
		Platform.ShareParams sp = new Platform.ShareParams();
		sp.setShareType(Platform.SHARE_TEXT);
		sp.setShareType(Platform.SHARE_WEBPAGE);
		if (!TextUtils.isEmpty(title)){
			sp.setTitle(title);
		}
		sp.setText(content);
		sp.setUrl(link);
		sp.setImageUrl(url);
		plat.setPlatformActionListener(new CustomActionListener());
		plat.share(sp);
	}

	//微信好友分享本地图片
	public void shareToWeChatPath(String title, String content, String path, String link, ShareCallbackListener l) {
		listener = l;
		initSDK(BaZi.getInstance());
		Platform plat = ShareSDK.getPlatform(Wechat.NAME);
		Platform.ShareParams sp = new Platform.ShareParams();
		sp.setShareType(Platform.SHARE_TEXT);
		sp.setShareType(Platform.SHARE_WEBPAGE);
		if (!TextUtils.isEmpty(title)){
			sp.setTitle(title);
		}
		sp.setText(content);
		sp.setUrl(link);
		sp.setImagePath(path);
		plat.setPlatformActionListener(new CustomActionListener());
		plat.share(sp);
	}

	//微信好友分享Bitmap
	public void shareToWeChatBitmapID(String title, String content, int id, String link, ShareCallbackListener l) {
		listener = l;
		initSDK(BaZi.getInstance());
		Platform plat = ShareSDK.getPlatform(Wechat.NAME);
		Platform.ShareParams sp = new Platform.ShareParams();
		sp.setShareType(Platform.SHARE_TEXT);
		sp.setShareType(Platform.SHARE_WEBPAGE);
		if (!TextUtils.isEmpty(title)){
			sp.setTitle(title);
		}
		sp.setText(content);
		sp.setUrl(link);
		sp.setImageData(getBitmap(id));
		plat.setPlatformActionListener(new CustomActionListener());
		plat.share(sp);
	}

	private Bitmap getBitmap(int id) {
		Bitmap bitmap = BitmapFactory.decodeResource(BaZi.getInstance().getResources(), id);
		return bitmap;
	}

	//微信朋友圈分享网络图片
	public void shareToWeChatMomentsUrl(String title, String content, String url, String link, ShareCallbackListener l) {
		listener = l;
		initSDK(BaZi.getInstance());
		Platform plat = ShareSDK.getPlatform(WechatMoments.NAME);
		Platform.ShareParams sp = new Platform.ShareParams();
		sp.setShareType(Platform.SHARE_TEXT);
		sp.setShareType(Platform.SHARE_WEBPAGE);
		if (!TextUtils.isEmpty(title)){
			sp.setTitle(title);
		}
		sp.setText(content);
		sp.setUrl(link);
		sp.setImageUrl(url);
		plat.setPlatformActionListener(new CustomActionListener());
		plat.share(sp);
	}

	//微信朋友圈分享本地图片
	public void shareToWeChatMomentsPath(String title, String content, String path, String link, ShareCallbackListener l) {
		listener = l;
		initSDK(BaZi.getInstance());
		Platform plat = ShareSDK.getPlatform(WechatMoments.NAME);
		Platform.ShareParams sp = new Platform.ShareParams();
		sp.setShareType(Platform.SHARE_TEXT);
		sp.setShareType(Platform.SHARE_WEBPAGE);
		if (!TextUtils.isEmpty(title)){
			sp.setTitle(title);
		}
		sp.setText(content);
		sp.setUrl(link);
		sp.setImagePath(path);
		plat.setPlatformActionListener(new CustomActionListener());
		plat.share(sp);
	}

	//微信朋友圈分享Bitmap
	public void shareToWeChatMomentsBitmapID(String title, String content, int id, String link, ShareCallbackListener l) {
		listener = l;
		initSDK(BaZi.getInstance());
		Platform plat = ShareSDK.getPlatform(WechatMoments.NAME);
		Platform.ShareParams sp = new Platform.ShareParams();
		sp.setShareType(Platform.SHARE_TEXT);
		sp.setShareType(Platform.SHARE_WEBPAGE);
		if (!TextUtils.isEmpty(title)){
			sp.setTitle(title);
		}
		sp.setText(content);
		sp.setUrl(link);
		sp.setImageData(getBitmap(id));
		plat.setPlatformActionListener(new CustomActionListener());
		plat.share(sp);
	}

	//qq空间
	public void shareToQQZoneUrl(String title, String content, String url, String link, ShareCallbackListener l) {
		listener = l;
		initSDK(BaZi.getInstance());
		if (TextUtils.isEmpty(title)) {
			title = "滴滴算命";
		}
		Platform plat = ShareSDK.getPlatform(QZone.NAME);
		Platform.ShareParams sp = new Platform.ShareParams();
		if (!TextUtils.isEmpty(title)){
			sp.setTitle(title);
		}
		sp.setTitleUrl(link);
		sp.setText(content);
		sp.setImageUrl(url);
		plat.setPlatformActionListener(new CustomActionListener());
		plat.share(sp);
	}

	//qq空间
	public void shareToQQZoneBitmapID(String title, String content, int id, String link, ShareCallbackListener l) {
		listener = l;
		initSDK(BaZi.getInstance());
		if (TextUtils.isEmpty(title)) {
			title = "滴滴算命";
		}
		Platform plat = ShareSDK.getPlatform(QZone.NAME);
		Platform.ShareParams sp = new Platform.ShareParams();
		if (!TextUtils.isEmpty(title)){
			sp.setTitle(title);
		}
		sp.setTitleUrl(link);
		sp.setText(content);
		sp.setImageData(getBitmap(id));
		plat.setPlatformActionListener(new CustomActionListener());
		plat.share(sp);
	}

	//qq好友
	public void shareToQQUrl(String title, String content, String url, String link, ShareCallbackListener l) {
		listener = l;
		initSDK(BaZi.getInstance());
		Platform plat = ShareSDK.getPlatform(QQ.NAME);
		Platform.ShareParams sp = new Platform.ShareParams();
		if (!TextUtils.isEmpty(title)){
			sp.setTitle(title);
		}
		sp.setTitleUrl(link);
		sp.setText(content);
		sp.setImageUrl(url);
		plat.setPlatformActionListener(new CustomActionListener());
		plat.share(sp);
	}

	//qq好友
	public void shareToQQBitmapID(String title, String content, int id, String link, ShareCallbackListener l) {
		listener = l;
		initSDK(BaZi.getInstance());
		Platform plat = ShareSDK.getPlatform(QQ.NAME);
		Platform.ShareParams sp = new Platform.ShareParams();
		if (!TextUtils.isEmpty(title)){
			sp.setTitle(title);
		}
		sp.setTitleUrl(link);
		sp.setText(content);
		sp.setImageData(getBitmap(id));
		plat.setPlatformActionListener(new CustomActionListener());
		plat.share(sp);
	}

	/**
	 * 将action转换为String
	 */
	public String actionToString(int action) {
		switch (action) {
		case Platform.ACTION_AUTHORIZING:
			return "ACTION_AUTHORIZING";
		case Platform.ACTION_GETTING_FRIEND_LIST:
			return "ACTION_GETTING_FRIEND_LIST";
		case Platform.ACTION_FOLLOWING_USER:
			return "ACTION_FOLLOWING_USER";
		case Platform.ACTION_SENDING_DIRECT_MESSAGE:
			return "ACTION_SENDING_DIRECT_MESSAGE";
		case Platform.ACTION_TIMELINE:
			return "ACTION_TIMELINE";
		case Platform.ACTION_USER_INFOR:
			return "ACTION_USER_INFOR";
		case Platform.ACTION_SHARE:
			return "ACTION_SHARE";
		default: {
			return "UNKNOWN";
		}
		}
	}

	@Override
	public boolean handleMessage(Message message) {
		String text = "";
		switch (message.arg1) {
		case 1: {
			text = "分享成功";
			if (listener != null) {
				listener.onComplete(text);
			}
		}
		break;
		case 2: {
			// 失败
			if ("WechatClientNotExistException".equals(message.obj.getClass().getSimpleName())) {
				text = "目前您的微信版本过低或未安装微信，需要安装微信才能使用";
			} else if ("WechatTimelineNotSupportedException".equals(message.obj.getClass().getSimpleName())) {
				text = "目前您的微信版本过低或未安装微信，需要安装微信才能使用";
			} else {
				text = "分享失败";
			}
			if (listener != null) {
				listener.onError(text);
			}
		}
		break;
		case 3: {
			// 取消
			text = "分享取消";
			if (listener != null) {
				listener.onCancel(text);
			}
		}
		break;
		}
		return false;
	}

	public class CustomActionListener implements PlatformActionListener {
		@Override
		public void onComplete(Platform platform, int action, HashMap<String, Object> stringObjectHashMap) {
			Message msg = new Message();
			msg.arg1 = 1;
			msg.arg2 = action;
			msg.obj = platform;
			UIHandler.sendMessage(msg, ShareUtil.this);
		}

		@Override
		public void onError(Platform platform, int action, Throwable throwable) {
			Message msg = new Message();
			msg.arg1 = 2;
			msg.arg2 = action;
			msg.obj = platform;
			UIHandler.sendMessage(msg, ShareUtil.this);
		}

		@Override
		public void onCancel(Platform platform, int action) {
			Message msg = new Message();
			msg.arg1 = 3;
			msg.arg2 = action;
			msg.obj = platform;
			UIHandler.sendMessage(msg, ShareUtil.this);
		}
	}
}
