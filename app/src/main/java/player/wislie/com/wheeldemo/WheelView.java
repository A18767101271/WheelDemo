package player.wislie.com.wheeldemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WheelView extends LinearLayout {

    private Scroller mScroller;


    private int visibleItems = 3; //可见的item数量

    private int itemHeight = 100;//每个item的高度

    private List<String> mDataList = new ArrayList<>();

    //滑轮的宽度
    private int mWheelWidth;
    //滑轮的高度
    private int mWheelHeight;

    //惯性最大最小速度
    protected int mMaximumVelocity, mMinimumVelocity;
    //检测手势的工具，可以获取手势的速度
    private VelocityTracker mVelocityTracker;

    private TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDivderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mMinY, mMaxY; //滑动的最小y值和最大y值
    private int initNeedScrollY; //初始时需要滑动的距离，这是中间的
    private float mLastY; //上一次的y坐标
    private int mCurrPosY; //当前的pos位置

    private WheelListener mWheelListener;

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScroller = new Scroller(context);
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();
        initData();
        initLayoutParam();
        initPaint();
    }

    public void setWheelListener(WheelListener wheelListener) {
        this.mWheelListener = wheelListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredWidth = DenisyUtil.getScreenWidth(getContext());
        setMeasuredDimension(measuredWidth, visibleItems * itemHeight);
    }

    private void initData() {
        mDataList.add("item0"); //0
        mDataList.add("item1"); //midY 1 mCurrPosY:-1 initNeedScrollY：100
        mDataList.add("item2"); //midY 2  mCurrPosY:0 initNeedScrollY：0
        mDataList.add("item3"); //midY 2 currPosY 0  initNeedScrollY 0
        mDataList.add("item4");
        mDataList.add("item5");
        mDataList.add("item6");
        mDataList.add("item7");
        mDataList.add("item8");
        mDataList.add("item9");
        mDataList.add("item10");
        mDataList.add("item11");

        //中间数字是1 往下移动1位
        //中间数字是2 不移动
        //中间数字是3 往上移动1位
        //中间数字是4 往上移动2位
        //...
        //初始化时位于滑轮中间的数字
        int middleDigit = mDataList.size() % 2 == 0 ? mDataList.size() / 2 : mDataList.size() / 2 + 1;
        //初始时需要滑动的距离
        initNeedScrollY = (2 - middleDigit) * itemHeight;
        // 1  minY -1*itemHeight, max 0*itemHeight
        // 12 minY -1*itemHeight max 0*itemHeight
        // 123 minY -1*itemHeight max 1*itemHeight
        // 1234 minY -1*itemHeight max 2*itemHeight
        // 12345 minY -1*itemHeight max 3*itemHeight
        // ...
        //计算minY 和 maxY的值
        mMinY = -itemHeight;
        mMaxY = (mDataList.size() - 1) * itemHeight + mMinY;
    }


    private void initLayoutParam() {
        setOrientation(LinearLayout.VERTICAL);
        mWheelWidth = DenisyUtil.getScreenWidth(getContext());
        mWheelHeight = itemHeight * visibleItems;
        for (int i = 0; i < mDataList.size(); i++) {
            String content = mDataList.get(i);

            TextView itemView = new TextView(getContext());
            itemView.setLayoutParams(new LayoutParams(mWheelWidth, itemHeight));
            itemView.setGravity(Gravity.CENTER);
            itemView.setTextSize(25);
            itemView.setTextColor(Color.GREEN);
            itemView.setText(content);
            addView(itemView);
        }
        this.setLayoutParams(new LayoutParams(mWheelWidth, mWheelHeight));
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //初始滑动的位置
                scrollTo(0, -initNeedScrollY);
            }
        });
    }

    private void initPaint() {
        setBackgroundColor(Color.BLUE);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(2);
        mTextPaint.setTextSize(60);
        mTextPaint.setColor(Color.BLACK);

        mDivderPaint.setStyle(Paint.Style.FILL);
        mDivderPaint.setColor(Color.RED);
        mDivderPaint.setStrokeWidth(2);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        float currY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastY = currY;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = -(currY - mLastY);
                scrollBy(0, (int) deltaY);
                mLastY = currY;
                break;

            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                float yVelocity = mVelocityTracker.getYVelocity();
                if (Math.abs(yVelocity) > mMinimumVelocity) {
                    //抛掷
                    mScroller.fling(0, getScrollY(), 0, (int) -yVelocity,
                            0, 0, mMinY, mMaxY);
                    invalidate();
                } else {
                    //使用startScroll 和 scrollBy 来控制偏移
                    offsetToIntPos();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

            if (!mScroller.computeScrollOffset()) { //滚动结束,滑动偏移到整数位置
                offsetToIntPos();
            }
            //继续调用computeScroll方法
            postInvalidate();
        }

    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < mMinY) {
            y = mMinY;
        }
        if (y > mMaxY) {
            y = mMaxY;
        }

        if (getScrollY() != y) {
            super.scrollTo(x, y);
        }
        //设置为整数
        mCurrPosY = scrollToPosY(y); //这是滑动的点 比如 -3,-2,-1, 0,1, 2, 3, 4...


//        int position = mCurrPosY + 1;
//        if (mWheelListener != null) {
//
//            mWheelListener.onPositionChanging(position);
//        }

    }

    //scrollTo方法中调用 调整位置
    private int scrollToPosY(int y) {

        int currPosY = y / itemHeight;
        if (y % itemHeight != 0) {
            if (Math.abs(y) > currPosY * itemHeight + 0.5 * itemHeight) {
                currPosY = currPosY + y / Math.abs(y);
            }
        }
        return currPosY;
    }

    //滑动偏移并调整到整数位置
    private void offsetToIntPos() {
        int scrollY = getScrollY();
        int currentY = mCurrPosY * itemHeight;
        int dy = currentY - scrollY;
        if (Math.abs(dy) > 0 && Math.abs(dy) < itemHeight / 2) {
            //渐变回弹
            mScroller.startScroll(0, getScrollY(), 0, dy, 500);
            invalidate();
        } else {
            //立刻回弹
            scrollBy(0, dy);
        }

    }

    @Override
    protected void dispatchDraw(Canvas canvas) { //绘制顺序 onDraw -> dispatchDraw, 防止分割线被被覆盖
        super.dispatchDraw(canvas);
        drawDivider(canvas);
    }

    //绘制线条
    private void drawDivider(Canvas canvas) {

        int scrollY = getScrollY();

        canvas.save();
        canvas.translate(0, scrollY);

        canvas.drawLine(0, itemHeight, mWheelWidth, itemHeight, mDivderPaint);
        canvas.drawLine(0, itemHeight * 2, mWheelWidth, itemHeight * 2, mDivderPaint);

        canvas.restore();
    }


    public void setSelectedPos(int pos) {

    }

    public int getSelectedPos() {
        return mCurrPosY + 1;
    }


}
