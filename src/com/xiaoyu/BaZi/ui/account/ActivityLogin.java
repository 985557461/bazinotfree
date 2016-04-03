package com.xiaoyu.BaZi.ui.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.xiaoyu.BaZi.R;
import com.xiaoyu.BaZi.background.Account;
import com.xiaoyu.BaZi.background.BaZi;
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
 * Created by xiaoyuPC on 2015/4/25.
 */
public class ActivityLogin extends ActivityBase implements View.OnClickListener {
    private ImageView backImage;
    private EditText nameEditText;
    private EditText pwdEditText;
    private TextView loginBtn;
    private TextView getPwdBtn;
    private TextView registerBtn;

    public static void open(Activity activity) {
        Intent intent = new Intent(activity, ActivityLogin.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void getViews() {
        backImage = (ImageView) findViewById(R.id.backImage);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        pwdEditText = (EditText) findViewById(R.id.pwdEditText);
        loginBtn = (TextView) findViewById(R.id.loginBtn);
        getPwdBtn = (TextView) findViewById(R.id.getPwdBtn);
        registerBtn = (TextView) findViewById(R.id.registerBtn);
    }

    @Override
    protected void initViews() {

    }

    @Override
    protected void setListeners() {
        backImage.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        getPwdBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backImage:
                finish();
                break;
            case R.id.loginBtn:
                tryToLogin();
                break;
            case R.id.getPwdBtn:
                ActivityFindOutPwd.open(this);
                break;
            case R.id.registerBtn:
                ActivityRegisterUser.open(this);
                break;
        }
    }

    private void tryToLogin() {
        String name = nameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            BaZiToast.makeShortText("请输入用户名");
            return;
        }
        String pwd = pwdEditText.getText().toString();
        if (TextUtils.isEmpty(pwd)) {
            BaZiToast.makeShortText("请输入密码");
            return;
        }
        showDialog("正在登陆...");
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("username", name));
        nameValuePairs.add(new BasicNameValuePair("password", pwd));
        Request.doRequest(this, nameValuePairs, ServerConfig.URL_BAZI_LOGIN, Request.GET,
                new Request.RequestListener() {
                    @Override
                    public void onException(Request.RequestException e) {
                        dismissDialog();
                        BaZiToast.makeShortText("登陆失败");
                    }

                    @Override
                    public void onComplete(String response) {
                        dismissDialog();
                        AccountInfoModel model = BaZi.getInstance().getGson().fromJsonWithNoException(response, AccountInfoModel.class);
                        if (model != null && model.code == 0) {
                            Account account = BaZi.getInstance().getAccount();
                            account.userId = model.user.userid;
                            account.userName = model.user.username;
                            account.email = model.user.email;
                            account.xing = model.user.xing;
                            account.ming = model.user.ming;
                            account.boy = model.user.sex.equals("1") ? true : false;
                            account.gongLi = model.user.datetype.equals("0") ? true : false;
                            account.birthday = model.user.birthday.split(",")[0];
                            account.birthTime = model.user.birthday.split(",")[1];
                            account.isVip = model.user.isvip;
                            account.saveMeInfoToPreference();
                            BaZiToast.makeShortText("登陆成功");
                            Intent intent = new Intent();
                            intent.setAction(ActivityMain.loginSuccessfulAction);
                            BaZi.getInstance().sendBroadcast(intent);
                            ActivityLogin.this.finish();
                        } else {
                            BaZiToast.makeShortText("登陆失败");
                        }
                    }
                });
    }
}
