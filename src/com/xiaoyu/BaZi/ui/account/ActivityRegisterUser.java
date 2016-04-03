package com.xiaoyu.BaZi.ui.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.xiaoyu.BaZi.R;
import com.xiaoyu.BaZi.background.Account;
import com.xiaoyu.BaZi.background.BaZi;
import com.xiaoyu.BaZi.background.CommonModel;
import com.xiaoyu.BaZi.background.config.ServerConfig;
import com.xiaoyu.BaZi.ui.ActivityBase;
import com.xiaoyu.BaZi.utils.BaZiToast;
import com.xiaoyu.BaZi.utils.CommonUtil;
import com.xiaoyu.BaZi.utils.Request;
import com.xiaoyu.BaZi.widget.BZDatePickDialog;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoyuPC on 2015/4/25.
 */
public class ActivityRegisterUser extends ActivityBase implements View.OnClickListener, BZDatePickDialog.BZDatePickDialogListener {
    private ImageView backImage;
    private EditText nameEditText;
    private EditText pwdEditText;
    private EditText emailEditText;
    private EditText xingEditText;
    private EditText mingEditText;
    private RadioGroup sexCheckBox;
    private EditText birthEditText;
    private ImageView choseDateImage;
    private TextView sureBtn;
    private TextView cancelBtn;

    private String name;
    private String pwd;
    private String email;
    private String xing;
    private String ming;
    private int dateType = 0;
    private String birthDay = "";
    private String birthTime = "";

    private int sex = 0;//1 男 0 女

    public static void open(Activity activity) {
        Intent intent = new Intent(activity, ActivityRegisterUser.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_register_user);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void getViews() {
        backImage = (ImageView) findViewById(R.id.backImage);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        pwdEditText = (EditText) findViewById(R.id.pwdEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        xingEditText = (EditText) findViewById(R.id.xingEditText);
        mingEditText = (EditText) findViewById(R.id.mingEditText);
        sexCheckBox = (RadioGroup) findViewById(R.id.sexCheckBox);
        birthEditText = (EditText) findViewById(R.id.birthEditText);
        choseDateImage = (ImageView) findViewById(R.id.choseDateImage);
        sureBtn = (TextView) findViewById(R.id.sureBtn);
        cancelBtn = (TextView) findViewById(R.id.cancelBtn);
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
                    sex = 0;
                }else if(position == R.id.radioMale){
                    sex = 1;
                }
            }
        });
        choseDateImage.setOnClickListener(this);
        sureBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backImage:
                finish();
                break;
            case R.id.sureBtn:
                tryToRegister();
                break;
            case R.id.cancelBtn:
                finish();
                break;
            case R.id.choseDateImage:
                BZDatePickDialog.showDlg(this, this);
                break;
        }
    }

    private void tryToRegister() {
        name = nameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            BaZiToast.makeShortText("请输入用户名");
            return;
        }
        pwd = pwdEditText.getText().toString();
        if (TextUtils.isEmpty(pwd)) {
            BaZiToast.makeShortText("请输入密码");
            return;
        }
        email = emailEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            BaZiToast.makeShortText("请输入邮箱地址");
            return;
        }
        if (!CommonUtil.isEmail(email)) {
            BaZiToast.makeShortText("邮箱地址不合法");
            return;
        }
        xing = xingEditText.getText().toString();
        if (TextUtils.isEmpty(xing)) {
            BaZiToast.makeShortText("请输入姓");
            return;
        }
        ming = mingEditText.getText().toString();
        if (TextUtils.isEmpty(ming)) {
            BaZiToast.makeShortText("请输入名");
            return;
        }
        String birth = birthEditText.getText().toString();
        if (TextUtils.isEmpty(birth)) {
            BaZiToast.makeShortText("请输入生日");
            return;
        }
        showDialog("正在注册...");
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("username", name));
        nameValuePairs.add(new BasicNameValuePair("password", pwd));
        nameValuePairs.add(new BasicNameValuePair("email", email));
        nameValuePairs.add(new BasicNameValuePair("xing", xing));
        nameValuePairs.add(new BasicNameValuePair("ming", ming));
        nameValuePairs.add(new BasicNameValuePair("sex", sex + ""));
        nameValuePairs.add(new BasicNameValuePair("datetype", dateType + ""));
        nameValuePairs.add(new BasicNameValuePair("birthday", birth));
        Request.doRequest(this,nameValuePairs, ServerConfig.URL_BAZI_REGUSER, Request.POST,
                new Request.RequestListener() {
                    @Override
                    public void onException(Request.RequestException e) {
                        dismissDialog();
                        BaZiToast.makeShortText("注册失败");
                    }

                    @Override
                    public void onComplete(String response) {
                        dismissDialog();
                        CommonModel model = BaZi.getInstance().getGson().fromJsonWithNoException(response, CommonModel.class);
                        if (model != null && model.code == 0) {
                            saveInfoToAccount();
                            BaZiToast.makeShortText("注册成功");
                            ActivityRegisterUser.this.finish();
                        } else {
                            BaZiToast.makeShortText("注册失败");
                        }
                    }
                });

    }

    private void saveInfoToAccount() {
        Account account = BaZi.getInstance().getAccount();
        account.userName = name;
        account.email = email;
        account.xing = xing;
        account.ming = ming;
        if (sex == 1) {
            account.boy = true;
        } else {
            account.boy = false;
        }
        if (dateType == 0) {
            account.gongLi = true;
        } else {
            account.gongLi = false;
        }
        account.birthday = birthDay;
        account.birthTime = birthTime;
        account.saveMeInfoToPreference();
    }

    @Override
    public void onDataPicked(int dateType, int year, int month, int day, int hour) {
        this.dateType = dateType;
        StringBuffer sb = new StringBuffer();
        sb.append(year).append("-").append(month).append("-").append(day);
        birthDay = sb.toString();
        birthTime = hour + "";
        sb.append(",").append(hour);
        birthEditText.setText(sb.toString());
    }
}
