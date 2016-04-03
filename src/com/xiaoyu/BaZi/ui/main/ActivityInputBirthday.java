package com.xiaoyu.BaZi.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.xiaoyu.BaZi.R;
import com.xiaoyu.BaZi.background.Account;
import com.xiaoyu.BaZi.background.BaZi;
import com.xiaoyu.BaZi.ui.ActivityBase;
import com.xiaoyu.BaZi.utils.BaZiToast;
import com.xiaoyu.BaZi.widget.BZDatePickDialog;

/**
 * Created by xiaoyuPC on 2015/4/26.
 */
public class ActivityInputBirthday extends ActivityBase implements View.OnClickListener, BZDatePickDialog.BZDatePickDialogListener {
    private ImageView backImage;
    private EditText xingEditText;
    private EditText mingEditText;
    private RadioGroup sexCheckBox;
    private EditText birthEditText;
    private ImageView choseDateImage;
    private TextView suanMingNotFree;
    private TextView suanMingFree;

    private String otherXing;
    private String otherMing;
    private int otherDateType = 0;
    private String otherBirthDay = "";
    private String otherBirthTime = "";
    private int otherSex = 0;// 0 女 1 男

    private Account account;

    public static final String kWhereFrom = "where_from";
    public static final String VActivityInputBirthday = "activity_input_birthday";

    public static void openForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, ActivityInputBirthday.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_input_birthday);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void getViews() {
        account = BaZi.getInstance().getAccount();
        backImage = (ImageView) findViewById(R.id.backImage);
        xingEditText = (EditText) findViewById(R.id.xingEditText);
        mingEditText = (EditText) findViewById(R.id.mingEditText);
        sexCheckBox = (RadioGroup) findViewById(R.id.sexCheckBox);
        birthEditText = (EditText) findViewById(R.id.birthEditText);
        choseDateImage = (ImageView) findViewById(R.id.choseDateImage);
        suanMingNotFree = (TextView) findViewById(R.id.suanMingNotFree);
        suanMingFree = (TextView) findViewById(R.id.suanMingFree);
    }

    @Override
    protected void initViews() {

    }

    @Override
    protected void setListeners() {
        backImage.setOnClickListener(this);
        sexCheckBox.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int position) {
                if(position == R.id.radioFemale){
                    otherSex = 0;
                }else if(position == R.id.radioMale){
                    otherSex = 1;
                }
            }
        });
        choseDateImage.setOnClickListener(this);
        suanMingNotFree.setOnClickListener(this);
        suanMingFree.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backImage:
                finish();
                break;
            case R.id.choseDateImage:
                BZDatePickDialog.showDlg(this, this);
                break;
            case R.id.suanMingNotFree:
                if (tryToSaveOtherInfo()) {
                    if (!TextUtils.isEmpty(account.isVip) && account.isVip.equals("1")) {//支付过
                        Intent intent = new Intent();
                        intent.putExtra(kWhereFrom, VActivityInputBirthday);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        BaZiToast.makeShortText("请先去支付~");
                        finish();
                    }
                }
                break;
            case R.id.suanMingFree:
                if (tryToSaveOtherInfo()) {
                    Intent intent = new Intent();
                    intent.putExtra(kWhereFrom, VActivityInputBirthday);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }
    }

    private boolean tryToSaveOtherInfo() {
        otherXing = xingEditText.getText().toString();
        if (TextUtils.isEmpty(otherXing)) {
            BaZiToast.makeShortText("请输入姓");
            return false;
        }
        otherMing = mingEditText.getText().toString();
        if (TextUtils.isEmpty(otherMing)) {
            BaZiToast.makeShortText("请输入名");
            return false;
        }
        String birth = birthEditText.getText().toString();
        if (TextUtils.isEmpty(birth)) {
            BaZiToast.makeShortText("请输入生日");
            return false;
        }
        account.otherXing = otherXing;
        account.otherMing = otherMing;
        if (otherSex == 1) {
            account.otherBoy = true;
        } else {
            account.otherBoy = false;
        }
        if (otherDateType == 0) {
            account.otherGongLi = true;
        } else {
            account.otherGongLi = false;
        }
        account.otherBirthday = otherBirthDay;
        account.otherBirthTime = otherBirthTime;
        account.saveOtherInfoToPreference();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (BZDatePickDialog.isShowing(this)) {
            BZDatePickDialog.onBackPressed(this);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onDataPicked(int dateType, int year, int month, int day, int hour) {
        this.otherDateType = dateType;
        StringBuffer sb = new StringBuffer();
        sb.append(year).append("-").append(month).append("-").append(day);
        otherBirthDay = sb.toString();
        otherBirthTime = hour + "";
        sb.append(",").append(hour);
        birthEditText.setText(sb.toString());
    }
}
