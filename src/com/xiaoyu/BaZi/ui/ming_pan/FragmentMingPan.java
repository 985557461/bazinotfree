package com.xiaoyu.BaZi.ui.ming_pan;

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
import java.util.Calendar;
import java.util.List;

/**
 * Created by xiaoyu on 15-3-11.
 * <p/>
 * 命盘
 */
public class FragmentMingPan extends Fragment implements View.OnClickListener {
    private TextView xingMing;
    private TextView sex;
    private TextView birthDayTime;
    private TextView zhuXingOne;
    private TextView zhuXingTwo;
    private TextView zhuXingThree;
    private TextView zhuXingFour;
    private TextView tianGanOne;
    private TextView tianGanTwo;
    private TextView tianGanThree;
    private TextView tianGanFour;
    private TextView diZhiOne;
    private TextView diZhiTwo;
    private TextView diZhiThree;
    private TextView diZhiFour;
    private TextView kongWang;
    private TextView daYunZhouSui;
    private TextView baZiGeJu;
    private TextView daYunOne;
    private TextView daYunTwo;
    private TextView daYunThree;
    private TextView daYunFour;
    private TextView daYunFive;
    private TextView daYunSix;
    private TextView daYunSeven;
    private TextView daYunEight;
    private LinearLayout liuNianLL;

    private ActivityBase activity;
    private Account account;
    private MingPanModel mingPanModel;
    private String[] liuNians = null;

    //今年的年份
    private String curYear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ming_pan, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tryToCalculateMingPan();
    }

    private void initViews(View view) {
        Calendar calendar = Calendar.getInstance();
        curYear = calendar.get(Calendar.YEAR) + "";
        account = BaZi.getInstance().getAccount();
        activity = (ActivityBase) getActivity();
        xingMing = (TextView) view.findViewById(R.id.xingMing);
        sex = (TextView) view.findViewById(R.id.sex);
        birthDayTime = (TextView) view.findViewById(R.id.birthDayTime);
        zhuXingOne = (TextView) view.findViewById(R.id.zhuXingOne);
        zhuXingTwo = (TextView) view.findViewById(R.id.zhuXingTwo);
        zhuXingThree = (TextView) view.findViewById(R.id.zhuXingThree);
        zhuXingFour = (TextView) view.findViewById(R.id.zhuXingFour);
        tianGanOne = (TextView) view.findViewById(R.id.tianGanOne);
        tianGanTwo = (TextView) view.findViewById(R.id.tianGanTwo);
        tianGanThree = (TextView) view.findViewById(R.id.tianGanThree);
        tianGanFour = (TextView) view.findViewById(R.id.tianGanFour);
        diZhiOne = (TextView) view.findViewById(R.id.diZhiOne);
        diZhiTwo = (TextView) view.findViewById(R.id.diZhiTwo);
        diZhiThree = (TextView) view.findViewById(R.id.diZhiThree);
        diZhiFour = (TextView) view.findViewById(R.id.diZhiFour);
        kongWang = (TextView) view.findViewById(R.id.kongWang);
        daYunZhouSui = (TextView) view.findViewById(R.id.daYunZhouSui);
        baZiGeJu = (TextView) view.findViewById(R.id.baZiGeJu);
        daYunOne = (TextView) view.findViewById(R.id.daYunOne);
        daYunTwo = (TextView) view.findViewById(R.id.daYunTwo);
        daYunThree = (TextView) view.findViewById(R.id.daYunThree);
        daYunFour = (TextView) view.findViewById(R.id.daYunFour);
        daYunFive = (TextView) view.findViewById(R.id.daYunFive);
        daYunSix = (TextView) view.findViewById(R.id.daYunSix);
        daYunSeven = (TextView) view.findViewById(R.id.daYunSeven);
        daYunEight = (TextView) view.findViewById(R.id.daYunEight);
        liuNianLL = (LinearLayout) view.findViewById(R.id.liuNianLL);

        daYunOne.setOnClickListener(this);
        daYunTwo.setOnClickListener(this);
        daYunThree.setOnClickListener(this);
        daYunFour.setOnClickListener(this);
        daYunFive.setOnClickListener(this);
        daYunSix.setOnClickListener(this);
        daYunSeven.setOnClickListener(this);
        daYunSeven.setOnClickListener(this);
        daYunEight.setOnClickListener(this);
    }

    public void tryToCalculateMingPan() {
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
        nameValuePairs.add(new BasicNameValuePair("type", "0"));
        Request.doRequest(activity, nameValuePairs, ServerConfig.URL_BAZI_BAZIFENXI, Request.GET,
                new Request.RequestListener() {
                    @Override
                    public void onException(Request.RequestException e) {
                        BaZiToast.makeShortText("链接不正确");
                    }

                    @Override
                    public void onComplete(String response) {
                        mingPanModel = BaZi.getInstance().getGson().fromJsonWithNoException(response, MingPanModel.class);
                        if (mingPanModel != null && mingPanModel.code == 0 && mingPanModel.data != null) {
                            refreshViews(mingPanModel);
                        } else {
                            BaZiToast.makeShortText("链接不正确");
                        }
                    }
                });
    }

    private void refreshViews(MingPanModel mingPanModel) {
        if (BaZi.getInstance().isForMe) {
            xingMing.setText(account.xing + account.ming);
            if (account.boy) {
                sex.setText("男");
            } else {
                sex.setText("女");
            }
            birthDayTime.setText(account.birthday + " " + account.birthTime + ":0");
        } else {
            xingMing.setText(account.otherXing + account.otherMing);
            if (account.otherBoy) {
                sex.setText("男");
            } else {
                sex.setText("女");
            }
            birthDayTime.setText(account.otherBirthday + " " + account.otherBirthTime + ":0");
        }
        //主星
        String[] zhuXings = mingPanModel.data.eight_six.trim().split("-");
        zhuXingOne.setText(zhuXings[0]);
        zhuXingTwo.setText(zhuXings[1]);
        zhuXingThree.setText(zhuXings[2]);
        zhuXingFour.setText(zhuXings[3]);
        String nianZhu = mingPanModel.data.yearzhu.trim();
        String yueZhu = mingPanModel.data.monthzhu.trim();
        String riZhu = mingPanModel.data.dayzhu.trim();
        String shiZhu = mingPanModel.data.hourzhu.trim();
        //天干
        tianGanOne.setText(nianZhu.substring(0, 1));
        tianGanTwo.setText(yueZhu.substring(0, 1));
        tianGanThree.setText(riZhu.substring(0, 1));
        tianGanFour.setText(shiZhu.substring(0, 1));
        //地支
        diZhiOne.setText(nianZhu.substring(1));
        diZhiTwo.setText(yueZhu.substring(1));
        diZhiThree.setText(riZhu.substring(1));
        diZhiFour.setText(shiZhu.substring(1));
        //空亡
        kongWang.setText(mingPanModel.data.kongwang);
        //大运周岁
        daYunZhouSui.setText(mingPanModel.data.dayun_year);
        //八字格局
        baZiGeJu.setText(mingPanModel.data.eight_pattern);
        //大运
        String[] daYuns = mingPanModel.data.lucky_year.split("-");
        daYunOne.setText(daYuns[0]);
        daYunTwo.setText(daYuns[1]);
        daYunThree.setText(daYuns[2]);
        daYunFour.setText(daYuns[3]);
        daYunFive.setText(daYuns[4]);
        daYunSix.setText(daYuns[5]);
        daYunSeven.setText(daYuns[6]);
        daYunEight.setText(daYuns[7]);
        //流年
        liuNians = mingPanModel.data.liunian.split("-");
        int index = checkLiuNianIncludeCurrYear();
        refreshLiuNian(index);
        updateDaYunColor(index);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.daYunOne:
                updateDaYunColor(0);
                refreshLiuNian(0);
                break;
            case R.id.daYunTwo:
                updateDaYunColor(1);
                refreshLiuNian(1);
                break;
            case R.id.daYunThree:
                updateDaYunColor(2);
                refreshLiuNian(2);
                break;
            case R.id.daYunFour:
                updateDaYunColor(3);
                refreshLiuNian(3);
                break;
            case R.id.daYunFive:
                updateDaYunColor(4);
                refreshLiuNian(4);
                break;
            case R.id.daYunSix:
                updateDaYunColor(5);
                refreshLiuNian(5);
                break;
            case R.id.daYunSeven:
                updateDaYunColor(6);
                refreshLiuNian(6);
                break;
            case R.id.daYunEight:
                updateDaYunColor(7);
                refreshLiuNian(7);
                break;
        }
    }

    private void updateDaYunColor(int index) {
        switch (index) {
            case 0:
                daYunOne.setBackgroundColor(getResources().getColor(R.color.common_red));
                daYunTwo.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunThree.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFour.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFive.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSix.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSeven.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunEight.setBackgroundColor(getResources().getColor(R.color.transprant));
                break;
            case 1:
                daYunOne.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunTwo.setBackgroundColor(getResources().getColor(R.color.common_red));
                daYunThree.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFour.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFive.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSix.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSeven.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunEight.setBackgroundColor(getResources().getColor(R.color.transprant));
                break;
            case 2:
                daYunOne.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunTwo.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunThree.setBackgroundColor(getResources().getColor(R.color.common_red));
                daYunFour.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFive.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSix.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSeven.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunEight.setBackgroundColor(getResources().getColor(R.color.transprant));
                break;
            case 3:
                daYunOne.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunTwo.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunThree.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFour.setBackgroundColor(getResources().getColor(R.color.common_red));
                daYunFive.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSix.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSeven.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunEight.setBackgroundColor(getResources().getColor(R.color.transprant));
                break;
            case 4:
                daYunOne.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunTwo.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunThree.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFour.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFive.setBackgroundColor(getResources().getColor(R.color.common_red));
                daYunSix.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSeven.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunEight.setBackgroundColor(getResources().getColor(R.color.transprant));
                break;
            case 5:
                daYunOne.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunTwo.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunThree.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFour.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFive.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSix.setBackgroundColor(getResources().getColor(R.color.common_red));
                daYunSeven.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunEight.setBackgroundColor(getResources().getColor(R.color.transprant));
                break;
            case 6:
                daYunOne.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunTwo.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunThree.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFour.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFive.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSix.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSeven.setBackgroundColor(getResources().getColor(R.color.common_red));
                daYunEight.setBackgroundColor(getResources().getColor(R.color.transprant));
                break;
            case 7:
                daYunOne.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunTwo.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunThree.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFour.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunFive.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSix.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunSeven.setBackgroundColor(getResources().getColor(R.color.transprant));
                daYunEight.setBackgroundColor(getResources().getColor(R.color.common_red));
                break;
        }
    }

    private void refreshLiuNian(int index) {
        for (int i = 0; i < liuNianLL.getChildCount(); i++) {
            LiuNianFrameLayout liuNianFrameLayout = (LiuNianFrameLayout) liuNianLL.getChildAt(i);
            String str = liuNians[index * 10 + i];
            String yearStr = str.split(",")[0];
            String descStr = str.split(",")[1];
            if (str.startsWith(curYear)) {
                liuNianFrameLayout.setBackgroundColor(getResources().getColor(R.color.common_red));
            } else {
                liuNianFrameLayout.setBackgroundColor(getResources().getColor(R.color.transprant));
            }
            liuNianFrameLayout.setData(yearStr, descStr);
        }
    }

    private int checkLiuNianIncludeCurrYear() {
        for (int i = 0; i < liuNians.length; i++) {
            if (liuNians[i].startsWith(curYear)) {
                return (i + 1) / 10 + (i + 1) % 10 == 0 ? 0 : 1 - 1;
            }
        }
        return 0;
    }
}
