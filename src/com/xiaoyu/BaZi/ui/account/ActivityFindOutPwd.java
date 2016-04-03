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
import com.xiaoyu.BaZi.background.CommonModel;
import com.xiaoyu.BaZi.background.config.ServerConfig;
import com.xiaoyu.BaZi.ui.ActivityBase;
import com.xiaoyu.BaZi.utils.BaZiToast;
import com.xiaoyu.BaZi.utils.CommonUtil;
import com.xiaoyu.BaZi.utils.Request;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoyuPC on 2015/4/25.
 */
public class ActivityFindOutPwd extends ActivityBase implements View.OnClickListener {
    private ImageView backImage;
    private EditText emailEditText;
    private TextView findOutBtn;

    private Account account;

    public static void open(Activity activity) {
        Intent intent = new Intent(activity, ActivityFindOutPwd.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_find_out_pwd);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void getViews() {
        account = BaZi.getInstance().getAccount();
        backImage = (ImageView) findViewById(R.id.backImage);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        findOutBtn = (TextView) findViewById(R.id.findOutBtn);
    }

    @Override
    protected void initViews() {
        if(!TextUtils.isEmpty(account.email)){
            emailEditText.setText(account.email);
        }
    }

    @Override
    protected void setListeners() {
        backImage.setOnClickListener(this);
        findOutBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backImage:
                finish();
                break;
            case R.id.findOutBtn:
                tryToFindOutPwd();
                break;
        }
    }

    private void tryToFindOutPwd() {
        String email = emailEditText.getText().toString();
        if(TextUtils.isEmpty(email)){
            BaZiToast.makeShortText("请输入邮箱地址");
            return;
        }
        if(!CommonUtil.isEmail(email)){
            BaZiToast.makeShortText("邮箱地址不合法");
            return;
        }
        if(TextUtils.isEmpty(account.userName)){
            BaZiToast.makeShortText("请先注册");
            return;
        }
        showDialog();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("username", account.userName));
        nameValuePairs.add(new BasicNameValuePair("email", email));
        Request.doRequest(this,nameValuePairs, ServerConfig.URL_BAZI_ZHAOMIMA, Request.POST,
                new Request.RequestListener() {
                    @Override
                    public void onException(Request.RequestException e) {
                        dismissDialog();
                        BaZiToast.makeShortText("找回密码失败");
                    }

                    @Override
                    public void onComplete(String response) {
                        dismissDialog();
                        CommonModel model = BaZi.getInstance().getGson().fromJsonWithNoException(response, CommonModel.class);
                        if (model != null && model.code == 0) {
                            BaZiToast.makeShortText("找回密码成功,请注意查收邮件");
                            ActivityFindOutPwd.this.finish();
                        } else {
                            BaZiToast.makeShortText("找回密码失败");
                        }
                    }
                });
    }
}
