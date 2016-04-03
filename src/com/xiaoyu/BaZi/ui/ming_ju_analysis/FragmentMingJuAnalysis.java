package com.xiaoyu.BaZi.ui.ming_ju_analysis;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * 命局分析
 */
public class FragmentMingJuAnalysis extends Fragment {
    private TextView caiyun;
    private TextView xingge;
    private TextView jiankang;
    private TextView xueli;

    private ActivityBase activity;
    private MingJuAnalysisModel mingJuAnalysisModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ming_ju_analysis, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tryToCalculateMingPanAnalysis();
    }

    private void initViews(View view) {
        activity = (ActivityBase) getActivity();
        caiyun = (TextView) view.findViewById(R.id.caiYun);
        xingge = (TextView) view.findViewById(R.id.xingGe);
        jiankang = (TextView) view.findViewById(R.id.jianKang);
        xueli = (TextView) view.findViewById(R.id.xueLi);
    }

    public void tryToCalculateMingPanAnalysis() {
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
        nameValuePairs.add(new BasicNameValuePair("type", "1"));
        Request.doRequest(activity, nameValuePairs, ServerConfig.URL_BAZI_BAZIFENXI, Request.GET,
                new Request.RequestListener() {
                    @Override
                    public void onException(Request.RequestException e) {
                        BaZiToast.makeShortText("链接不正确");
                    }

                    @Override
                    public void onComplete(String response) {
                        mingJuAnalysisModel = BaZi.getInstance().getGson().fromJsonWithNoException(response, MingJuAnalysisModel.class);
                        if (mingJuAnalysisModel != null && mingJuAnalysisModel.code == 0) {
                            refreshViews(mingJuAnalysisModel);
                        } else {
                            BaZiToast.makeShortText("链接不正确");
                        }
                    }
                });
    }

    private void refreshViews(MingJuAnalysisModel mingJuAnalysisModel) {
        caiyun.setText(mingJuAnalysisModel.caiyun);
        xingge.setText(mingJuAnalysisModel.xingge);
        jiankang.setText(mingJuAnalysisModel.jiankang);
        xueli.setText(mingJuAnalysisModel.xueli);
    }
}
