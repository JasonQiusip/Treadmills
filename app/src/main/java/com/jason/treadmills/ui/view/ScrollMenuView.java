package com.jason.treadmills.ui.view;


import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.OverScroller;

public class ScrollMenuView extends ViewGroup{

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

    public ScrollMenuView(Context context) {
        super(context);
        initParams(context);
    }

    public ScrollMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context);
    }

    private void initParams(Context context) {
        mScroller = new OverScroller(context);
        vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mGestureDetector = new GestureDetectorCompat(context, mOnGestureListener);
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
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e("moveBy", "onInterceptTouchEvent: ");
        final int action = MotionEventCompat.getActionMasked(ev);

        // Always handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll.
            mIsScrolling = false;
            return false; // Do not intercept touch event, let the child handle it
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e("moveBy", "ACTION_DOWN: " + mTouchSlop);
                initStartPoint(ev);
                break;
            case MotionEvent.ACTION_MOVE: {
                if (mIsScrolling) {
                    // We're currently scrolling, so yes, intercept the
                    // touch event!
                    return true;
                }

                final int xDiff = calculateDistanceX(ev);

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
        return mGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);

    }

//    private void checkVelocityTracker() {
//        if (velocityTracker == null) {
//            velocityTracker = VelocityTracker.obtain();
//        } else {
//            velocityTracker.clear();
//        }
//    }

    private final GestureDetector.SimpleOnGestureListener mOnGestureListener
            = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onDown(MotionEvent e) {
            mScroller.forceFinished(true);
            initStartPoint(e);
            ViewCompat.postInvalidateOnAnimation(ScrollMenuView.this);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.e("onScroll ", "distanceX:  "+ distanceX);
            moveBy((int)distanceX, 0);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            fling((int) -velocityX, (int) -velocityY);
            return true;
        }
    };

    private void fling(int velocityX, int velocityY) {
        mScroller.forceFinished(true);
        int mScrollX = getScrollX();
        Log.e("fling  ", "" + mScrollX);
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
    protected void onDraw(Canvas canvas) {
        Log.e("moveBy", "onDraw: ");
        super.onDraw(canvas);
    }

    private void initStartPoint(MotionEvent event) {
        final int pointerIndex = MotionEventCompat.getActionIndex(event);
        final float x = MotionEventCompat.getX(event, pointerIndex);
        final float y = MotionEventCompat.getY(event, pointerIndex);

        // Remember where we started (for dragging)
        startX = x;
        startY = y;
        // Save the ID of this pointer (for dragging)
        mActivePointerId = MotionEventCompat.getPointerId(event, 0);
    }

    public void moveBy(int deltaX, int deltaY) {
        Log.e("moveBy", "deltaX: " + deltaX + "    deltaY: " + deltaY);
//        if (Math.abs(deltaX) >= Math.abs(deltaY)) {
            scrollBy(deltaX, 0);
//        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
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


