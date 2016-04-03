package com.xiaoyu.BaZi.ui.pay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wanpu.pay.PayConnect;
import com.wanpu.pay.PayResultListener;
import com.xiaoyu.BaZi.R;
import com.xiaoyu.BaZi.background.Account;
import com.xiaoyu.BaZi.background.BaZi;
import com.xiaoyu.BaZi.background.CommonModel;
import com.xiaoyu.BaZi.background.config.CommonPreference;
import com.xiaoyu.BaZi.background.config.ServerConfig;
import com.xiaoyu.BaZi.ui.ActivityBase;
import com.xiaoyu.BaZi.ui.main.ActivityMain;
import com.xiaoyu.BaZi.utils.BaZiToast;
import com.xiaoyu.BaZi.utils.Request;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoyu on 15-3-11.
 * <p/>
 * 支付
 */
public class FragmentPay extends Fragment implements View.OnClickListener {
    private LinearLayout containerLL;
    private TextView orderText;
    private EditText emailEditText;
    private TextView payMoney;
    private TextView buyBtn;
    private TextView maskPay;
    private ActivityBase activityBase;

    private Account account;

    private String orderId = "";
    private String userId = "";
    private String goodsName = "滴滴算命终身详批服务";
    private float price = 0;
    private String goodsDesc = "购买滴滴算命详细批命";
    private String notifyUrl = "";

    private Context payViewContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        price = CommonPreference.getIntValue(CommonPreference.kPayMoney, 10);
        activityBase = (ActivityBase) getActivity();
        account = BaZi.getInstance().getAccount();
        containerLL = (LinearLayout) view.findViewById(R.id.containerLL);
        orderText = (TextView) view.findViewById(R.id.orderText);
        emailEditText = (EditText) view.findViewById(R.id.emailEditText);
        payMoney = (TextView) view.findViewById(R.id.payMoney);
        buyBtn = (TextView) view.findViewById(R.id.buyBtn);
        maskPay = (TextView) view.findViewById(R.id.maskPay);

        orderId = System.currentTimeMillis() + "";
        orderText.setText(orderId);

        payMoney.setText("人民币" + price + "元");

        userId = PayConnect.getInstance(activityBase).getDeviceId(activityBase);
        refreshIsVip();
        refreshToLogin();
    }

    public void refreshToLogin() {
        Account account = BaZi.getInstance().getAccount();
        if (!TextUtils.isEmpty(account.email)) {
            emailEditText.setText(account.email);
        }
        if (TextUtils.isEmpty(account.userId)) {
            containerLL.setVisibility(View.GONE);
            maskPay.setVisibility(View.VISIBLE);
        } else {
            containerLL.setVisibility(View.VISIBLE);
            maskPay.setVisibility(View.GONE);
        }
    }

    public void refreshIsVip() {
        if (!TextUtils.isEmpty(account.isVip) && account.isVip.equals("1")) {
            buyBtn.setOnClickListener(null);
            buyBtn.setText("已经购买");
        } else {
            buyBtn.setOnClickListener(this);
            buyBtn.setText("购 买");
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buyBtn) {
            try {
                orderId = System.currentTimeMillis() + "";
                orderText.setText(orderId);
                PayConnect.getInstance(activityBase).pay(activityBase,
                        orderId,
                        userId,
                        price,
                        goodsName,
                        goodsDesc,
                        notifyUrl,
                        new MyPayResultListener());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void toTellServerVip() {
        if (TextUtils.isEmpty(account.userId)) {
            BaZiToast.makeShortText("请先去登陆~");
            return;
        }
        activityBase.showDialog("请稍后...");
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("userid", account.userId));
        Request.doRequest(activityBase, nameValuePairs, ServerConfig.URL_BAZI_PAYRECORD, Request.GET,
                new Request.RequestListener() {
                    @Override
                    public void onException(Request.RequestException e) {
                        activityBase.dismissDialog();
                        BaZiToast.makeShortText("支付失败");
                    }

                    @Override
                    public void onComplete(String response) {
                        activityBase.dismissDialog();
                        CommonModel commonModel = BaZi.getInstance().getGson().fromJsonWithNoException(response, CommonModel.class);
                        if (commonModel != null && commonModel.code == 0) {
                            account.isVip = "1";
                            account.saveMeInfoToPreference();
                            BaZiToast.makeShortText("支付成功");
                            Intent intent = new Intent();
                            intent.setAction(ActivityMain.paySuccessfulAction);
                            BaZi.getInstance().sendBroadcast(intent);
                            PayConnect.getInstance(activityBase).closePayView(payViewContext);
                        }
                    }
                });
    }

    private class MyPayResultListener implements PayResultListener {
        @Override
        public void onPayFinish(Context payViewCtx, String orderId, int resultCode, String resultString, int payType,
                                float amount, String goodsName) {
            if (resultCode == 0) {
                payViewContext = payViewCtx;
                PayConnect.getInstance(activityBase).confirm(orderId, payType);
                toTellServerVip();
            } else {
                BaZiToast.makeShortText("支付失败");
            }
        }
    }
}
