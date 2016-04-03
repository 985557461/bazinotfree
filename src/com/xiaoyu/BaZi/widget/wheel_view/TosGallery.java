/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaoyu.BaZi.widget.wheel_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.*;
import android.view.animation.Transformation;
import android.widget.Scroller;
import com.xiaoyu.BaZi.R;

/**
 * A view that shows items in a center-locked, horizontally scrolling list.
 * <p>
 * The default values for the Gallery assume you will be using
 * {@link android.R.styleable#Theme_galleryItemBackground} as the background for each View given to
 * the Gallery from the Adapter. If you are not doing this, you may need to adjust some Gallery
 * properties, such as the spacing.
 * <p>
 * Views given to the Gallery should use {@link TosGallery.LayoutParams} as their layout parameters
 * type.
 *
 * @attr ref android.R.styleable#Gallery_animationDuration
 * @attr ref android.R.styleable#Gallery_spacing
 * @attr ref android.R.styleable#Gallery_gravity
 */
public class TosGallery extends TosAbsSpinner implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private static final String TAG = "Gallery";

    private static final boolean localLOGV = false;

    /**
     * Duration in milliseconds from the start of a scroll during which we're unsure whether the
     * user is scrolling or flinging.
     */
    private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;

    /**
     * Horizontal spacing between items.
     */
    private int mSpacing = 0;

    /**
     * How long the transition animation should run when a child view changes position, measured in
     * milliseconds.
     */
    private int mAnimationDuration = 400;

    /**
     * The alpha of items that are not selected.
     */
    private float mUnselectedAlpha;

    /**
     * Left most edge of a child seen so far during layout.
     */
    private int mLeftMost;

    /**
     * Right most edge of a child seen so far during layout.
     */
    private int mRightMost;

    private int mGravity;

    /**
     * Helper for detecting touch gestures.
     */
    private GestureDetector mGestureDetector;

    /**
     * The position of the item that received the user's down touch.
     */
    private int mDownTouchPosition;

    /**
     * The view of the item that received the user's down touch.
     */
    private View mDownTouchView;

    /**
     * Executes the delta scrolls from a fling or scroll movement.
     */
    private FlingRunnable mFlingRunnable = new FlingRunnable();

    /**
     * Sets mSuppressSelectionChanged = false. This is used to set it to false in the future. It
     * will also trigger a selection changed.
     */
    private Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {
        public void run() {
            mSuppressSelectionChanged = false;
            selectionChanged();
        }
    };

    /**
     * When fling runnable runs, it resets this to false. Any method along the path until the end of
     * its run() can set this to true to abort any remaining fling. For example, if we've reached
     * either the leftmost or rightmost item, we will set this to true.
     */
    private boolean mShouldStopFling;

    /**
     * The currently selected item's child.
     */
    private View mSelectedChild;

    /**
     * Whether to continuously callback on the item selected listener during a fling.
     */
    private boolean mShouldCallbackDuringFling = true;

    /**
     * Whether to callback when an item that is not selected is clicked.
     */
    private boolean mShouldCallbackOnUnselectedItemClick = true;

    /**
     * If true, do not callback to item selected listener.
     */
    private boolean mSuppressSelectionChanged;

    /**
     * If true, we have received the "invoke" (center or enter buttons) key down. This is checked
     * before we action on the "invoke" key up, and is subsequently cleared.
     */
    private boolean mReceivedInvokeKeyDown;

    private AdapterContextMenuInfo mContextMenuInfo;

    /**
     * If true, this onScroll is the first for this user's drag (remember, a drag sends many
     * onScrolls).
     */
    private boolean mIsFirstScroll;

    public TosGallery(Context context) {
        this(context, null);
    }

    public TosGallery(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.galleryStyle);
    }

    public TosGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setIsLongpressEnabled(true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Gallery, defStyle, 0);

        int index = a.getInt(R.styleable.Gallery_gravity, -1);
        if (index >= 0) {
            setGravity(index);
        }

        int animationDuration = a.getInt(R.styleable.Gallery_animationDuration, -1);
        if (animationDuration > 0) {
            setAnimationDuration(animationDuration);
        }

        int spacing = a.getDimensionPixelOffset(R.styleable.Gallery_spacing, 0);
        setSpacing(spacing);

        float unselectedAlpha = a.getFloat(R.styleable.Gallery_unselectedAlpha, 0.5f);
        setUnselectedAlpha(unselectedAlpha);

        a.recycle();

        // Deleted by LiHong at 2011/08/12 begin.
        //
        // Note: FLAG_USE_CHILD_DRAWING_ORDER and FLAG_SUPPORT_STATIC_TRANSFORMATIONS are
        // defined in ViewGroup class, they are protected static final, in this class, we
        // can not access the members, but we can use methods to replace these members.

        /**
         * // We draw the selected item last (because otherwise the item to the // right overlaps
         * it) mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;
         *
         * mGroupFlags |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;
         */

        //
        // Deleted by LiHong at 2011/08/12 end.

        // Added by LiHong at 2011/08/12 begin===================================.
        //

        // We draw the selected item last (because otherwise the item to the
        // right overlaps it)
        setChildrenDrawingOrderEnabled(true);

        setStaticTransformationsEnabled(true);

        // The scroll bar size.
        mScrollBarSize = ViewConfiguration.get(context).getScaledScrollBarSize();

        if (isOrientationVertical()) {
            mGravity = Gravity.CENTER_HORIZONTAL;
        } else {
            mGravity = Gravity.CENTER_VERTICAL;
        }
        //
        // Added by LiHong at 2011/08/12 end=====================================.
    }

    /**
     * Whether or not to callback on any {@link #getOnItemSelectedListener()} while the items are
     * being flinged. If false, only the final selected item will cause the callback. If true, all
     * items between the first and the final will cause callbacks.
     *
     * @param shouldCallback Whether or not to callback on the listener while the items are being
     *            flinged.
     */
    public void setCallbackDuringFling(boolean shouldCallback) {
        mShouldCallbackDuringFling = shouldCallback;
    }

    /**
     * Whether or not to callback when an item that is not selected is clicked. If false, the item
     * will become selected (and re-centered). If true, the {@link #getOnItemClickListener()} will
     * get the callback.
     *
     * @param shouldCallback Whether or not to callback on the listener when a item that is not
     *            selected is clicked.
     * @hide
     */
    public void setCallbackOnUnselectedItemClick(boolean shouldCallback) {
        mShouldCallbackOnUnselectedItemClick = shouldCallback;
    }

    /**
     * Sets how long the transition animation should run when a child view changes position. Only
     * relevant if animation is turned on.
     *
     * @param animationDurationMillis The duration of the transition, in milliseconds.
     *
     * @attr ref android.R.styleable#Gallery_animationDuration
     */
    public void setAnimationDuration(int animationDurationMillis) {
        mAnimationDuration = animationDurationMillis;
    }

    /**
     * Sets the spacing between items in a Gallery
     *
     * @param spacing The spacing in pixels between items in the Gallery
     *
     * @attr ref android.R.styleable#Gallery_spacing
     */
    public void setSpacing(int spacing) {
        mSpacing = spacing;
    }

    /**
     * Sets the alpha of items that are not selected in the Gallery.
     *
     * @param unselectedAlpha the alpha for the items that are not selected.
     *
     * @attr ref android.R.styleable#Gallery_unselectedAlpha
     */
    public void setUnselectedAlpha(float unselectedAlpha) {
        mUnselectedAlpha = unselectedAlpha;
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        t.clear();
        t.setAlpha(child == mSelectedChild ? 1.0f : mUnselectedAlpha);

        Log(" getChildStaticTransformation   mSelectedPosition =  " + mSelectedPosition + "   mFirstPosition = "
                + mFirstPosition + "     mSelectedChild = " + mSelectedChild);

        return true;
    }

    // Added by LiHong at 2011/09/29 begin ===========================
    //

    protected void onDrawHorizontalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        // This method is hide for client, add the bottom margin of the scroll bar.
        //
        // Note: If the bottom margin is not zero, we should override the
        // #invalidate(int, int, int, int) method so that the scroll bar can be fade when
        // completing scrolling.
        t -= mScrollBarBottomMargin;
        b -= mScrollBarBottomMargin;
        t = b - mScrollBarSize;

        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }

    /**
     * Mark the the area defined by the rect (l,t,r,b) as needing to be drawn. The coordinates of
     * the dirty rect are relative to the view. If the view is visible, {@link #onDraw} will be
     * called at some point in the future. This must be called from a UI thread. To call from a
     * non-UI thread, call {@link #postInvalidate()}.
     *
     * @param l the left position of the dirty region
     * @param t the top position of the dirty region
     * @param r the right position of the dirty region
     * @param b the bottom position of the dirty region
     */
    public void invalidate(int l, int t, int r, int b) {
        t -= (mScrollBarSize + mScrollBarBottomMargin);

        super.invalidate(l, t, r, b);
    }

    // Added by LiHong at 2011/09/29 end =============================

    @Override
    protected int computeHorizontalScrollExtent() {
        // Only 1 item is considered to be selected
        // return 1;

        // Added by LiHong at 2011/09/29 begin ==================
        //
        // Note: Support the horizontal scroll bar for this gallery.

        final int count = getChildCount();
        if (count > 0) {
            int extent = count * 100;

            View view = getChildAt(0);
            final int left = view.getLeft();
            int width = view.getWidth();
            if (width > 0) {
                boolean isFirst = (0 == mFirstPosition);
                // If the first position is zero and the left is more than zero, we do not add the
                // left extent.
                if (!(isFirst && left > 0)) {
                    extent += (left * 100) / width;
                }
            }

            view = getChildAt(count - 1);
            final int right = view.getRight();
            width = view.getWidth();
            if (width > 0) {
                boolean isLast = (mFirstPosition + count == mItemCount);
                // If the last child is show, we do no add the right extent.
                if (!(isLast && right < getWidth())) {
                    extent -= ((right - getWidth()) * 100) / width;
                }
            }

            return extent;
        }

        return 0;

        // Added by LiHong at 2011/09/29 end ====================
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        // Current scroll position is the same as the selected position
        // return mSelectedPosition;

        // Added by LiHong at 2011/09/29 begin ==================
        //
        // Note: Support the horizontal scroll bar for this gallery.

        if (mFirstPosition >= 0 && getChildCount() > 0) {
            final View view = getChildAt(0);
            final int left = view.getLeft();
            int width = view.getWidth();
            if (width > 0) {
                final int whichCol = mFirstPosition / 1;
                return Math.max(whichCol * 100 - (left * 100) / width, 0);
            }
        }

        return mSelectedPosition;

        // Added by LiHong at 2011/09/29 end ====================
    }

    @Override
    protected int computeHorizontalScrollRange() {
        // Scroll range is the same as the item count
        // return mItemCount;

        // Added by LiHong at 2011/09/29 begin ==================
        //
        // Note: Support the horizontal scroll bar for this gallery.

        final int numRows = 1;
        final int colCount = (mItemCount + numRows - 1) / numRows;
        return Math.max(colCount * 100, 0);

        // Added by LiHong at 2011/09/29 end ====================
    }

    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        /*
         * Gallery expects Gallery.LayoutParams.
         */
        return new TosGallery.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        /*
         * Remember that we are in layout to prevent more layout request from being generated.
         */
        mInLayout = true;
        layout(0, false);
        mInLayout = false;
    }

    @Override
    int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }

    /**
     * Tracks a motion scroll. In reality, this is used to do just about any movement to items
     * (touch scroll, arrow-key scroll, set an item as selected).
     *
     * @param deltaX Change in X from the previous event.
     */
    void trackMotionScroll(int deltaX) {

        if (getChildCount() == 0) {
            return;
        }

        boolean toLeft = deltaX < 0;

        // Deleted by LiHong at 2011/08/12 begin =========================
        //
        if (isSlotInCenter()) {
            // If the gallery scroll cycle, or item count not fill the gallery fully.
            if (!isScrollCycle() || getChildCount() >= mItemCount) {
                int limitedDeltaX = getLimitedMotionScrollAmount(toLeft, deltaX);
                if (limitedDeltaX != deltaX) {
                    // The above call returned a limited amount, so stop any scrolls/flings
                    mFlingRunnable.endFling(false);
                    onFinishedMovement();
                }
            }

            offsetChildrenLeftAndRight(deltaX);

            detachOffScreenChildren(toLeft);

            if (toLeft) {
                // If moved left, there will be empty space on the right
                fillToGalleryRight();
            } else {
                // Similarly, empty space on the left
                fillToGalleryLeft();
            }

            // Clear unused views
            mRecycler.clear();

            setSelectionToCenterChild();

            invalidate();
            return;
        }
        //
        // Deleted by LiHong at 2011/08/12 end ===========================

        // Add by ZhouYuanqi begin.
        if (toLeft) {
            View child = getChildAt(getChildCount() - 1);

            if (null != child && child.getRight() < getStopFlingPosition()) {
                deltaX = 0;
                if (null != mFlingRunnable) {
                    mFlingRunnable.stop(false);
                }
            }
        } else {
            // First child is out of gallery left bound.
            View child = getChildAt(0);
            if (null != child && child.getLeft() > (getWidth() - getStopFlingPosition())) {
                deltaX = 0;

                if (null != mFlingRunnable) {
                    mFlingRunnable.stop(false);
                }
            }
        }
        // Add by ZhouYuanqi end.

        offsetChildrenLeftAndRight(deltaX);

        detachOffScreenChildren(toLeft);

        if (toLeft) {
            // If moved left, there will be empty space on the right
            fillToGalleryRight();
        } else {
            // Similarly, empty space on the left
            fillToGalleryLeft();
        }

        // Clear unused views
        mRecycler.clear();

        // Must call this method, if not, it may lead crash when user scroll left or right.
        setSelectionToCenterChild();

        // Added by LiHong at 2011/09/29 to awake the scroll bar.
        awakenScrollBars();
        invalidate();
    }

    void trackMotionScrollVertical(int deltaY) {

        if (getChildCount() == 0) {
            return;
        }

        boolean toTop = deltaY < 0;

        // Deleted by LiHong at 2011/08/12 begin =========================
        //
        if (isSlotInCenter()) {
            // If the gallery scroll cycle, or item count not fill the gallery fully.
            if (!isScrollCycle() || getChildCount() >= mItemCount) {
                int limitedDeltaX = getLimitedMotionScrollAmount(toTop, deltaY);
                if (limitedDeltaX != deltaY) {
                    // The above call returned a limited amount, so stop any scrolls/flings
                    mFlingRunnable.endFling(false);
                    onFinishedMovement();
                }
            }

            offsetChildrenTopAndBottom(deltaY);

            detachOffScreenChildrenVertical(toTop);

            if (toTop) {
                // If moved left, there will be empty space on the right
                fillToGalleryBottom();
            } else {
                // Similarly, empty space on the left
                fillToGalleryTop();
            }

            // Clear unused views
            mRecycler.clear();

            setSelectionToCenterChildVertical();

            invalidate();
            return;
        }
        //
        // Deleted by LiHong at 2011/08/12 end ===========================

        // Add by ZhouYuanqi begin.
        if (toTop) {
            View child = getChildAt(getChildCount() - 1);

            if (null != child && child.getRight() < getStopFlingPosition()) {
                deltaY = 0;
                if (null != mFlingRunnable) {
                    mFlingRunnable.stop(false);
                }
            }
        } else {
            // First child is out of gallery left bound.
            View child = getChildAt(0);
            if (null != child && child.getLeft() > (getWidth() - getStopFlingPosition())) {
                deltaY = 0;

                if (null != mFlingRunnable) {
                    mFlingRunnable.stop(false);
                }
            }
        }
        // Add by ZhouYuanqi end.

        offsetChildrenTopAndBottom(deltaY);

        detachOffScreenChildrenVertical(toTop);

        if (toTop) {
            // If moved left, there will be empty space on the right
            fillToGalleryBottom();
        } else {
            // Similarly, empty space on the left
            fillToGalleryTop();
        }

        // Clear unused views
        mRecycler.clear();

        // Must call this method, if not, it may lead crash when user scroll left or right.
        setSelectionToCenterChild();

        // Added by LiHong at 2011/09/29 to awake the scroll bar.
        awakenScrollBars();
        invalidate();
    }

    int getLimitedMotionScrollAmount(boolean motionToLeft, int deltaX) {
        int extremeItemPosition = motionToLeft ? mItemCount - 1 : 0;
        View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);

        if (extremeChild == null) {
            return deltaX;
        }

        int extremeChildCenter = getCenterOfView(extremeChild);
        int galleryCenter = getCenterOfGallery();

        if (motionToLeft) {
            if (extremeChildCenter <= galleryCenter) {

                // The extreme child is past his boundary point!
                return 0;
            }
        } else {
            if (extremeChildCenter >= galleryCenter) {

                // The extreme child is past his boundary point!
                return 0;
            }
        }

        int centerDifference = galleryCenter - extremeChildCenter;

        return motionToLeft ? Math.max(centerDifference, deltaX) : Math.min(centerDifference, deltaX);
    }

    /**
     * Offset the horizontal location of all children of this view by the specified number of
     * pixels.
     *
     * @param offset the number of pixels to offset
     */
    private void offsetChildrenLeftAndRight(int offset) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).offsetLeftAndRight(offset);
        }
    }

    private void offsetChildrenTopAndBottom(int offset) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).offsetTopAndBottom(offset);
        }
    }

    /**
     * @return The center of this Gallery.
     */
    protected int getCenterOfGallery() {
        if (isOrientationVertical()) {
            return (getHeight() - getPaddingTop() - getPaddingBottom()) / 2 + getPaddingTop();
        }
        // return (getWidth() - mPaddingLeft - mPaddingRight) / 2 + mPaddingLeft;

        // Modified by LiHong at 2011/08/12 begin ========================
        //
        return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
        //
        // Modified by LiHong at 2011/08/12 end ==========================
    }

    /**
     * @return the stop fling position.
     */
    private float getStopFlingPosition() {
        if (isOrientationVertical()) {
            return (getHeight() - getPaddingTop() - getPaddingBottom()) * onStopFlingPosRatio() + getPaddingTop();
        }

        return (getWidth() - getPaddingLeft() - getPaddingRight()) * onStopFlingPosRatio() + getPaddingLeft();
    }

    /**
     * @return the position for stopping fling.
     */
    protected float onStopFlingPosRatio() {
        return 0.0f;
    }

    /**
     * @return The center of the given view.
     */
    private int getCenterOfView(View view) {
        if (isOrientationVertical()) {
            return view.getTop() + view.getHeight() / 2;
        }

        return view.getLeft() + view.getWidth() / 2;
    }

    /**
     * Detaches children that are off the screen (i.e.: Gallery bounds).
     *
     * @param toLeft Whether to detach children to the left of the Gallery, or to the right.
     */
    private void detachOffScreenChildren(boolean toLeft) {
        int numChildren = getChildCount();
        int firstPosition = mFirstPosition;
        int start = 0;
        int count = 0;

        if (toLeft) {
            final int galleryLeft = getPaddingLeft();// mPaddingLeft;
            for (int i = 0; i < numChildren; i++) {
                final View child = getChildAt(i);
                if (child.getRight() >= galleryLeft) {
                    break;
                } else {
                    count++;
                    mRecycler.put(firstPosition + i, child);
                }
            }

            // Do not detach the last child when the child is out of the left bound.
            if (count == numChildren) {
                count -= 1;
            }

        } else {
            final int galleryRight = getWidth() - getPaddingRight();// mPaddingRight;
            for (int i = numChildren - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                if (child.getLeft() <= galleryRight) {
                    break;
                } else {
                    start = i;
                    count++;
                    mRecycler.put(firstPosition + i, child);
                }
            }

            // Do not detach the first child when the child is out of the left bound.
            if (0 == start) {
                start += 1;
            }
        }

        detachViewsFromParent(start, count);

        if (toLeft) {
            mFirstPosition += count;

            // Added by LiHong at 2012/10/11 begin =======
            if (isScrollCycle()) {
                mFirstPosition = mFirstPosition % mItemCount;
            }
            // Added by LiHong at 2012/10/11 end =========
        }
    }

    private void detachOffScreenChildrenVertical(boolean toTop) {
        int numChildren = getChildCount();
        int firstPosition = mFirstPosition;
        int start = 0;
        int count = 0;

        if (toTop) {
            final int galleryTop = getPaddingTop();// mPaddingLeft;
            for (int i = 0; i < numChildren; i++) {
                final View child = getChildAt(i);
                if (child.getBottom() >= galleryTop) {
                    break;
                } else {
                    count++;
                    mRecycler.put(firstPosition + i, child);
                }
            }

            // Do not detach the last child when the child is out of the left bound.
            if (count == numChildren) {
                count -= 1;
            }
        } else {
            final int galleryBottom = getHeight() - getPaddingBottom();// mPaddingRight;
            for (int i = numChildren - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                if (child.getTop() <= galleryBottom) {
                    break;
                } else {
                    start = i;
                    count++;
                    mRecycler.put(firstPosition + i, child);
                }
            }

            // Do not detach the first child when the child is out of the left bound.
            if (0 == start) {
                start += 1;
            }
        }

        detachViewsFromParent(start, count);

        if (toTop) {
            mFirstPosition += count;

            // Added by LiHong at 2012/10/11 begin =======
            if (isScrollCycle()) {
                mFirstPosition = mFirstPosition % mItemCount;
            }
            // Added by LiHong at 2012/10/11 end =========
        }
    }

    /**
     * Scrolls the items so that the selected item is in its 'slot' (its center is the gallery's
     * center).
     */
    public void scrollIntoSlots() {
        if (isOrientationVertical()) {
            scrollIntoSlotsVertical();
            return;
        }

        // Deleted by LiHong at 2011/08/12 begin ====================================
        //
        if (isSlotInCenter()) {
            if (getChildCount() == 0 || mSelectedChild == null)
                return;

            int selectedCenter = getCenterOfView(mSelectedChild);
            int targetCenter = getCenterOfGallery();

            int scrollAmount = targetCenter - selectedCenter;
            if (scrollAmount != 0) {
                mFlingRunnable.startUsingDistance(scrollAmount);
            } else {
                onFinishedMovement();
            }

            return;
        }
        //
        // Deleted by LiHong at 2011/08/12 end ======================================

        // Added by LiHong at 2011/08/12 begin ====================================
        //
        // Note: Make the gallery item views always dock right or left sides.
        // If the gallery is playing animation, do nothing.
        //
        if (getChildCount() == 0) {
            return;
        }

        int scrollAmount = 0;

        if (0 == mFirstPosition) {
            // In these cases the gallery child count is equal or more than the item count
            // (adapter.getCount()),
            // and the gallery first child's left is bigger than zero, we should move the
            // first child anchors at the most left side of gallery.
            View child = getChildAt(0);

            // Make the first child anchors at the most left side of gallery when it is over
            // the left side of gallery.
            if (child.getLeft() >= 0) {
                scrollAmount = getPaddingLeft() - child.getLeft();
            } else {
                /*
                 * Delete by ZhouYuanqi on 2012/2/8. // Make the right child anchors at the right
                 * side of gallery. child = getChildAt(getChildCount() - 1); // If the child's right
                 * side is fully seeing, i.e, the child right side is // in the right of gallery. if
                 * (child.getRight() < (getRight() - getPaddingRight())) { scrollAmount = getWidth()
                 * - getPaddingRight() - child.getRight(); }
                 *
                 * // Add this line to solve the issue that shrink children. scrollAmount =
                 * getPaddingLeft() - m_firstChildOffset;
                 */

                // Add by ZhouYuanqi on 2012/2/8 for solve the issue
                // when scroll from right to left.
                View lastChild = getChildAt(getChildCount() - 1);

                if ((lastChild.getRight() - child.getLeft()) < (getRight() - getPaddingRight())) {
                    scrollAmount = getPaddingLeft() - mFirstChildOffset;
                } else if (lastChild.getRight() < (getRight() - getPaddingRight())) {
                    scrollAmount = getWidth() - getPaddingRight() - lastChild.getRight();
                }
            }
        }
        // If the most right view is the last item.
        else if (mFirstPosition + getChildCount() == mItemCount) {
            View child = getChildAt(getChildCount() - 1);
            // If the child's right side is fully seeing, i.e, the child right side is
            // in the right of gallery.
            if (child.getRight() < (getRight() - getPaddingRight())) {
                scrollAmount = getWidth() - getPaddingRight() - child.getRight();
            }
        }

        if (0 != scrollAmount) {
            // Call startUsingDistance method to implement elastic effect.
            mFlingRunnable.startUsingDistance(scrollAmount);
        } else {
            onFinishedMovement();
        }

        //
        // Added by LiHong at 2011/08/12 end ======================================
    }

    private void scrollIntoSlotsVertical() {
        // Deleted by LiHong at 2011/08/12 begin ====================================
        //
        if (isSlotInCenter()) {
            if (getChildCount() == 0 || mSelectedChild == null)
                return;

            int selectedCenter = getCenterOfView(mSelectedChild);
            int targetCenter = getCenterOfGallery();

            int scrollAmount = targetCenter - selectedCenter;
            if (scrollAmount != 0) {
                mFlingRunnable.startUsingDistance(scrollAmount);
            } else {
                onFinishedMovement();
            }

            return;
        }
        //
        // Deleted by LiHong at 2011/08/12 end ======================================

        // Added by LiHong at 2011/08/12 begin ====================================
        //
        // Note: Make the gallery item views always dock right or left sides.
        // If the gallery is playing animation, do nothing.
        //

        if (getChildCount() == 0) {
            return;
        }

        int scrollAmount = 0;

        if (0 == mFirstPosition) {
            // In these cases the gallery child count is equal or more than the item count
            // (adapter.getCount()),
            // and the gallery first child's left is bigger than zero, we should move the
            // first child anchors at the most left side of gallery.
            View child = getChildAt(0);

            // Make the first child anchors at the most left side of gallery when it is over
            // the left side of gallery.
            if (child.getTop() >= 0) {
                scrollAmount = getPaddingTop() - child.getTop();
            } else {
                /*
                 * Delete by ZhouYuanqi on 2012/2/8. // Make the right child anchors at the right
                 * side of gallery. child = getChildAt(getChildCount() - 1); // If the child's right
                 * side is fully seeing, i.e, the child right side is // in the right of gallery. if
                 * (child.getRight() < (getRight() - getPaddingRight())) { scrollAmount = getWidth()
                 * - getPaddingRight() - child.getRight(); }
                 *
                 * // Add this line to solve the issue that shrink children. scrollAmount =
                 * getPaddingLeft() - m_firstChildOffset;
                 */

                // Add by ZhouYuanqi on 2012/2/8 for solve the issue
                // when scroll from right to left.
                View lastChild = getChildAt(getChildCount() - 1);

                if ((lastChild.getBottom() - child.getTop()) < (getBottom() - getPaddingBottom())) {
                    scrollAmount = getPaddingLeft() - mFirstChildOffset;
                } else if (lastChild.getBottom() < (getBottom() - getPaddingBottom())) {
                    scrollAmount = getHeight() - getPaddingBottom() - lastChild.getBottom();
                }
            }
        }
        // If the most right view is the last item.
        else if (mFirstPosition + getChildCount() == mItemCount) {
            View child = getChildAt(getChildCount() - 1);
            // If the child's right side is fully seeing, i.e, the child right side is
            // in the right of gallery.
            if (child.getBottom() < (getBottom() - getPaddingBottom())) {
                scrollAmount = getHeight() - getPaddingBottom() - child.getBottom();
            }
        }

        if (0 != scrollAmount) {
            // Call startUsingDistance method to implement elastic effect.
            mFlingRunnable.startUsingDistance(scrollAmount);
        } else {
            onFinishedMovement();
        }

        //
        // Added by LiHong at 2011/08/12 end ======================================
    }

    private void onFinishedMovement() {
        if (mSuppressSelectionChanged) {
            mSuppressSelectionChanged = false;

            // We haven't been callbacking during the fling, so do it now
            super.selectionChanged();
        }
        invalidate();
    }

    @Override
    protected void selectionChanged() {
        if (!mSuppressSelectionChanged) {
            super.selectionChanged();
        }
    }

    /**
     * Looks for the child that is closest to the center and sets it as the selected child.
     */
    private void setSelectionToCenterChild() {

        View selView = mSelectedChild;
        if (mSelectedChild == null)
            return;

        int galleryCenter = getCenterOfGallery();

        // Common case where the current selected position is correct
        if (selView.getLeft() <= galleryCenter && selView.getRight() >= galleryCenter) {
            return;
        }

        // TODO better search
        int closestEdgeDistance = Integer.MAX_VALUE;
        int newSelectedChildIndex = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {

            View child = getChildAt(i);

            if (child.getLeft() <= galleryCenter && child.getRight() >= galleryCenter) {
                // This child is in the center
                newSelectedChildIndex = i;
                break;
            }

            int childClosestEdgeDistance = Math.min(Math.abs(child.getLeft() - galleryCenter),
                    Math.abs(child.getRight() - galleryCenter));
            if (childClosestEdgeDistance < closestEdgeDistance) {
                closestEdgeDistance = childClosestEdgeDistance;
                newSelectedChildIndex = i;
            }
        }

        int newPos = mFirstPosition + newSelectedChildIndex;

        // Added by LiHong at 2012/11/18 begin ======
        if (isScrollCycle()) {
            newPos = newPos % mItemCount;
        }
        // Added by LiHong at 2012/11/18 end ========

        if (newPos != mSelectedPosition) {
            setSelectedPositionInt(newPos);
            setNextSelectedPositionInt(newPos);
            checkSelectionChanged();
        }
    }

    /**
     * Looks for the child that is closest to the center and sets it as the selected child.
     */
    private void setSelectionToCenterChildVertical() {

        View selView = mSelectedChild;
        if (mSelectedChild == null)
            return;

        int galleryCenter = getCenterOfGallery();

        if (null != selView) {
            // Common case where the current selected position is correct
            if (selView.getTop() <= galleryCenter && selView.getBottom() >= galleryCenter) {
                return;
            }
        }

        // TODO better search
        int closestEdgeDistance = Integer.MAX_VALUE;
        int newSelectedChildIndex = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {

            View child = getChildAt(i);

            if (child.getTop() <= galleryCenter && child.getBottom() >= galleryCenter) {
                // This child is in the center
                newSelectedChildIndex = i;
                break;
            }

            int childClosestEdgeDistance = Math.min(Math.abs(child.getTop() - galleryCenter),
                    Math.abs(child.getBottom() - galleryCenter));
            if (childClosestEdgeDistance < closestEdgeDistance) {
                closestEdgeDistance = childClosestEdgeDistance;
                newSelectedChildIndex = i;
            }
        }

        int newPos = mFirstPosition + newSelectedChildIndex;

        // Added by LiHong at 2012/11/18 begin ======
        if (isScrollCycle()) {
            newPos = newPos % mItemCount;
        }
        // Added by LiHong at 2012/11/18 end ========

        if (newPos != mSelectedPosition) {
            setSelectedPositionInt(newPos);
            setNextSelectedPositionInt(newPos);
            checkSelectionChanged();
        }
    }

    /**
     * Creates and positions all views for this Gallery.
     * <p>
     * We layout rarely, most of the time {@link #trackMotionScroll(int)} takes care of
     * repositioning, adding, and removing children.
     *
     * @param delta Change in the selected position. +1 means the selection is moving to the right,
     *            so views are scrolling to the left. -1 means the selection is moving to the left.
     */
    @Override
    void layout(int delta, boolean animate) {
        if (isOrientationVertical()) {
            layoutVertical(delta, animate);
            return;
        }

        // Make the first child's position does not change.
        // View firstChild = getChildAt(0);
        // int firstChildLeft =(null != firstChild) ? firstChild.getLeft() : 0;
        int childrenLeft = mSpinnerPadding.left + mFirstChildOffset;

        // Modified by LiHong at 2011/08/12 begin===========================.
        //
        // int childrenWidth = mRight - mLeft - mSpinnerPadding.left - mSpinnerPadding.right;
        // int childrenWidth = getRight() - getLeft() - mSpinnerPadding.left -
        // mSpinnerPadding.right;
        //
        // Modified by LiHong at 2011/08/12 end=============================.

        if (mDataChanged) {
            handleDataChanged();
        }

        // Handle an empty gallery by removing all views.
        if (mItemCount == 0) {
            resetList();
            return;
        }

        // Update to the new selected position.
        if (mNextSelectedPosition >= 0) {
            setSelectedPositionInt(mNextSelectedPosition);
        }

        // All views go in recycler while we are in layout
        recycleAllViews();

        // Clear out old views
        // removeAllViewsInLayout();
        detachAllViewsFromParent();

        /*
         * These will be used to give initial positions to views entering the gallery as we scroll
         */
        mRightMost = 0;
        mLeftMost = 0;

        // Make selected view and center it

        /*
         * mFirstPosition will be decreased as we add views to the left later on. The 0 for x will
         * be offset in a couple lines down.
         */
        // mFirstPosition = mSelectedPosition;
        // View sel = makeAndAddView(mSelectedPosition, 0, 0, true);
        mFirstPosition = mSelectedPosition;
        View sel = makeAndAddView(mFirstPosition, 0, 0, true);

        // Put the selected child in the center

        // Modify by LiHong at 2011/08/12 begin ============================
        //
        // Note: Make the selected item offset is near left of the gallery, default
        // the first item is selected, so the first item's offset is near left of gallery.
        //

        // int selectedOffset = childrenLeft + (childrenWidth / 2) - (sel.getWidth() / 2);
        int selectedOffset = childrenLeft + mSpacing;

        if (isSlotInCenter()) {
            int childrenWidth = getRight() - getLeft() - mSpinnerPadding.left - mSpinnerPadding.right;
            selectedOffset = childrenLeft + (childrenWidth / 2) - (sel.getWidth() / 2);
        }

        //
        // Modify by LiHong at 2011/08/12 end ============================

        sel.offsetLeftAndRight(selectedOffset);

        fillToGalleryRight();
        fillToGalleryLeft();

        // Flush any cached views that did not get reused above
        mRecycler.clear();

        invalidate();
        // checkSelectionChanged();

        mDataChanged = false;
        mNeedSync = false;
        setNextSelectedPositionInt(mSelectedPosition);

        updateSelectedItemMetadata();

        // Added by LiHong at 2012/11/17 begin ==========
        //
        // NOTE: If the child count is less than the item count, we should disable cycle scroll,
        // but, we should NOT change the mIsScrollCycle which is set by callers, because
        // after user enlarge the item count such as add data dynamically, if the item count
        // is bigger than child count, the gallery should be scrolling cycle.
        mIsScrollCycleTemp = !(getChildCount() >= mItemCount);
        // Added by LiHong at 2012/11/17 end ============
    }

    void layoutVertical(int delta, boolean animate) {
        // Make the first child's position does not change.
        // View firstChild = getChildAt(0);
        // int firstChildLeft =(null != firstChild) ? firstChild.getLeft() : 0;
        int childrenTop = mSpinnerPadding.top + mFirstChildOffset;

        // Modified by LiHong at 2011/08/12 begin===========================.
        //
        // int childrenWidth = mRight - mLeft - mSpinnerPadding.left - mSpinnerPadding.right;
        // int childrenWidth = getRight() - getLeft() - mSpinnerPadding.left -
        // mSpinnerPadding.right;
        //
        // Modified by LiHong at 2011/08/12 end=============================.

        if (mDataChanged) {
            handleDataChanged();
        }

        // Handle an empty gallery by removing all views.
        if (mItemCount == 0) {
            resetList();
            return;
        }

        // Update to the new selected position.
        if (mNextSelectedPosition >= 0) {
            setSelectedPositionInt(mNextSelectedPosition);
        }

        // All views go in recycler while we are in layout
        recycleAllViews();

        // Clear out old views
        // removeAllViewsInLayout();
        detachAllViewsFromParent();

        /*
         * These will be used to give initial positions to views entering the gallery as we scroll
         */
        mRightMost = 0;
        mLeftMost = 0;

        // Make selected view and center it

        /*
         * mFirstPosition will be decreased as we add views to the left later on. The 0 for x will
         * be offset in a couple lines down.
         */
        // mFirstPosition = mSelectedPosition;
        // View sel = makeAndAddView(mSelectedPosition, 0, 0, true);
        mFirstPosition = mSelectedPosition;
        View sel = makeAndAddViewVertical(mFirstPosition, 0, 0, true);

        // Put the selected child in the center

        // Modify by LiHong at 2011/08/12 begin ============================
        //
        // Note: Make the selected item offset is near left of the gallery, default
        // the first item is selected, so the first item's offset is near left of gallery.
        //

        // int selectedOffset = childrenLeft + (childrenWidth / 2) - (sel.getWidth() / 2);
        int selectedOffset = childrenTop + mSpacing;

        if (isSlotInCenter()) {
            int childrenHeight = getBottom() - getTop() - mSpinnerPadding.top - mSpinnerPadding.bottom;
            selectedOffset = childrenTop + (childrenHeight / 2) - (sel.getHeight() / 2);
        }

        //
        // Modify by LiHong at 2011/08/12 end ============================

        sel.offsetTopAndBottom(selectedOffset);

        fillToGalleryBottom();
        fillToGalleryTop();

        // Flush any cached views that did not get reused above
        mRecycler.clear();

        invalidate();
        // checkSelectionChanged();

        mDataChanged = false;
        mNeedSync = false;
        setNextSelectedPositionInt(mSelectedPosition);

        updateSelectedItemMetadata();

        // Added by LiHong at 2012/11/17 begin ==========
        //
        // NOTE: If the child count is less than the item count, we should disable cycle scroll,
        // but, we should NOT change the mIsScrollCycle which is set by callers, because
        // after user enlarge the item count such as add data dynamically, if the item count
        // is bigger than child count, the gallery should be scrolling cycle.
        mIsScrollCycleTemp = !(getChildCount() >= mItemCount);
        // Added by LiHong at 2012/11/17 end ============
    }

    private void fillToGalleryLeft() {

        // Added by LiHong at 2012/10/11 begin =======
        if (isScrollCycle()) {
            fillToGalleryLeftCycle();
            return;
        }
        // Added by LiHong at 2012/10/11 end =========

        int itemSpacing = mSpacing;
        int galleryLeft = getPaddingLeft();// mPaddingLeft;

        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curRightEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            // No children available!
            curPosition = 0;
            // curRightEdge = mRight - mLeft - mPaddingRight;
            curRightEdge = getRight() - getLeft() - getPaddingRight();
            mShouldStopFling = true;
        }

        while (curRightEdge > galleryLeft && curPosition >= 0) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curRightEdge, false);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition--;
        }
    }

    private void fillToGalleryTop() {

        // Added by LiHong at 2012/10/11 begin =======
        if (isScrollCycle()) {
            fillToGalleryTopCycle();
            return;
        }
        // Added by LiHong at 2012/10/11 end =========

        int itemSpacing = mSpacing;
        int galleryTop = getPaddingTop();// mPaddingLeft;

        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curRightEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curRightEdge = prevIterationView.getTop() - itemSpacing;
        } else {
            // No children available!
            curPosition = 0;
            // curRightEdge = mRight - mLeft - mPaddingRight;
            curRightEdge = getBottom() - getTop() - getPaddingBottom();
            mShouldStopFling = true;
        }

        while (curRightEdge > galleryTop && curPosition >= 0) {
            prevIterationView = makeAndAddViewVertical(curPosition, curPosition - mSelectedPosition, curRightEdge,
                    false);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curRightEdge = prevIterationView.getTop() - itemSpacing;
            curPosition--;
        }
    }

    private void fillToGalleryRight() {

        // Added by LiHong at 2012/10/11 begin =======
        if (isScrollCycle()) {
            fillToGalleryRightCycle();
            return;
        }
        // Added by LiHong at 2012/10/11 end =========

        int itemSpacing = mSpacing;

        // int galleryRight = mRight - mLeft - mPaddingRight;
        int galleryRight = getRight() - getLeft() - getPaddingRight();

        int numChildren = getChildCount();
        int numItems = mItemCount;

        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curLeftEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            mFirstPosition = curPosition = mItemCount - 1;
            curLeftEdge = getPaddingLeft();// mPaddingLeft;
            mShouldStopFling = true;
        }

        while (curLeftEdge < galleryRight && curPosition < numItems) {

            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition++;
        }
    }

    private void fillToGalleryBottom() {

        // Added by LiHong at 2012/10/11 begin =======
        if (isScrollCycle()) {
            fillToGalleryBottomCycle();
            return;
        }
        // Added by LiHong at 2012/10/11 end =========

        int itemSpacing = mSpacing;

        // int galleryRight = mRight - mLeft - mPaddingRight;
        int galleryRight = getBottom() - getTop() - getPaddingRight();

        int numChildren = getChildCount();
        int numItems = mItemCount;

        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curLeftEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curLeftEdge = prevIterationView.getBottom() + itemSpacing;
        } else {
            mFirstPosition = curPosition = mItemCount - 1;
            curLeftEdge = getPaddingTop();// mPaddingLeft;
            mShouldStopFling = true;
        }

        while (curLeftEdge < galleryRight && curPosition < numItems) {

            prevIterationView = makeAndAddViewVertical(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getBottom() + itemSpacing;
            curPosition++;
        }
    }

    /**
     * Obtain a view, either by pulling an existing view from the recycler or by getting a new one
     * from the adapter. If we are animating, make sure there is enough information in the view's
     * layout parameters to animate from the old to new positions.
     *
     * @param position Position in the gallery for the view to obtain
     * @param offset Offset from the selected position
     * @param x X-coordintate indicating where this view should be placed. This will either be the
     *            left or right edge of the view, depending on the fromLeft paramter
     * @param fromLeft Are we posiitoning views based on the left edge? (i.e., building from left to
     *            right)?
     * @return A view that has been added to the gallery
     */
    private View makeAndAddView(int position, int offset, int x, boolean fromLeft) {

        View child;

        if (!mDataChanged) {
            child = mRecycler.get(position);
            if (child != null) {

                // Can reuse an existing view
                int childLeft = child.getLeft();

                // Remember left and right edges of where views have been placed
                mRightMost = Math.max(mRightMost, childLeft + child.getMeasuredWidth());
                mLeftMost = Math.min(mLeftMost, childLeft);

                // Position the view
                setUpChild(child, offset, x, fromLeft);

                return child;
            }
        }

        // Nothing found in the recycler -- ask the adapter for a view
        child = mAdapter.getView(position, null, this);

        // Position the view
        setUpChild(child, offset, x, fromLeft);

        return child;
    }

    private View makeAndAddViewVertical(int position, int offset, int y, boolean fromTop) {

        View child;

        if (!mDataChanged) {
            child = mRecycler.get(position);
            if (child != null) {

                // Can reuse an existing view
                int childTop = child.getTop();

                // Remember left and right edges of where views have been placed
                mRightMost = Math.max(mRightMost, childTop + child.getMeasuredHeight());
                mLeftMost = Math.min(mLeftMost, childTop);

                // Position the view
                setUpChildVertical(child, offset, y, fromTop);

                return child;
            }
        }

        // Nothing found in the recycler -- ask the adapter for a view
        child = mAdapter.getView(position, null, this);

        // Position the view
        setUpChildVertical(child, offset, y, fromTop);

        return child;
    }

    /**
     * Helper for makeAndAddView to set the position of a view and fill out its layout paramters.
     *
     * @param child The view to position
     * @param offset Offset from the selected position
     * @param x X-coordintate indicating where this view should be placed. This will either be the
     *            left or right edge of the view, depending on the fromLeft paramter
     * @param fromLeft Are we posiitoning views based on the left edge? (i.e., building from left to
     *            right)?
     */
    private void setUpChild(View child, int offset, int x, boolean fromLeft) {

        // Respect layout params that are already in the view. Otherwise
        // make some up...
        TosGallery.LayoutParams lp = (TosGallery.LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = (TosGallery.LayoutParams) generateDefaultLayoutParams();
        }

        addViewInLayout(child, fromLeft ? -1 : 0, lp);

        child.setSelected(offset == 0);

        // Get measure specs
        int childHeightSpec = getChildMeasureSpec(mHeightMeasureSpec, mSpinnerPadding.top
                + mSpinnerPadding.bottom, lp.height);
        int childWidthSpec = getChildMeasureSpec(mWidthMeasureSpec, mSpinnerPadding.left
                + mSpinnerPadding.right, lp.width);

        // Measure child
        child.measure(childWidthSpec, childHeightSpec);

        int childLeft;
        int childRight;

        // Position vertically based on gravity setting
        int childTop = calculateTop(child, true);
        int childBottom = childTop + child.getMeasuredHeight();

        int width = child.getMeasuredWidth();
        if (fromLeft) {
            childLeft = x;
            childRight = childLeft + width;
        } else {
            childLeft = x - width;
            childRight = x;
        }

        child.layout(childLeft, childTop, childRight, childBottom);
    }

    /**
     * Helper for makeAndAddView to set the position of a view and fill out its layout paramters.
     *
     * @param child The view to position
     * @param offset Offset from the selected position
     * @param y X-coordintate indicating where this view should be placed. This will either be the
     *            left or right edge of the view, depending on the fromLeft paramter
     * @param fromTop Are we posiitoning views based on the left edge? (i.e., building from left to
     *            right)?
     */
    private void setUpChildVertical(View child, int offset, int y, boolean fromTop) {

        // Respect layout params that are already in the view. Otherwise
        // make some up...
        TosGallery.LayoutParams lp = (TosGallery.LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = (TosGallery.LayoutParams) generateDefaultLayoutParams();
        }

        addViewInLayout(child, fromTop ? -1 : 0, lp);

        child.setSelected(offset == 0);

        // Get measure specs
        int childHeightSpec = getChildMeasureSpec(mHeightMeasureSpec, mSpinnerPadding.top
                + mSpinnerPadding.bottom, lp.height);
        int childWidthSpec = getChildMeasureSpec(mWidthMeasureSpec, mSpinnerPadding.left
                + mSpinnerPadding.right, lp.width);

        // Measure child
        child.measure(childWidthSpec, childHeightSpec);

        int childTop;
        int childBottom;

        // Position vertically based on gravity setting
        int childLeft = calculateLeft(child, true);
        int childRight = childLeft + child.getMeasuredWidth();

        int height = child.getMeasuredHeight();
        if (fromTop) {
            childTop = y;
            childBottom = childTop + height;
        } else {
            childTop = y - height;
            childBottom = y;
        }

        child.layout(childLeft, childTop, childRight, childBottom);
    }

    /**
     * Figure out vertical placement based on mGravity
     *
     * @param child Child to place
     * @return Where the top of the child should be
     */
    private int calculateTop(View child, boolean duringLayout) {
        // int myHeight = duringLayout ? mMeasuredHeight : getHeight();
        int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
        int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight();

        int childTop = 0;

        switch (mGravity) {
        case Gravity.TOP:
            childTop = mSpinnerPadding.top;
            break;
        case Gravity.CENTER_VERTICAL:
            int availableSpace = myHeight - mSpinnerPadding.bottom - mSpinnerPadding.top - childHeight;
            childTop = mSpinnerPadding.top + (availableSpace / 2);
            break;
        case Gravity.BOTTOM:
            childTop = myHeight - mSpinnerPadding.bottom - childHeight;
            break;
        }
        return childTop;
    }

    private int calculateLeft(View child, boolean duringLayout) {
        // int myHeight = duringLayout ? mMeasuredHeight : getHeight();
        int myWidth = duringLayout ? getMeasuredWidth() : getWidth();
        int childWidth = duringLayout ? child.getMeasuredWidth() : child.getWidth();

        int childLeft = 0;

        switch (mGravity) {
        case Gravity.LEFT:
            childLeft = mSpinnerPadding.left;
            break;
        case Gravity.CENTER_HORIZONTAL:
            int availableSpace = myWidth - mSpinnerPadding.right - mSpinnerPadding.left - childWidth;
            childLeft = mSpinnerPadding.left + (availableSpace / 2);
            break;
        case Gravity.RIGHT:
            childLeft = myWidth - mSpinnerPadding.right - childWidth;
            break;
        }
        return childLeft;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Give everything to the gesture detector
        boolean retValue = mGestureDetector.onTouchEvent(event);

        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            // Helper method for lifted finger
            onUp();
        } else if (action == MotionEvent.ACTION_CANCEL) {
            onCancel();
        }

        return retValue;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onSingleTapUp(MotionEvent e) {

        if (mDownTouchPosition >= 0) {

            // If the gallery is scroll cycle, we must calculate the touch position.
            if (isScrollCycle()) {
                mDownTouchPosition = mDownTouchPosition % getCount();
            }

            if (isSlotInCenter()) {
                // An item tap should make it selected, so scroll to this child.
                scrollToChild(mDownTouchPosition - mFirstPosition);
            }

            performItemSelect(mDownTouchPosition);

            // Also pass the click so the client knows, if it wants to.
            if (mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition) {
                performItemClick(mDownTouchView, mDownTouchPosition, mAdapter.getItemId(mDownTouchPosition));
            }

            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        // Added by LiHong at 2011/09/07 begin ===================
        //
        // User can disable scroll action when the child count is less than the adapter item count.
        //
        boolean disableScroll = shouldDisableScroll();

        if (disableScroll) {
            return true;
        }

        //
        // Added by LiHong at 2011/09/07 end =====================

        if (!mShouldCallbackDuringFling) {
            // We want to suppress selection changes

            // Remove any future code to set mSuppressSelectionChanged = false
            removeCallbacks(mDisableSuppressSelectionChangedRunnable);

            // This will get reset once we scroll into slots
            if (!mSuppressSelectionChanged)
                mSuppressSelectionChanged = true;
        }

        if (isOrientationVertical()) {
            // Accelerate or decelerate the velocity of gallery on X directioin.
            velocityY *= getVelocityRatio();
            // Fling the gallery!
            mFlingRunnable.startUsingVelocity((int) -velocityY);
        } else {
            // Accelerate or decelerate the velocity of gallery on X directioin.
            velocityX *= getVelocityRatio();
            // Fling the gallery!
            mFlingRunnable.startUsingVelocity((int) -velocityX);
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        // Added by LiHong at 2011/09/07 begin ===================
        //
        // User can disable scroll action when the child count is less than the adapter item count.
        //
        boolean disableScroll = shouldDisableScroll();

        if (disableScroll) {
            return true;
        }

        mScrolling = true;
        //
        // Added by LiHong at 2011/09/07 end =====================

        if (localLOGV)
            Log.v(TAG, String.valueOf(e2.getX() - e1.getX()));

        /*
         * Now's a good time to tell our parent to stop intercepting our events! The user has moved
         * more than the slop amount, since GestureDetector ensures this before calling this method.
         * Also, if a parent is more interested in this touch's events than we are, it would have
         * intercepted them by now (for example, we can assume when a Gallery is in the ListView, a
         * vertical scroll would not end up in this method since a ListView would have intercepted
         * it by now).
         */
        // mParent.requestDisallowInterceptTouchEvent(true);
        getParent().requestDisallowInterceptTouchEvent(true);

        // As the user scrolls, we want to callback selection changes so related-
        // info on the screen is up-to-date with the gallery's selection
        if (!mShouldCallbackDuringFling) {
            if (mIsFirstScroll) {
                /*
                 * We're not notifying the client of selection changes during the fling, and this
                 * scroll could possibly be a fling. Don't do selection changes until we're sure it
                 * is not a fling.
                 */
                if (!mSuppressSelectionChanged)
                    mSuppressSelectionChanged = true;
                postDelayed(mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT);
            }
        } else {
            if (mSuppressSelectionChanged)
                mSuppressSelectionChanged = false;
        }

        // Track the motion
        if (isOrientationVertical()) {
            trackMotionScrollVertical(-1 * (int) distanceY);
        } else {
            trackMotionScroll(-1 * (int) distanceX);
        }

        mIsFirstScroll = false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onDown(MotionEvent e) {

        // Kill any existing fling/scroll
        mFlingRunnable.stop(false);

        // Get the item's view that was touched
        mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());

        if (mDownTouchPosition >= 0) {
            mDownTouchView = getChildAt(mDownTouchPosition - mFirstPosition);
            mDownTouchView.setPressed(true);
        }

        // Reset the multiple-scroll tracking state
        mIsFirstScroll = true;

        // Must return true to get matching events for this down event.
        return true;
    }

    /**
     * Called when a touch event's action is MotionEvent.ACTION_UP.
     */
    protected void onUp() {

        if (mFlingRunnable.mScroller.isFinished()) {
            scrollIntoSlots();
        }

        dispatchUnpress();
    }

    /**
     * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
     */
    void onCancel() {
        onUp();
    }

    /**
     * {@inheritDoc}
     */
    public void onLongPress(MotionEvent e) {

        if (mDownTouchPosition < 0) {
            return;
        }

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        long id = getItemIdAtPosition(mDownTouchPosition);
        dispatchLongPress(mDownTouchView, mDownTouchPosition, id);
    }

    // Unused methods from GestureDetector.OnGestureListener below

    /**
     * {@inheritDoc}
     */
    public void onShowPress(MotionEvent e) {
    }

    // Unused methods from GestureDetector.OnGestureListener above

    private void dispatchPress(View child) {

        if (child != null) {
            child.setPressed(true);
        }

        setPressed(true);
    }

    protected void dispatchUnpress() {

        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).setPressed(false);
        }

        setPressed(false);
    }

    @Override
    public void dispatchSetSelected(boolean selected) {
        /*
         * We don't want to pass the selected state given from its parent to its children since this
         * widget itself has a selected state to give to its children.
         */
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {

        // Show the pressed state on the selected child
        if (mSelectedChild != null) {
            mSelectedChild.setPressed(pressed);
        }
    }

    @Override
    protected ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {

        final int longPressPosition = getPositionForView(originalView);
        if (longPressPosition < 0) {
            return false;
        }

        final long longPressId = mAdapter.getItemId(longPressPosition);
        return dispatchLongPress(originalView, longPressPosition, longPressId);
    }

    @Override
    public boolean showContextMenu() {

        if (isPressed() && mSelectedPosition >= 0) {
            int index = mSelectedPosition - mFirstPosition;
            View v = getChildAt(index);
            return dispatchLongPress(v, mSelectedPosition, mSelectedRowId);
        }

        return false;
    }

    private boolean dispatchLongPress(View view, int position, long id) {
        boolean handled = false;

        if (mOnItemLongClickListener != null) {
            handled = mOnItemLongClickListener.onItemLongClick(this, mDownTouchView, mDownTouchPosition, id);
        }

        if (!handled) {
            mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
            handled = super.showContextMenuForChild(this);
        }

        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }

        return handled;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Gallery steals all key events
        return event.dispatch(this);
    }

    /**
     * Handles left, right, and clicking
     *
     * @see View#onKeyDown
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {

        case KeyEvent.KEYCODE_DPAD_LEFT:
            if (movePrevious()) {
                playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
            }
            return true;

        case KeyEvent.KEYCODE_DPAD_RIGHT:
            if (moveNext()) {
                playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
            }
            return true;

        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER:
            mReceivedInvokeKeyDown = true;
            // fallthrough to default handling
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER: {

            if (mReceivedInvokeKeyDown) {
                if (mItemCount > 0) {

                    dispatchPress(mSelectedChild);
                    postDelayed(new Runnable() {
                        public void run() {
                            dispatchUnpress();
                        }
                    }, ViewConfiguration.getPressedStateDuration());

                    int selectedIndex = mSelectedPosition - mFirstPosition;
                    performItemClick(getChildAt(selectedIndex), mSelectedPosition,
                            mAdapter.getItemId(mSelectedPosition));
                }
            }

            // Clear the flag
            mReceivedInvokeKeyDown = false;

            return true;
        }
        }

        return super.onKeyUp(keyCode, event);
    }

    boolean movePrevious() {
        if (mItemCount > 0 && mSelectedPosition > 0) {
            // scrollToChild(mSelectedPosition - mFirstPosition - 1);
            return true;
        } else {
            return false;
        }
    }

    boolean moveNext() {
        if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
            // scrollToChild(mSelectedPosition - mFirstPosition + 1);
            return true;
        } else {
            return false;
        }
    }

    private boolean scrollToChild(int childPosition) {
        View child = getChildAt(childPosition);

        if (child != null) {
            int distance = getCenterOfGallery() - getCenterOfView(child);
            mFlingRunnable.startUsingDistance(distance);
            return true;
        }

        return false;
    }

    @Override
    protected void setSelectedPositionInt(int position) {
        super.setSelectedPositionInt(position);

        // Updates any metadata we keep about the selected item.
        updateSelectedItemMetadata();
    }

    private void updateSelectedItemMetadata() {

        View oldSelectedChild = mSelectedChild;

        Log(" updateSelectedItemMetadata   mSelectedPosition =  " + mSelectedPosition + "   mFirstPosition = "
                + mFirstPosition);

        int index = mSelectedPosition - mFirstPosition;
        if (isScrollCycle()) {
            if (mFirstPosition > mSelectedPosition) {
                index = mItemCount - mFirstPosition + mSelectedPosition;
            }
        }

        View child = mSelectedChild = getChildAt(index);
        if (child == null) {
            return;
        }

        child.setSelected(true);
        child.setFocusable(true);

        if (hasFocus()) {
            child.requestFocus();
        }

        // We unfocus the old child down here so the above hasFocus check
        // returns true
        if (oldSelectedChild != null) {

            // Make sure its drawable state doesn't contain 'selected'
            oldSelectedChild.setSelected(false);

            // Make sure it is not focusable anymore, since otherwise arrow keys
            // can make this one be focused
            oldSelectedChild.setFocusable(false);
        }

    }

    /**
     * Describes how the child views are aligned.
     * 
     * @param gravity
     * 
     * @attr ref android.R.styleable#Gallery_gravity
     */
    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            requestLayout();
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {

        // Removed by LiHong at 201109/21 begin =============
        //
        //

        int selectedIndex = mSelectedPosition - mFirstPosition;
        // Just to be safe
        if (selectedIndex < 0)
            return i;

        if (i == childCount - 1) {
            // Draw the selected child last
            return selectedIndex;
        } else if (i >= selectedIndex) {
            // Move the children to the right of the selected child earlier one
            return i + 1;
        } else {
            // Keep the children to the left of the selected child the same
            return i;
        }
        //
        // Removed by LiHong at 201109/21 end ===============

        // We make the drawing order reverses the children order in list.
        // return (childCount - 1 - i);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

        /*
         * The gallery shows focus by focusing the selected item. So, give focus to our selected
         * item instead. We steal keys from our selected item elsewhere.
         */
        if (gainFocus && mSelectedChild != null) {
            mSelectedChild.requestFocus(direction);
        }

    }

    /**
     * Responsible for fling behavior. Use {@link #startUsingVelocity(int)} to initiate a fling.
     * Each frame of the fling is handled in {@link #run()}. A FlingRunnable will keep re-posting
     * itself until the fling is done.
     * 
     */
    private class FlingRunnable implements Runnable {
        /**
         * Tracks the decay of a fling scroll
         */
        private Scroller mScroller;

        /**
         * X value reported by mScroller on the previous fling
         */
        private int mLastFlingX;
        private int mLastFlingY;

        public FlingRunnable() {
            mScroller = new Scroller(getContext());
        }

        private void startCommon() {
            // Remove any pending flings
            removeCallbacks(this);
        }

        public void startUsingVelocity(int initialVelocity) {
            if (initialVelocity == 0)
                return;

            startCommon();

            if (isOrientationVertical()) {
                int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
                mLastFlingY = initialY;
                mScroller.fling(0, initialY, 0, initialVelocity, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
                post(this);
                return;
            }

            int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingX = initialX;
            mScroller.fling(initialX, 0, initialVelocity, 0, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            post(this);
        }

        public void startUsingDistance(int distance) {
            if (distance == 0)
                return;

            if (isOrientationVertical()) {
                startCommon();
                mScrolling = true;
                mLastFlingY = 0;
                mScroller.startScroll(0, 0, 0, -distance, mAnimationDuration);
                post(this);
                return;
            }

            startCommon();
            mScrolling = true;
            mLastFlingX = 0;
            mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);
            post(this);
        }

        public void stop(boolean scrollIntoSlots) {
            removeCallbacks(this);
            endFling(scrollIntoSlots);
        }

        private void endFling(boolean scrollIntoSlots) {

            mScrolling = false;

            /*
             * Force the scroller's status to finished (without setting its position to the end)
             */
            mScroller.forceFinished(true);

            if (scrollIntoSlots)
                scrollIntoSlots();

            // Added by LiHong at 2011/09/14 begin ==============
            //
            onEndFling();
            //
            // Added by LiHong at 2011/09/14 end ================
        }

        public void run() {

            if (isOrientationVertical()) {
                runVertical();
                return;
            }

            if (mItemCount == 0) {
                endFling(true);
                return;
            }

            mShouldStopFling = false;

            final Scroller scroller = mScroller;
            boolean more = scroller.computeScrollOffset();
            final int x = scroller.getCurrX();

            // Flip sign to convert finger direction to list items direction
            // (e.g. finger moving down means list is moving towards the top)
            int delta = mLastFlingX - x;

            // Pretend that each frame of a fling scroll is a touch scroll
            if (delta > 0) {
                // Moving towards the left. Use first view as mDownTouchPosition
                mDownTouchPosition = mFirstPosition;

                // Don't fling more than 1 screen
                // delta = Math.min(getWidth() - mPaddingLeft - mPaddingRight - 1, delta);
                delta = Math.min(getWidth() - getPaddingLeft() - getPaddingRight() - 1, delta);
            } else {
                // Moving towards the right. Use last view as mDownTouchPosition
                int offsetToLast = getChildCount() - 1;
                mDownTouchPosition = mFirstPosition + offsetToLast;

                // Don't fling more than 1 screen
                delta = Math.max(-(getWidth() - getPaddingRight() - getPaddingLeft() - 1), delta);
            }

            trackMotionScroll(delta);

            if (more && !mShouldStopFling) {
                mLastFlingX = x;
                post(this);
            } else {
                endFling(true);
            }
        }

        public void runVertical() {

            if (mItemCount == 0) {
                endFling(true);
                return;
            }

            mShouldStopFling = false;

            final Scroller scroller = mScroller;
            boolean more = scroller.computeScrollOffset();
            final int y = scroller.getCurrY();

            // Flip sign to convert finger direction to list items direction
            // (e.g. finger moving down means list is moving towards the top)
            int delta = mLastFlingY - y;

            // Pretend that each frame of a fling scroll is a touch scroll
            if (delta > 0) {
                // Moving towards the left. Use first view as mDownTouchPosition
                mDownTouchPosition = mFirstPosition;

                // Don't fling more than 1 screen
                // delta = Math.min(getWidth() - mPaddingLeft - mPaddingRight - 1, delta);
                delta = Math.min(getHeight() - getPaddingTop() - getPaddingBottom() - 1, delta);
            } else {
                // Moving towards the right. Use last view as mDownTouchPosition
                int offsetToLast = getChildCount() - 1;
                mDownTouchPosition = mFirstPosition + offsetToLast;

                // Don't fling more than 1 screen
                delta = Math.max(-(getHeight() - getPaddingBottom() - getPaddingTop() - 1), delta);
            }

            trackMotionScrollVertical(delta);

            if (more && !mShouldStopFling) {
                mLastFlingY = y;
                post(this);
            } else {
                endFling(true);
            }
        }
    }

    /**
     * Gallery extends LayoutParams to provide a place to hold current Transformation information
     * along with previous position/transformation info.
     * 
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    // Added by LiHong at 2011/08/12 begin ============================================.
    //
    // Author: Lee Hong
    //
    // Date 2012/10/12
    //
    /**
     * Indicate disable scroll action when the child item is less than mItemCount. in other word,
     * the children can be fully seeing in the gallery.
     */
    private boolean mIsDisableScroll = false;

    /**
     * scrolling and animating flag
     */
    private boolean mScrolling = false;

    /**
     * The first child offset.
     */
    private int mFirstChildOffset = 0;

    /**
     * The bottom margin of the horizontal scroll bar.
     */
    private int mScrollBarBottomMargin = 0;

    /**
     * The scroll bar size.
     */
    private int mScrollBarSize = 5;

    /**
     * The scroll velocity ratio.
     */
    private float mVelocityRatio = 1.0f;

    /**
     * Indicate the gallery scroll cycle or not.
     */
    private boolean mIsScrollCycle = false;

    /**
     * The temporary member for mIsScrollCycle
     */
    private boolean mIsScrollCycleTemp = true;

    /**
     * Slot into center. The default behavior of gallery is that the selected child will be slot in
     * center.
     */
    private boolean mIsSlotCenter = false;

    /**
     * The orientation, default value is horizontal
     */
    private int mOrientation = HORIZONTAL;

    /**
     * The fling listener.
     */
    private OnEndFlingListener mOnEndFlingListener = null;

    /**
     * The orientation horizontal
     */
    public static final int HORIZONTAL = 0x01;

    /**
     * The orientation vertical
     */
    public static final int VERTICAL = 0x02;

    /**
     * Select a child.
     */
    private boolean performItemSelect(int childPosition) {
        if (childPosition != mSelectedPosition) {
            setSelectedPositionInt(childPosition);
            setNextSelectedPositionInt(childPosition);
            checkSelectionChanged();

            return true;
        }

        return false;
    }

    /**
     * Set the first child offset.
     * 
     * @param firstChildOffset The value of first child offset.
     * 
     * @author LeeHong
     */
    public void setFirstChildOffset(int firstChildOffset) {
        mFirstChildOffset = firstChildOffset;
    }

    /**
     * Set the first position of the gallery.
     * 
     * @param firstPosition The first position.
     * 
     * @author LeeHong
     */
    public void setFirstPosition(int firstPosition) {
        mFirstPosition = firstPosition;
    }

    /**
     * Indicate the gallery selected slot in center of not, default is false.
     * 
     * @param isSlotCenter
     */
    public void setSlotInCenter(boolean isSlotCenter) {
        mIsSlotCenter = isSlotCenter;
    }

    /**
     * Indicate the gallery selected slot in center of not, default is false.
     * 
     * @return
     */
    public boolean isSlotInCenter() {
        return mIsSlotCenter;
    }

    /**
     * Indicate the gallery layout in vertical or not.
     * 
     * @return
     */
    private boolean isOrientationVertical() {
        return (mOrientation == VERTICAL);
    }

    /**
     * @return the m_oritentation
     */
    public int getOrientation() {
        return mOrientation;
    }

    /**
     * @param orientation the m_orientation to set
     */
    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    /**
     * Set the gallery fling listener.
     * 
     * @param listener The OnEndFlingListener instance.
     * 
     * @author LeeHong
     */
    public void setOnEndFlingListener(OnEndFlingListener listener) {
        mOnEndFlingListener = listener;
    }

    /**
     * Call this method to disable the scroll action, this method makes affect only when the all
     * items are displaying in the gallery.
     * 
     * @param disableScroll true if disable the scroll action, otherwise false, default false.
     * 
     * @author LeeHong
     */
    public void setDisableScroll(boolean disableScroll) {
        mIsDisableScroll = disableScroll;
    }

    /**
     * Set the scroll bar bottom margin
     * 
     * @param scrollBarBottmMargin
     * 
     * @author LeeHong
     */
    public void setScrollBarBottomMargin(int scrollBarBottomMargin) {
        mScrollBarBottomMargin = scrollBarBottomMargin;
    }

    /**
     * Set the scroll bar size.
     * 
     * @param scrollBarSize The scroll bar.
     * 
     * @author LeeHong
     */
    public void setScrollBarSize(int scrollBarSize) {
        mScrollBarSize = scrollBarSize;
    }

    /**
     * Get the The position of the first child displayed.
     * 
     * @return
     * 
     * @author LeeHong
     */
    public int getFirstPosition() {
        return mFirstPosition;
    }

    /**
     * Get the spacing between children.
     * 
     * @return The spacing of the children.
     * 
     * @author LeeHong
     */
    public int getSpacing() {
        return mSpacing;
    }

    /**
     * Return the scrolling flag.
     * 
     * @return true if the gallery is scrolling, otherwise false.
     * 
     * @author LeeHong
     */
    public boolean isScrolling() {
        return mScrolling;
    }

    /**
     * Scroll gallery items towards left or right direction with the one item's width.
     * 
     * @param toLeft Indicate offset towards left or right.
     * 
     * @return The offset of the items.
     * 
     * @author LeeHong
     */
    public int scrollGalleryItems(boolean toLeft) {
        if (0 == getChildCount()) {
            return 0;
        }

        int offset = 0;

        if (toLeft) {
            View child = getChildAt(getChildCount() - 1);
            offset = child.getRight() - this.getRight() + this.getPaddingRight();
            offset = Math.max(offset, 0);

            // The item is not the last one.
            if (0 == offset && (mFirstPosition + getChildCount() != mItemCount)) {
                offset += (null != child) ? child.getWidth() : 0;
            }
        } else {
            View child = getChildAt(0);
            offset = child.getLeft() - this.getPaddingLeft();
            offset = Math.min(offset, 0);

            // The current item is not the first one.
            if (0 == offset && 0 != mFirstPosition) {
                offset -= (null != child) ? child.getWidth() : 0;
            }
        }

        // If the offset is not zero, scroll these items.
        if (0 != offset) {
            // trackMotionScroll(-1 * offset);
            if (null != mFlingRunnable) {
                mFlingRunnable.startUsingDistance(-1 * offset);
            }
        }

        return (-1 * offset);
    }

    /**
     * Scroll gallery items towards left or right direction with the one item's width.
     * 
     * @param offset The scroll offset
     * 
     * @author LeeHong
     */
    public int scrollGalleryItems(int offset) {
        if (null != mFlingRunnable) {
            mFlingRunnable.startUsingDistance(-1 * offset);
        }

        return (-1 * offset);
    }

    /**
     * Get the index from specified point.
     * 
     * @param pt The specified point.
     * 
     * @return The based zero index, -1 if not found child which bound not contains the point.
     * 
     * @author LeeHong
     */
    public int getItemIndexFromPoint(android.graphics.Point pt) {
        int nChildCount = getChildCount();
        int nIndex = -1;
        Rect rc = new Rect();
        this.getDrawingRect(rc);

        if (rc.contains(pt.x, pt.y)) {
            for (int i = 0; i < nChildCount; ++i) {
                // Get the rectangle of the child.
                getChildAt(i).getHitRect(rc);

                if (rc.contains(pt.x, pt.y)) {
                    nIndex = i;
                    break;
                }
            }
        }

        return (nIndex >= 0) ? (nIndex + mFirstPosition) : -1;
    }

    /**
     * Called when the gallery ends fling operation.
     * 
     * @author LeeHong
     */
    protected void onEndFling() {
        if (null != mOnEndFlingListener) {
            mOnEndFlingListener.onEndFling(this);
        }
    }

    /**
     * Called when fling occurs, this method can return ratio value to accelerate or decelerate the
     * velocity of gallery.
     * 
     * @return default return 1.0f.
     * 
     * @author LeeHong
     */
    public float getVelocityRatio() {
        return mVelocityRatio;
    }

    /**
     * Set the scroll or fling velocity ratio, value is in the range [0.5, 1.5], default value is
     * 1.0f.
     * 
     * @param velocityRatio
     * 
     * @author LeeHong
     */
    public void setVelocityRatio(float velocityRatio) {
        mVelocityRatio = velocityRatio;

        if (mVelocityRatio < 0.5f) {
            mVelocityRatio = 0.5f;
        } else if (mVelocityRatio > 1.5f) {
            mVelocityRatio = 1.5f;
        }
    }

    /**
     * Indicate the should disable scroll action.
     * 
     * @return true if disable scroll, otherwise false.
     * 
     * @author LeeHong
     */
    protected boolean shouldDisableScroll() {
        if (mIsDisableScroll) {
            if (getChildCount() < mItemCount) {
                return false;
            }

            // First child is out of gallery left bound.
            View child = getChildAt(0);
            if (null != child && child.getLeft() < getLeft()) {
                return false;
            }

            // Last child is out of gallery right bound.
            child = getChildAt(getChildCount() - 1);
            if (null != child && child.getRight() > getRight()) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (MotionEvent.ACTION_UP == e.getAction()) {
            if (mDownTouchPosition >= 0) {
                // Also pass the click so the client knows, if it wants to.
                if (mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition) {
                    performItemDoubleClick(mDownTouchView, mDownTouchPosition, mAdapter.getItemId(mDownTouchPosition));
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    /**
     * This method is used to indicate the point is contained in a child or not.
     * 
     * @param x The x coordinate to the gallery.
     * @param y The y coordinate to the gallery.
     * 
     * @return true if the point contained in a child.
     * 
     * @author LeeHong
     */
    public boolean isPointInChild(float x, float y) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View child = getChildAt(i);
            if (x >= child.getLeft() && x <= child.getRight() && y >= child.getTop() && y <= child.getBottom()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Set the scroll cycle.
     * 
     * @param scrollCycle
     */
    public void setScrollCycle(boolean scrollCycle) {
        mIsScrollCycle = scrollCycle;
    }

    /**
     * Get the flag for scroll cycle.
     * 
     * @return
     */
    public boolean isScrollCycle() {
        return mIsScrollCycle && mIsScrollCycleTemp;
    }

    /**
     * @author LeeHong
     */
    protected void Log(String msg) {
        if (localLOGV) {
            Log.d(TAG, msg);
        }
    }

    private void fillToGalleryLeftCycle() {
        int itemSpacing = mSpacing;
        int galleryLeft = getPaddingLeft();// mPaddingLeft;

        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curRightEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            // No children available!
            curPosition = 0;
            // curRightEdge = mRight - mLeft - mPaddingRight;
            curRightEdge = getRight() - getLeft() - getPaddingRight();
            mShouldStopFling = true;
        }

        while (curRightEdge > galleryLeft && curPosition >= 0) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curRightEdge, false);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition--;
        }

        // Added by LiHong at 2012/10/11 begin ===========
        curPosition = mItemCount - 1;
        while (curRightEdge > galleryLeft && getChildCount() < mItemCount) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curRightEdge, false);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition--;
        }
        // Added by LiHong at 2012/10/11 end =============
    }

    private void fillToGalleryTopCycle() {
        int itemSpacing = mSpacing;
        int galleryTop = getPaddingTop();// mPaddingLeft;

        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curRightEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curRightEdge = prevIterationView.getTop() - itemSpacing;
        } else {
            // No children available!
            curPosition = 0;
            // curRightEdge = mRight - mLeft - mPaddingRight;
            curRightEdge = getBottom() - getTop() - getPaddingBottom();
            mShouldStopFling = true;
        }

        while (curRightEdge > galleryTop && curPosition >= 0) {
            prevIterationView = makeAndAddViewVertical(curPosition, curPosition - mSelectedPosition, curRightEdge,
                    false);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curRightEdge = prevIterationView.getTop() - itemSpacing;
            curPosition--;
        }

        // Added by LiHong at 2012/10/11 begin ===========
        curPosition = mItemCount - 1;
        while (curRightEdge > galleryTop && getChildCount() < mItemCount) {
            prevIterationView = makeAndAddViewVertical(curPosition, curPosition - mSelectedPosition, curRightEdge,
                    false);

            // Remember some state
            mFirstPosition = curPosition;

            // Set state for next iteration
            curRightEdge = prevIterationView.getTop() - itemSpacing;
            curPosition--;
        }
        // Added by LiHong at 2012/10/11 end =============
    }

    private void fillToGalleryRightCycle() {
        int itemSpacing = mSpacing;

        // int galleryRight = mRight - mLeft - mPaddingRight;
        int galleryRight = getRight() - getLeft() - getPaddingRight();

        int numChildren = getChildCount();
        int numItems = mItemCount;

        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curLeftEdge;

        Log("  fillToGalleryRightCycle mFirstPosition = " + mFirstPosition);

        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            mFirstPosition = curPosition = mItemCount - 1;
            curLeftEdge = getPaddingLeft();// mPaddingLeft;
            mShouldStopFling = true;
        }

        while (curLeftEdge < galleryRight && curPosition < numItems) {

            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition++;
        }

        // Added by LiHong at 2012/10/11 begin ===========
        curPosition = curPosition % numItems;
        while (curLeftEdge <= galleryRight && getChildCount() < mItemCount) {
            prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition++;
        }
        // Added by LiHong at 2012/10/11 end =============
    }

    private void fillToGalleryBottomCycle() {
        int itemSpacing = mSpacing;

        // int galleryRight = mRight - mLeft - mPaddingRight;
        int galleryBottom = getBottom() - getTop() - getPaddingBottom();

        int numChildren = getChildCount();
        int numItems = mItemCount;

        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curLeftEdge;

        Log("  fillToGalleryRightCycle mFirstPosition = " + mFirstPosition);

        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curLeftEdge = prevIterationView.getBottom() + itemSpacing;
        } else {
            mFirstPosition = curPosition = mItemCount - 1;
            curLeftEdge = getPaddingTop();// mPaddingLeft;
            mShouldStopFling = true;
        }

        while (curLeftEdge < galleryBottom && curPosition < numItems) {

            prevIterationView = makeAndAddViewVertical(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getBottom() + itemSpacing;
            curPosition++;
        }

        // Added by LiHong at 2012/10/11 begin ===========
        curPosition = curPosition % numItems;
        while (curLeftEdge <= galleryBottom && getChildCount() < mItemCount) {
            prevIterationView = makeAndAddViewVertical(curPosition, curPosition - mSelectedPosition, curLeftEdge, true);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getBottom() + itemSpacing;
            curPosition++;
        }
        // Added by LiHong at 2012/10/11 end =============
    }

    /**
     * This interface defines methods for TosGallery.
     * 
     * @author LeeHong
     */
    public interface OnEndFlingListener {
        /**
         * Called when the fling operation ends.
         * 
         * @param v The gallery view.
         */
        public void onEndFling(TosGallery v);
    }
    //
    // Added by LiHong at 2011/08/12 end ============================================.
}
