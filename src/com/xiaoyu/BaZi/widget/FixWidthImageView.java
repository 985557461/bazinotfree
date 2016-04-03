package com.xiaoyu.BaZi.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by sreay on 14-9-3.
 */
public class FixWidthImageView extends ImageView {

	public FixWidthImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FixWidthImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FixWidthImageView(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthModel = View.MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
		int heightModel = View.MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);

		int width = 0;
		int height = 0;

		if (widthModel == View.MeasureSpec.EXACTLY || heightModel == View.MeasureSpec.EXACTLY) {
			if (widthModel == View.MeasureSpec.EXACTLY && heightModel == View.MeasureSpec.EXACTLY) {
				//取较大的长度
				if (widthSize > heightSize) {
					width = height = widthSize;
				} else {
					width = height = heightSize;
				}
			} else if (widthModel == View.MeasureSpec.EXACTLY) {
				width = height = widthSize;
			} else {
				width = height = heightSize;
			}
		} else {
			//指定默认的大小
			width = height = widthSize;
		}
		setMeasuredDimension(width,height);
	}
}
