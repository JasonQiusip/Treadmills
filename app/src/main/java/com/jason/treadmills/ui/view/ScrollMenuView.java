package com.jason.treadmills.ui.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by Jason on 2015/3/11.
 */
public class ScrollMenuView extends ViewGroup{

    private Scroller mScroller;
    private int mLeftWidth;
    private int mRightWidth;

    public ScrollMenuView(Context context) {
        super(context);
        mScroller = new Scroller(context);
        ScrollMenuView.this.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public ScrollMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(widthMeasureSpec);
        int count = getChildCount();

        mLeftWidth = 0;
        mRightWidth = 0;
        int maxHeight = 0;
        int maxWidth = 0;

        for(int i = 0; i < count; i++)
        {
            View child = getChildAt(i);
            LayoutParams layoutParams = child.getLayoutParams();
            if(layoutParams.width > 0) {
                widthMeasureSpec = layoutParams.width;
            }
            if(layoutParams.height > 0){
                heightMeasureSpec = layoutParams.height;
            }

            child.measure(widthMeasureSpec, heightMeasureSpec);
        }
        scrollTo(width, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        int count = getChildCount();
        for(int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(child.getVisibility() != View.GONE){
                int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }
}


