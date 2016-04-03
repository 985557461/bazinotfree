package com.xiaoyu.BaZi.ui.life_yun_shi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xiaoyu.BaZi.R;
import com.xiaoyu.BaZi.background.Account;
import com.xiaoyu.BaZi.background.BaZi;
import com.xiaoyu.BaZi.background.config.ServerConfig;
import com.xiaoyu.BaZi.ui.ActivityBase;
import com.xiaoyu.BaZi.utils.BaZiToast;
import com.xiaoyu.BaZi.utils.Request;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoyu on 15-3-11.
 * <p/>
 * 一生运势
 */
public class FragmentLifeYunShi extends Fragment {
    private LinearLayout containerLL;
    private TextView liuNianYunCheng;
    private TextView maskPay;

    private Account account;
    private ActivityBase activity;
    private LifeYunShiModel lifeYunShiModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_life_yun_shi, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tryToCalculateLiuNianYunCheng();
    }


    private void initViews(View view) {
        account = BaZi.getInstance().getAccount();
        activity = (ActivityBase) getActivity();
        containerLL = (LinearLayout) view.findViewById(R.id.containerLL);
        liuNianYunCheng = (TextView) view.findViewById(R.id.liuNianYunCheng);
        maskPay = (TextView) view.findViewById(R.id.maskPay);
        refreshCanLook();
    }

    public void refreshCanLook(){
        if (!TextUtils.isEmpty(account.isVip) && account.isVip.equals("1")) {
            containerLL.setVisibility(View.VISIBLE);
            maskPay.setVisibility(View.GONE);
        } else {
            containerLL.setVisibility(View.GONE);
            maskPay.setVisibility(View.VISIBLE);
        }
    }

    public void tryToCalculateLiuNianYunCheng() {
        Account account = BaZi.getInstance().getAccount();
        String xingMing = null;
        String sex = null;
        String dateType = null;
        String birthDayTime = null;
        if (BaZi.getInstance().isForMe) {
            xingMing = account.xing + account.ming;
            sex = account.boy ? "1" : "0";
            dateType = account.gongLi ? "0" : "1";
            birthDayTime = account.birthday + "," + account.birthTime;
        } else {
            xingMing = account.otherXing + account.otherMing;
            sex = account.otherBoy ? "1" : "0";
            dateType = account.otherGongLi ? "0" : "1";
            birthDayTime = account.otherBirthday + "," + account.otherBirthTime;
        }
        if (TextUtils.isEmpty(xingMing)) {
            BaZiToast.makeShortText("请输入生日");
            return;
        }

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("username", xingMing));
        nameValuePairs.add(new BasicNameValuePair("sex", sex));
        nameValuePairs.add(new BasicNameValuePair("datetype", dateType));
        nameValuePairs.add(new BasicNameValuePair("birthday", birthDayTime));
        nameValuePairs.add(new BasicNameValuePair("type", "2"));
        Request.doRequest(activity, nameValuePairs, ServerConfig.URL_BAZI_BAZIFENXI, Request.GET,
                new Request.RequestListener() {
                    @Override
                    public void onException(Request.RequestException e) {
                        BaZiToast.makeShortText("链接不正确");
                    }

                    @Override
                    public void onComplete(String response) {
                        lifeYunShiModel = BaZi.getInstance().getGson().fromJsonWithNoException(response, LifeYunShiModel.class);
                        if (lifeYunShiModel != null && lifeYunShiModel.code == 0) {
                            refreshViews(lifeYunShiModel);
                        } else {
                            BaZiToast.makeShortText("链接不正确");
                        }
                    }
                });
    }

    private void refreshViews(LifeYunShiModel lifeYunShiModel) {
        refreshCanLook();
        liuNianYunCheng.setText(lifeYunShiModel.message);
    }
}
