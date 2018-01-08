package demo.sollian.com.nestedtouchrecyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author sollian on 2018/1/5.
 */

public class NestedTouchRecyclerView extends RecyclerView {
    private float beginX;
    private float beginY;
    private float deltaX;
    private float deltaY;
    private float firstValidDelta;

    private int orientation;

    public NestedTouchRecyclerView(Context context) {
        super(context);
    }

    public NestedTouchRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedTouchRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initWhenDown(e);
                break;
            case MotionEvent.ACTION_MOVE:
                processMove(e);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                firstValidDelta = 0;
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(e);
    }

    private void initWhenDown(MotionEvent e) {
        beginX = e.getX();
        beginY = e.getY();
        firstValidDelta = 0;
        getParent().requestDisallowInterceptTouchEvent(true);
        orientation = getLayoutManager().canScrollHorizontally()
                ? OrientationHelper.HORIZONTAL : OrientationHelper.VERTICAL;
    }

    private boolean handle;

    private void processMove(MotionEvent e) {
        if (firstValidDelta != 0) {
            if (handle) {
                deltaX = e.getX() - beginX;
                deltaY = e.getY() - beginY;
                handleMove(false);
            }
            return;
        }

        deltaX = e.getX() - beginX;
        deltaY = e.getY() - beginY;
        firstValidDelta = Math.abs(deltaX) - Math.abs(deltaY);
        if (orientation == OrientationHelper.VERTICAL) {
            firstValidDelta = -firstValidDelta;
        }

        if (firstValidDelta > 0) {
            handleMove(true);
        } else if (firstValidDelta < 0) {
            handle = false;
            getParent().requestDisallowInterceptTouchEvent(false);
        }
    }

    private void handleMove(boolean isFirst) {
        boolean canScroll2Start;
        boolean canScroll2End;
        if (orientation == OrientationHelper.HORIZONTAL) {
            /*
            当RecyclerView中item数量过多时，canScrollHorizontally(1)会返回错误的false，因为int值溢出了！！
            所以这里多加一层computeHorizontalScrollOffset()的判断
             */
            int offset = computeHorizontalScrollOffset();
            canScroll2Start = deltaX > 0 && canScrollHorizontally(-1);
            canScroll2End = deltaX < 0 && (canScrollHorizontally(1) || offset == Integer.MAX_VALUE);
        } else {
            int offset = computeVerticalScrollOffset();
            canScroll2Start = deltaY > 0 && canScrollVertically(-1);
            canScroll2End = deltaY < 0 && (canScrollVertically(1) || offset == Integer.MAX_VALUE);
        }

        handle = canScroll2Start || canScroll2End;
        if (isFirst) {
            getParent().requestDisallowInterceptTouchEvent(handle);
        }
    }
}
