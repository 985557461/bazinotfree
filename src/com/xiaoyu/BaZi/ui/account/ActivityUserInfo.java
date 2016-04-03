package com.xiaoyu.BaZi.ui.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.xiaoyu.BaZi.R;
import com.xiaoyu.BaZi.background.Account;
import com.xiaoyu.BaZi.background.BaZi;
import com.xiaoyu.BaZi.ui.ActivityBase;
import com.xiaoyu.BaZi.ui.main.ActivityMain;

/**
 * Created by xiaoyuPC on 2015/4/30.
 */
public class ActivityUserInfo extends ActivityBase implements View.OnClickListener {
    private TextView nameText;
    private TextView emailText;
    private TextView xingText;
    private TextView mingText;
    private TextView sexText;
    private TextView birthDayTimeText;
    private TextView logoutBtn;

    private Account account;

    public static void open(Activity activity) {
        Intent intent = new Intent(activity, ActivityUserInfo.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_user_info);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void getViews() {
        account = BaZi.getInstance().getAccount();
        nameText = (TextView) findViewById(R.id.nameText);
        emailText = (TextView) findViewById(R.id.emailText);
        xingText = (TextView) findViewById(R.id.xingText);
        mingText = (TextView) findViewById(R.id.mingText);
        sexText = (TextView) findViewById(R.id.sexText);
        birthDayTimeText = (TextView) findViewById(R.id.birthDayTimeText);
        logoutBtn = (TextView) findViewById(R.id.logoutBtn);
    }

    @Override
    protected void initViews() {
        nameText.setText(account.userName);
        emailText.setText(account.email);
        xingText.setText(account.xing);
        mingText.setText(account.ming);
        sexText.setText(account.boy ? "男" : "女");
        birthDayTimeText.setText(account.birthday + "," + account.birthTime + ":0");
    }

    @Override
    protected void setListeners() {
        logoutBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.logoutBtn) {
            Account account = BaZi.getInstance().getAccount();
            account.clearMeInfo();
            Intent intent = new Intent();
            intent.setAction(ActivityMain.logoutSuccessfulAction);
            BaZi.getInstance().sendBroadcast(intent);
            finish();
        }
    }
}
