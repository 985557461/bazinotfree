package com.xiaoyu.BaZi.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.meilishuo.gson.annotations.SerializedName;
import com.wanpu.pay.PayConnect;
import com.xiaoyu.BaZi.R;
import com.xiaoyu.BaZi.background.Account;
import com.xiaoyu.BaZi.background.BaZi;
import com.xiaoyu.BaZi.background.config.CommonPreference;
import com.xiaoyu.BaZi.background.config.ServerConfig;
import com.xiaoyu.BaZi.ui.ActivityBase;
import com.xiaoyu.BaZi.ui.account.ActivityLogin;
import com.xiaoyu.BaZi.ui.account.ActivityRegisterUser;
import com.xiaoyu.BaZi.ui.account.ActivityUserInfo;
import com.xiaoyu.BaZi.ui.life_yun_shi.FragmentLifeYunShi;
import com.xiaoyu.BaZi.ui.ming_pan.FragmentMingPan;
import com.xiaoyu.BaZi.ui.ming_ju_analysis.FragmentMingJuAnalysis;
import com.xiaoyu.BaZi.ui.pay.FragmentPay;
import com.xiaoyu.BaZi.utils.BaZiToast;
import com.xiaoyu.BaZi.utils.Request;
import com.xiaoyu.BaZi.utils.share_util.ShareCallbackListener;
import com.xiaoyu.BaZi.utils.share_util.ShareUtil;
import com.xiaoyu.BaZi.widget.HGAlertDlg;
import com.xiaoyu.BaZi.widget.HGShareView;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class ActivityMain extends ActivityBase implements View.OnClickListener, HGShareView.HGShareViewListener, ShareCallbackListener {
    private ImageView memberImage;
    private TextView mingPanBtn;
    private TextView mingPanAnalysisBtn;
    private TextView lifeYunShiBtn;
    private TextView payBtn;
    private ViewPager contentViewPager;
    private TextView inputBirthBtn;
    private TextView meBaZiBtn;
    private TextView shareBtn;

    private Account account;

    /**
     * fragment
     * *
     */
    private List<Fragment> fragments = new ArrayList<Fragment>();
    private MainAdapter mainAdapter;
    private FragmentMingPan fragmentMingPan;
    private FragmentMingJuAnalysis fragmentMingJuAnalysis;
    private FragmentLifeYunShi fragmentLifeYunShi;
    private FragmentPay fragmentPay;

    /**
     * shareView
     * *
     */
    private HGShareView hgShareView;
    private ShareUtil shareUtil;
    private static final String shareUrl = "http://182.92.227.113/bazi/app.html";

    /**
     * requestCode
     * *
     */
    public static final int requestCodeOfInputBirthday = 10001;


    /**
     * 双击返回退出app
     * *
     */
    private static int TIME_LONG = 3 * 1000;
    private long lastTime;

    /**
     * 广播（用于支付或者登陆成功后发广播，隐藏掉遮罩层）
     * *
     */
    private BaZiBroadcastReceiver baZiBroadcastReceiver;

    public static final String loginSuccessfulAction = "login_successful_action";
    public static final String paySuccessfulAction = "pay_successful_action";
    public static final String logoutSuccessfulAction = "logout_successful_action";

    /**
     * 30天试用期之后，弹出对话框
     * *
     */
    private HGAlertDlg dlg;
    private String retrieveImei;

    public class BaZiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(loginSuccessfulAction)) {
                if (fragmentPay != null) {
                    fragmentPay.refreshToLogin();
                    fragmentPay.refreshIsVip();
                }
                if (fragmentLifeYunShi != null) {
                    fragmentLifeYunShi.refreshCanLook();
                }
            } else if (action.equals(paySuccessfulAction)) {
                if (fragmentLifeYunShi != null) {
                    fragmentLifeYunShi.refreshCanLook();
                }
                if (fragmentPay != null) {
                    fragmentPay.refreshIsVip();
                }
            } else if (action.equals(logoutSuccessfulAction)) {
                if (fragmentPay != null) {
                    fragmentPay.refreshToLogin();
                    fragmentPay.refreshIsVip();
                }
                if (fragmentLifeYunShi != null) {
                    fragmentLifeYunShi.refreshCanLook();
                }
            }
        }
    }

    private void registerBroadcastReceiver() {
        baZiBroadcastReceiver = new BaZiBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(loginSuccessfulAction);
        intentFilter.addAction(paySuccessfulAction);
        intentFilter.addAction(logoutSuccessfulAction);
        registerReceiver(baZiBroadcastReceiver, intentFilter);
    }

    private void unRegisterBroadcastReceiver() {
        if (baZiBroadcastReceiver != null) {
            unregisterReceiver(baZiBroadcastReceiver);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        account = BaZi.getInstance().getAccount();
        shareUtil = BaZi.getInstance().getShareUtil();
        registerBroadcastReceiver();
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        PayConnect.getInstance("1d8be5861fbee5c556bdbc20be7d7d52", "me", this);
        synchronousMoney();
    }

    @Override
    protected void getViews() {
        account = BaZi.getInstance().getAccount();
        memberImage = (ImageView) findViewById(R.id.memberImage);
        mingPanBtn = (TextView) findViewById(R.id.mingPanBtn);
        mingPanAnalysisBtn = (TextView) findViewById(R.id.mingPanAnalysisBtn);
        lifeYunShiBtn = (TextView) findViewById(R.id.lifeYunShiBtn);
        payBtn = (TextView) findViewById(R.id.payBtn);
        contentViewPager = (ViewPager) findViewById(R.id.contentViewPager);
        inputBirthBtn = (TextView) findViewById(R.id.inputBirthBtn);
        meBaZiBtn = (TextView) findViewById(R.id.meBaZiBtn);
        shareBtn = (TextView) findViewById(R.id.shareBtn);
    }

    @Override
    protected void initViews() {
        fragmentMingPan = new FragmentMingPan();
        fragmentMingJuAnalysis = new FragmentMingJuAnalysis();
        fragmentLifeYunShi = new FragmentLifeYunShi();
        fragmentPay = new FragmentPay();
        fragments.add(fragmentMingPan);
        fragments.add(fragmentMingJuAnalysis);
        fragments.add(fragmentLifeYunShi);
        fragments.add(fragmentPay);
        contentViewPager.setOffscreenPageLimit(fragments.size());
        mainAdapter = new MainAdapter(getSupportFragmentManager());
        contentViewPager.setAdapter(mainAdapter);
        updateTabColor(0);
    }

    @Override
    protected void setListeners() {
        memberImage.setOnClickListener(this);
        mingPanBtn.setOnClickListener(this);
        mingPanAnalysisBtn.setOnClickListener(this);
        lifeYunShiBtn.setOnClickListener(this);
        payBtn.setOnClickListener(this);
        contentViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        inputBirthBtn.setOnClickListener(this);
        meBaZiBtn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void synchronousMoney() {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        Request.doRequest(this, nameValuePairs, ServerConfig.URL_BAZI_PAYNUM, Request.GET,
                new Request.RequestListener() {
                    @Override
                    public void onException(Request.RequestException e) {
                    }

                    @Override
                    public void onComplete(String response) {
                        PayMoneyModel payMoneyModel = BaZi.getInstance().getGson().fromJsonWithNoException(response, PayMoneyModel.class);
                        if (payMoneyModel != null && payMoneyModel.code == 0) {
                            CommonPreference.setIntValue(CommonPreference.kPayMoney, payMoneyModel.num);
                        }
                    }
                });
    }


    private String retrieveImei(Context context) {// 上传imei号
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getDeviceId();
    }

    private void showAlertDlg() {
        if (!HGAlertDlg.isShowing(this)) {
            dlg = HGAlertDlg.showDlg("提示", "30天试用期已到,请购买", this, new HGAlertDlg.HGAlertDlgClickListener() {
                @Override
                public void onAlertDlgClicked(boolean isConfirm) {
                    if (isConfirm) {
                        updateTabColor(4);
                        contentViewPager.setCurrentItem(4, true);
                    }
                }
            });
            dlg.setPositiveBnText("去购买");
        }
    }

    class PayMoneyModel {
        @SerializedName("code")
        public int code;
        @SerializedName("message")
        public String message;
        @SerializedName("num")
        public int num;
    }

    private void updateTabColor(int index) {
        switch (index) {
            case 0:
                mingPanBtn.setSelected(true);
                mingPanAnalysisBtn.setSelected(false);
                lifeYunShiBtn.setSelected(false);
                payBtn.setSelected(false);
                break;
            case 1:
                mingPanBtn.setSelected(false);
                mingPanAnalysisBtn.setSelected(true);
                lifeYunShiBtn.setSelected(false);
                payBtn.setSelected(false);
                break;
            case 2:
                mingPanBtn.setSelected(false);
                mingPanAnalysisBtn.setSelected(false);
                lifeYunShiBtn.setSelected(true);
                payBtn.setSelected(false);
                break;
            case 3:
                mingPanBtn.setSelected(false);
                mingPanAnalysisBtn.setSelected(false);
                lifeYunShiBtn.setSelected(false);
                payBtn.setSelected(true);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.memberImage:
                Account account = BaZi.getInstance().getAccount();
                if (TextUtils.isEmpty(account.userName)) {
                    ActivityLogin.open(this);
                } else {
                    ActivityUserInfo.open(this);
                }
                break;
            case R.id.mingPanBtn:
                updateTabColor(0);
                contentViewPager.setCurrentItem(0, true);
                break;
            case R.id.mingPanAnalysisBtn:
                updateTabColor(1);
                contentViewPager.setCurrentItem(1, true);
                break;
            case R.id.lifeYunShiBtn:
                updateTabColor(2);
                contentViewPager.setCurrentItem(2, true);
                break;
            case R.id.payBtn:
                updateTabColor(4);
                contentViewPager.setCurrentItem(4, true);
                break;
            case R.id.inputBirthBtn:
                ActivityInputBirthday.openForResult(this, requestCodeOfInputBirthday);
                break;
            case R.id.meBaZiBtn:
                tryToSuanMingForMySelf();
                break;
            case R.id.shareBtn:
                showShareView();
                break;
        }
    }

    private void showShareView() {
        hgShareView = HGShareView.getDlgView(ActivityMain.this);
        if (hgShareView == null) {
            hgShareView = new HGShareView(ActivityMain.this, ActivityMain.this);
        }
        hgShareView.show();
    }

    private void tryToSuanMingForMySelf() {
        if (TextUtils.isEmpty(account.userId)) {
            ActivityRegisterUser.open(this);
            return;
        }
        BaZi.getInstance().isForMe = true;
        fragmentMingPan.tryToCalculateMingPan();
        fragmentMingJuAnalysis.tryToCalculateMingPanAnalysis();
        fragmentLifeYunShi.tryToCalculateLiuNianYunCheng();
    }

    @Override
    public void onBackPressed() {
        if (HGShareView.isShowing(this)) {
            HGShareView.getDlgView(this).dismiss();
            return;
        }

        long t = System.currentTimeMillis();
        if (t - lastTime < TIME_LONG) {
            killActivity();
        } else {
            BaZiToast.makeShortText("再按一次返回键退出");
            lastTime = t;
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestCodeOfInputBirthday && resultCode == RESULT_OK) {
            String whereFrom = data.getStringExtra(ActivityInputBirthday.kWhereFrom);
            if (!TextUtils.isEmpty(whereFrom) && whereFrom.equals(ActivityInputBirthday.VActivityInputBirthday)) {
                BaZi.getInstance().isForMe = false;
                fragmentMingPan.tryToCalculateMingPan();
                fragmentMingJuAnalysis.tryToCalculateMingPanAnalysis();
                fragmentLifeYunShi.tryToCalculateLiuNianYunCheng();
            }
        }
    }

    @Override
    protected void onDestroy() {
        unRegisterBroadcastReceiver();
        PayConnect.getInstance(this).close();
        System.exit(0);
        super.onDestroy();
    }

    @Override
    public void onWeChatClicked() {
        shareUtil.shareToWeChatBitmapID("滴滴算命", "滴滴算命是一款超极实用 准确的八字算命软件，集合了多年的八字实战经验，根据八字多个著作 通过移动APP来展示给人们。", R.drawable.icon_launcher, shareUrl, this);
    }

    @Override
    public void onWeXinClicked() {
        shareUtil.shareToWeChatMomentsBitmapID("滴滴算命", "滴滴算命是一款超极实用 准确的八字算命软件，集合了多年的八字实战经验，根据八字多个著作 通过移动APP来展示给人们。", R.drawable.icon_launcher, shareUrl, this);
    }

    @Override
    public void onQZoneClicked() {
        shareUtil.shareToQQZoneBitmapID("滴滴算命", "滴滴算命是一款超极实用 准确的八字算命软件，集合了多年的八字实战经验，根据八字多个著作 通过移动APP来展示给人们。", R.drawable.icon_launcher, shareUrl, this);
    }

    @Override
    public void onQFriendClicked() {
        shareUtil.shareToQQBitmapID("滴滴算命", "滴滴算命是一款超极实用 准确的八字算命软件，集合了多年的八字实战经验，根据八字多个著作 通过移动APP来展示给人们。", R.drawable.icon_launcher, shareUrl, this);
    }

    @Override
    public void onComplete(String text) {
        BaZiToast.makeShortText(text);
    }

    @Override
    public void onError(String text) {
        BaZiToast.makeShortText(text);
    }

    @Override
    public void onCancel(String text) {
        BaZiToast.makeShortText(text);
    }

    class MainAdapter extends FragmentStatePagerAdapter {
        public MainAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int arg0) {
            switch (arg0) {
                case 0:
                    updateTabColor(0);
                    break;
                case 1:
                    updateTabColor(1);
                    break;
                case 2:
                    updateTabColor(2);
                    break;
                case 3:
                    updateTabColor(3);
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
