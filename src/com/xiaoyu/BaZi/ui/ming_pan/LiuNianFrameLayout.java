package com.xiaoyu.BaZi.ui.ming_pan;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.xiaoyu.BaZi.R;

/**
 * Created by xiaoyuPC on 2015/5/9.
 */
public class LiuNianFrameLayout extends FrameLayout {
    private TextView yearTextView;
    private TextView descTextView;

    public LiuNianFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public LiuNianFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LiuNianFrameLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.liu_nian_view, this, true);
        yearTextView = (TextView) findViewById(R.id.yearTextView);
        descTextView = (TextView) findViewById(R.id.descTextView);
    }

    public void setData(String yearStr, String descStr) {
        yearTextView.setText(yearStr);
        descTextView.setText(descStr);
    }
}
