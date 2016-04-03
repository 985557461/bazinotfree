package com.xiaoyu.BaZi.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xiaoyu.BaZi.R;

@SuppressLint("WrongViewCast")
public class HGShareView extends FrameLayout implements View.OnClickListener {
	private TextView cancel;
	private LinearLayout weChat;
	private LinearLayout wexin;
	private LinearLayout qzone;
	private LinearLayout qFriend;
	private LinearLayout container;
	private View centerSpan;
	private LinearLayout secondLine;
	private HGShareViewListener l;
	private HGEditSheetAnimationListener animationListener;
	private FrameLayout rootView;

	private TranslateAnimation moveInAni;
	private TranslateAnimation moveOutAni;
	private static final int kAniDuration = 300;

	public interface HGShareViewListener {

		void onWeChatClicked();

		void onWeXinClicked();

		void onQZoneClicked();

		void onQFriendClicked();
	}

	public interface HGEditSheetAnimationListener {
		void onAnimationStart();

		void onAnimationEnd();
	}

	public HGShareView(Context context) {
		super(context);
	}

	@SuppressWarnings("deprecation")
	public HGShareView(Activity activity, HGShareViewListener l) {
		super(activity);
		LayoutInflater li = LayoutInflater.from(activity);
		li.inflate(R.layout.view_share, this, true);
		cancel = (TextView) findViewById(R.id.cancel);
		weChat = (LinearLayout) findViewById(R.id.weChat);
		wexin = (LinearLayout) findViewById(R.id.wexin);
		qzone = (LinearLayout) findViewById(R.id.qzone);
		qFriend = (LinearLayout) findViewById(R.id.qFriend);
		container = (LinearLayout) findViewById(R.id.container);
		centerSpan = findViewById(R.id.centerSpan);
		secondLine = (LinearLayout) findViewById(R.id.secondLine);
		cancel.setOnClickListener(this);
		weChat.setOnClickListener(this);
		wexin.setOnClickListener(this);
		qzone.setOnClickListener(this);
		qFriend.setOnClickListener(this);
		setVisibility(View.GONE);
		rootView = getRootView(activity);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		setLayoutParams(params);
		setId(R.id.view_share);
		rootView.addView(this, params);
		this.l = l;
		moveInAni = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
		moveInAni.setDuration(kAniDuration);

		moveOutAni = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
		moveOutAni.setDuration(kAniDuration);

		moveOutAni.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (null != animationListener) {
					animationListener.onAnimationStart();
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				setVisibility(View.GONE);
				if (null != animationListener) {
					animationListener.onAnimationEnd();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
	}

	public void onlyShowWeChat() {
		centerSpan.setVisibility(View.GONE);
		secondLine.setVisibility(View.GONE);
		qzone.setVisibility(View.INVISIBLE);
	}

	public static boolean onBackPressed(Activity activity) {
		HGShareView dlg = getDlgView(activity);
		if (null != dlg && dlg.isShowing()) {
			dlg.dismiss();
			return true;
		}
		return false;
	}

	public static boolean hasDlg(Activity activity) {
		HGShareView dlg = getDlgView(activity);
		return dlg != null;
	}

	public static boolean isShowing(Activity activity) {
		HGShareView dlg = getDlgView(activity);
		if (null != dlg && dlg.isShowing()) {
			return true;
		}
		return false;
	}

	public static HGShareView getDlgView(Activity activity) {
		return (HGShareView) getRootView(activity).findViewById(R.id.view_share);
	}

	private static FrameLayout getRootView(Activity activity) {
		return (FrameLayout) activity.findViewById(R.id.rootView);
	}

	public boolean isShowing() {
		return getVisibility() == View.VISIBLE;
	}

	public void show() {
		setVisibility(View.VISIBLE);
		container.startAnimation(moveInAni);
	}

	public void dismiss() {
		if (getParent() == null) {
			return;
		}
		container.startAnimation(moveOutAni);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getRawX();
		float y = event.getRawY();
		Rect rect = new Rect();
		container.getGlobalVisibleRect(rect);
		if (!rect.contains((int) x, (int) y)) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				dismiss();
			}
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		dismiss();
		switch (v.getId()) {
		case R.id.weChat:
			if (l != null) {
				l.onWeChatClicked();
			}
			break;
		case R.id.wexin:
			if (l != null) {
				l.onWeXinClicked();
			}
			break;
		case R.id.qzone:
			if (l != null) {
				l.onQZoneClicked();
			}
			break;
		case R.id.qFriend:
			if (l != null) {
				l.onQFriendClicked();
			}
			break;
		}
	}
}
