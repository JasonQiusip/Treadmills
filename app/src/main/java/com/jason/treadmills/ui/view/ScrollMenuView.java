package com.jason.treadmills.ui.view;


import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.OverScroller;

import com.jason.treadmills.utils.Logger;

public class ScrollMenuView extends ViewGroup{

    private int mMaximumFlingVelocity;
    private int mMinFlingVelocity;
    private ViewConfiguration vc;
    private int desireWidth;
    private int desireHeight;
    private MarginLayoutParams layoutParams;
    private OverScroller mScroller;
    private float startX = 0f;
    private VelocityTracker velocityTracker;
    private int mTouchSlop;
    private boolean mIsScrolling;
    private GestureDetectorCompat mGestureDetector;
    // Edge effect / overscroll tracking objects.
    private EdgeEffectCompat mEdgeEffectTop;
    private EdgeEffectCompat mEdgeEffectBottom;
    private EdgeEffectCompat mEdgeEffectLeft;
    private EdgeEffectCompat mEdgeEffectRight;

    private boolean mEdgeEffectTopActive;
    private boolean mEdgeEffectBottomActive;
    private boolean mEdgeEffectLeftActive;
    private boolean mEdgeEffectRightActive;
    private int childWidth;
    private float startY;
    private int mActivePointerId;
    private int mTouchSlopSquare;
    private float mLastFocusX;
    private float mLastFocusY;
    private float focusX;
    private float focusY;
    private long ANIMATED_SCROLL_GAP = 200;
    private long mLastScroll;
    private Camera mCamera;


    public ScrollMenuView(Context context) {
        super(context);
        initParams(context);
    }

    public ScrollMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context);
    }

    private void initParams(Context context) {
        mScroller = new OverScroller(context, new DecelerateInterpolator());
        vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = vc.getScaledMaximumFlingVelocity();
//        mGestureDetector = new GestureDetectorCompat(context, mOnGestureListener);
        mEdgeEffectLeft = new EdgeEffectCompat(context);
        mEdgeEffectTop = new EdgeEffectCompat(context);
        mEdgeEffectRight = new EdgeEffectCompat(context);
        mEdgeEffectBottom = new EdgeEffectCompat(context);
        mTouchSlopSquare = mTouchSlop * mTouchSlop;
        mCamera = new Camera();
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 计算所有child view 要占用的空间
        desireWidth = 0;
        desireHeight = 0;
        int count = getChildCount();
        for (int i = 0; i < count; ++i) {
            View v = getChildAt(i);
            if (v.getVisibility() != View.GONE) {
                measureChildWithMargins(v, widthMeasureSpec, 0, heightMeasureSpec, 0);
                layoutParams = (MarginLayoutParams) v.getLayoutParams();
                desireWidth += v.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
                childWidth = v.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
                desireHeight = Math.max(desireHeight, v.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin);
            }
        }

        // count with padding
        desireWidth += getPaddingLeft() + getPaddingRight();
        desireHeight += getPaddingTop() + getPaddingBottom();

        // see if the size is big enough
        desireWidth = Math.max(desireWidth, getSuggestedMinimumWidth());
        desireHeight = Math.max(desireHeight, getSuggestedMinimumHeight());

        setMeasuredDimension(resolveSize(desireWidth, widthMeasureSpec),
                resolveSize(desireHeight, heightMeasureSpec));

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        int count = getChildCount();
        for(int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(child.getVisibility() != View.GONE){
                int childWidth = child.getMeasuredWidth();
                MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
                child.layout(childLeft + layoutParams.leftMargin, layoutParams.topMargin,
                        childLeft + childWidth + layoutParams.rightMargin, child.getMeasuredHeight() + layoutParams.bottomMargin);
                childLeft += childWidth + layoutParams.leftMargin + layoutParams.rightMargin;
            }
        }
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);

        // Always handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll.
            mIsScrolling = false;
            return false; // Do not intercept touch event, let the child handle it
        }
        calFocusXY(event, action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startX = mLastFocusX = focusX;
                startY = mLastFocusY = focusY;
                break;
            case MotionEvent.ACTION_MOVE: {
                if (mIsScrolling) {
                    return true;
                }
                final int xDiff = calculateDistanceX(event);
                if (Math.abs(xDiff) > mTouchSlop) {
                    // Start scrolling!
                    mIsScrolling = true;
                    return true;
                }
                break;
            }
        }

        return false;
    }

    private int calculateDistanceX(MotionEvent ev) {
        return (int)ev.getX() - (int)startX;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        checkVelocityTracker();
        velocityTracker.addMovement(event);
        int action = event.getAction();

        calFocusXY(event, action);

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                rotate();
                startX = mLastFocusX = focusX;
                startY = mLastFocusY = focusY;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (focusX - mLastFocusX);
                int deltaY = (int) (focusY - mLastFocusY);

                moveBy(-deltaX, 0);
                mLastFocusX = focusX;
                mLastFocusY = focusY;
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final float velocityY = velocityTracker.getYVelocity(mActivePointerId);
                final float velocityX = velocityTracker.getXVelocity(mActivePointerId);
                fling(-velocityX, -velocityY);

                break;
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.recycle();
                break;
        }

        return true;
    }

    Matrix matrix=new Matrix();
    void rotate()
    {
        mCamera.save();
        mCamera.rotateY(30);
        mCamera.getMatrix(matrix);
        mCamera.restore();
//        matrix.preTranslate(-100, -100);
//        matrix.postTranslate(100, 100);

    }
    private void calFocusXY(MotionEvent event, int action) {
        final boolean pointerUp =
                (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? event.getActionIndex() : -1;

        // Determine focal point
        float sumX = 0, sumY = 0;
        final int count = event.getPointerCount();
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;
            sumX += event.getX(i);
            sumY += event.getY(i);
        }
        final int div = pointerUp ? count - 1 : count;
        focusX = sumX / div;
        focusY = sumY / div;
    }

    private void checkVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void fling(float velocityX, float velocityY) {
        mScroller.forceFinished(true);
        int mScrollX = getScrollX();
        Logger.showErrorLog("fling  ", "" + mScrollX + "  " + desireWidth + " getwidth " + getWidth());
        int maxX = desireWidth - getWidth();
        if (mScrollX > maxX) {
            // 超出了右边界，弹回
            mScroller.startScroll(mScrollX, 0, maxX - mScrollX, 0);
            ViewCompat.postInvalidateOnAnimation(this);
        } else if (mScrollX < 0) {
            // 超出了左边界，弹回
            mScroller.startScroll(mScrollX, 0, -mScrollX, 0);
            ViewCompat.postInvalidateOnAnimation(this);
        } else if (Math.abs(velocityX) >= mMinFlingVelocity && maxX > 0) {
            mScroller.fling(mScrollX, 0, (int) velocityX, 0, 0, maxX, 0, 0);
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        canvas.save();
        setChildTransformation(child, matrix);
        //initialize canvas state. Child 0,0 coordinates will match canvas 0,0
        canvas.translate(child.getLeft(), child.getTop());
        //set child transformation on canvas
        canvas.concat(matrix);
        return super.drawChild(canvas, child, drawingTime);
    }

    private void setChildTransformation(View child, Matrix m){
        m.reset();

//		addChildRotation(child, m);
        addChildScale(child, m);
//		addChildCircularPathZOffset(child, m);
//        addChildAdjustPosition(child,m);

        //set coordinate system origin to center of child
        m.preTranslate(-child.getWidth()/2f, -child.getHeight()/2f);
        //move back
        m.postTranslate(child.getWidth()/2f, child.getHeight()/2f);

    }

    private void addChildScale(View v,Matrix m){
        final float f = getScaleFactor(getChildsCenter(v));
        m.postScale(f, f);
    }

    private int getChildsCenter(View v){
        final int w = v.getRight() - v.getLeft();
        return v.getLeft() + w/2;
    }

    private void addChildAdjustPosition(View child, Matrix m) {
        final int c = getChildsCenter(child);
        final float crp = getClampedRelativePosition(getRelativePosition(c), 12);
        final float d = 10f * 0.1f * 0.5f * crp * getSpacingMultiplierOnCirlce(c);

        m.postTranslate(d, 0f);
    }

    private float getSpacingMultiplierOnCirlce(int childCenter){
        float x = getRelativePosition(childCenter)/2;
        return (float) Math.sin(Math.acos(x));
    }

    private float getClampedRelativePosition(float position, float threshold){
        if(position < 0){
            if(position < -threshold) return -1f;
            else return position/threshold;
        }
        else{
            if(position > threshold) return 1;
            else return position/threshold;
        }
    }

    private float getScaleFactor(int childCenter){
        return 1f;
    }

    private float getRelativePosition(int pixexPos){
        final int half = getWidth()/2;
        final int centerPos = getScrollX() + half;

        return (pixexPos - centerPos)/((float) half);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Logger.showErrorLog("dispatchDraw", " canvas ");

        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    public void moveBy(int deltaX, int deltaY) {
        scrollBy(deltaX, 0);
    }

    /**
     * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
     *
     * @param dx the number of pixels to scroll by on the X axis
     * @param dy the number of pixels to scroll by on the Y axis
     */
    public final void smoothScrollBy(int dx, int dy) {
        if (getChildCount() == 0) {
            // Nothing to do.
            return;
        }
        long duration = AnimationUtils.currentAnimationTimeMillis() - mLastScroll;
        if (duration > ANIMATED_SCROLL_GAP) {
            final int width = getWidth() - getPaddingLeft() - getPaddingRight();
            final int right = getChildAt(0).getWidth();
            final int maxX = Math.max(0, right - width);
            final int scrollX = (int)mLastFocusX;
            dx = Math.max(0, Math.min(scrollX + dx, maxX)) - scrollX;

            mScroller.startScroll(scrollX, (int)mLastFocusY, dx, 0);
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            scrollBy(dx, dy);
        }
        mLastScroll = AnimationUtils.currentAnimationTimeMillis();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
//        Logger.showErrorLog("computeScroll  ","....................----------------");
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    // ----------------------------------------------------------------------
    // The rest of the implementation is for custom per-child layout parameters.
    // If you do not need these (for example you are writing a layout manager
    // that does fixed positioning of its children), you can drop all of this.

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ScrollMenuView.LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /**
     * Custom per-child layout information.
     */
    public static class LayoutParams extends MarginLayoutParams {
        /**
         * The gravity to apply with the View to which these layout parameters
         * are associated.
         */
//        public int gravity = Gravity.TOP | Gravity.START;

        public static int POSITION_MIDDLE = 0;
        public static int POSITION_LEFT = 1;
        public static int POSITION_RIGHT = 2;

        public int position = POSITION_MIDDLE;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            // Pull the layout param values from the layout XML during
            // inflation.  This is not needed if you don't care about
            // changing the layout behavior in XML.
//            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.CustomLayoutLP);
//            gravity = a.getInt(R.styleable.CustomLayoutLP_android_layout_gravity, gravity);
//            position = a.getInt(R.styleable.CustomLayoutLP_layout_position, position);
//            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

}


