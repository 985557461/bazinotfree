package com.xiaoyu.BaZi.widget.wheel_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SoundEffectConstants;
import android.view.View;
import com.xiaoyu.BaZi.R;

public class WheelView extends TosGallery {
    /**
     * The selector.
     */
    private Drawable mSelectorDrawable = null;

    /**
     * The bound rectangle of selector.
     */
    private Rect mSelectorBound = new Rect();

    /**
     * The top shadow.
     */
    private GradientDrawable mTopShadow = null;

    /**
     * The bottom shadow.
     */
    private GradientDrawable mBottomShadow = null;

    /**
     * Shadow colors
     */
    private static final int[] SHADOWS_COLORS = { 0xFF111111, 0x00AAAAAA, 0x00AAAAAA };

    /**
     * The constructor method.
     * 
     * @param context
     */
    public WheelView(Context context) {
        super(context);

        initialize(context);
    }

    /**
     * The constructor method.
     * 
     * @param context
     * @param attrs
     */
    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize(context);
    }

    /**
     * The constructor method.
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initialize(context);
    }

    /**
     * Initialize.
     * 
     * @param context
     */
    private void initialize(Context context) {
        this.setVerticalScrollBarEnabled(false);
        this.setSlotInCenter(true);
        this.setOrientation(VERTICAL);
        this.setGravity(Gravity.CENTER_HORIZONTAL);
        this.setUnselectedAlpha(1.0f);

        // This lead the onDraw() will be called.
        this.setWillNotDraw(false);

        // The selector rectangle drawable.
        this.mSelectorDrawable = getContext().getResources().getDrawable(R.drawable.wheel_val);
        this.mTopShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        this.mBottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);

        // The default background.
        this.setBackgroundResource(R.drawable.wheel_bg);

        // Disable the sound effect default.
        this.setSoundEffectsEnabled(false);
    }

    /**
     * Called by draw to draw the child views. This may be overridden by derived classes to gain
     * control just before its children are drawn (but after its own view has been drawn).
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // After draw child, we do the following things:
        // +1, Draw the center rectangle.
        // +2, Draw the shadows on the top and bottom.

        drawCenterRect(canvas);

        drawShadows(canvas);
    }

    /**
     * setOrientation
     */
    @Override
    public void setOrientation(int orientation) {
        if (HORIZONTAL == orientation) {
            throw new IllegalArgumentException("The orientation must be VERTICAL");
        }

        super.setOrientation(orientation);
    }

    /**
     * Call when the ViewGroup is layout.
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        int galleryCenter = getCenterOfGallery();
        View v = this.getChildAt(0);

        int height = (null != v) ? v.getMeasuredHeight() : 50;
        int top = galleryCenter - height / 2;
        int bottom = top + height;

        mSelectorBound.set(getPaddingLeft(), top, getWidth() - getPaddingRight(), bottom);
    }

    /**
     * @see com.nj1s.lib.widget.TosGallery#setSelectedPositionInt(int)
     */
    @Override
    protected void selectionChanged() {
        super.selectionChanged();

        playSoundEffect(SoundEffectConstants.CLICK);
    }

    /**
     * Draw the selector drawable.
     * 
     * @param canvas
     */
    private void drawCenterRect(Canvas canvas) {
        if (null != mSelectorDrawable) {
            mSelectorDrawable.setBounds(mSelectorBound);
            mSelectorDrawable.draw(canvas);
        }
    }

    /**
     * Draw the shadow
     * 
     * @param canvas
     */
    private void drawShadows(Canvas canvas) {
        int height = (int) (2.0 * mSelectorBound.height());
        mTopShadow.setBounds(0, 0, getWidth(), height);
        mTopShadow.draw(canvas);

        mBottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
        mBottomShadow.draw(canvas);
    }
}
